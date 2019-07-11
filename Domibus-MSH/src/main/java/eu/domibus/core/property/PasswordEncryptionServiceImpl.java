package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.*;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.util.DomibusEncryptionException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionServiceImpl.class);

    public static final String ENCRYPTED_KEY = "encrypted.key";
    public static final String ENC_START = "ENC(";
    public static final String ENC_END = ")";
    protected static final DateTimeFormatter BACKUP_FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss.SSS");


    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected PasswordEncryptionDao passwordEncryptionDao;

    @Autowired
    protected EncryptionUtil encryptionUtil;

    @Autowired
    protected DateUtil dateUtil;


    @Override
    public void encryptPasswords() {
        LOG.debug("Checking if password encryption is configured");

        final PasswordEncryptionContextDefault passwordEncryptionContext = new PasswordEncryptionContextDefault(domibusPropertyProvider, domibusConfigurationService);
        encryptPasswordsIfConfigured(passwordEncryptionContext);


        if (domibusConfigurationService.isMultiTenantAware()) {
            final List<Domain> domains = domainService.getDomains();
            for (Domain domain : domains) {
                final PasswordEncryptionContextDomain passwordEncryptionContextDomain = new PasswordEncryptionContextDomain(domibusPropertyProvider, domibusConfigurationService, domain);
                encryptPasswordsIfConfigured(passwordEncryptionContextDomain);
            }
        }

        LOG.debug("Finished checking if password encryption is configured");
    }

    protected List<String> getPropertiesToEncrypt(PasswordEncryptionContext passwordEncryptionContext) {
        final String propertiesToEncryptString = passwordEncryptionContext.getProperty("domibus.password.encryption.properties");
        if (StringUtils.isEmpty(propertiesToEncryptString)) {
            LOG.debug("No properties to encrypt");
            return new ArrayList<>();
        }
        final String[] propertiesToEncrypt = StringUtils.split(propertiesToEncryptString, ",");
        LOG.debug("The following properties are configured for encryption [{}]", propertiesToEncrypt);

        List<String> result = Arrays.stream(propertiesToEncrypt).filter(propertyName -> {
            final String propertyValue = passwordEncryptionContext.getProperty(propertyName);
            if (!isValueEncrypted(propertyValue)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());


        LOG.debug("The following properties are not encrypted [{}]", result);

        return result;
    }

    protected void encryptPasswordsIfConfigured(PasswordEncryptionContext passwordEncryptionContext) {
        LOG.debug("Checking if the encryption key should be created");

        final Boolean encryptionActive = passwordEncryptionContext.isPasswordEncryptionActive();
        if (!encryptionActive) {
            LOG.debug("Password encryption is not activate");
            return;
        }

        final List<String> propertiesToEncrypt = getPropertiesToEncrypt(passwordEncryptionContext);
        if (CollectionUtils.isEmpty(propertiesToEncrypt)) {
            LOG.debug("No properties are needed to be encrypted");
            return;
        }

        final File encryptedKeyFile = getEncryptedKeyFile(passwordEncryptionContext);

        PasswordEncryptionSecret secret = null;
        if (encryptedKeyFile.exists()) {
            secret = passwordEncryptionDao.getSecret(encryptedKeyFile);
        } else {
            secret = passwordEncryptionDao.createSecret(encryptedKeyFile);
        }

        LOG.debug("Using encrypted key file [{}]", encryptedKeyFile);
        final SecretKey secretKey = encryptionUtil.getSecretKey(secret.getSecretKey());
        final IvParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(secret.getInitVector());
        final List<PasswordEncryptionResult> encryptedProperties = encryptProperties(passwordEncryptionContext, propertiesToEncrypt, secretKey, secretKeySpec);

        replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);

        LOG.debug("Finished creating the encryption key");
    }

    public File getEncryptedKeyFile(PasswordEncryptionContext passwordEncryptionContext) {
        final String encryptionKeyLocation = passwordEncryptionContext.getProperty("domibus.password.encryption.key.location");
        LOG.debug("Configured encryptionKeyLocation [{}]", encryptionKeyLocation);

        return new File(encryptionKeyLocation, ENCRYPTED_KEY);
    }

    protected List<PasswordEncryptionResult> encryptProperties(PasswordEncryptionContext passwordEncryptionContext, List<String> propertiesToEncrypt, SecretKey secretKey, IvParameterSpec secretKeySpec) {
        List<PasswordEncryptionResult> result = new ArrayList<>();

        LOG.debug("Encrypting properties");

        for (String propertyName : propertiesToEncrypt) {
            final PasswordEncryptionResult passwordEncryptionResult = encryptProperty(passwordEncryptionContext, secretKey, secretKeySpec, propertyName);
            if (passwordEncryptionResult != null) {
                LOG.debug("Property [{}] encrypted [{}]", propertyName, passwordEncryptionResult.getFormattedBase64EncryptedValue());

                result.add(passwordEncryptionResult);
            }
        }

        return result;
    }

    //TODO add cache
    public String decryptProperty(PasswordEncryptionContext passwordEncryptionContext, String propertyName, String encryptedFormatValue) {
        final boolean valueEncrypted = isValueEncrypted(encryptedFormatValue);
        if (!valueEncrypted) {
            LOG.trace("Property [{}] is not encrypted: skipping decrypting value", propertyName);
            return encryptedFormatValue;
        }

        final File encryptedKeyFile = getEncryptedKeyFile(passwordEncryptionContext);

        PasswordEncryptionSecret secret = passwordEncryptionDao.getSecret(encryptedKeyFile);
        LOG.debug("Using encrypted key file for decryption [{}]", encryptedKeyFile);

        final SecretKey secretKey = encryptionUtil.getSecretKey(secret.getSecretKey());
        final IvParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(secret.getInitVector());

        String base64EncryptedValue = extractValueFromEncryptedFormat(encryptedFormatValue);
        final byte[] encryptedValue = Base64.decodeBase64(base64EncryptedValue);

        return encryptionUtil.decrypt(encryptedValue, secretKey, secretKeySpec);
    }


    protected PasswordEncryptionResult encryptProperty(PasswordEncryptionContext passwordEncryptionContext, SecretKey secretKey, IvParameterSpec secretKeySpec, String propertyName) {
        final String propertyValue = passwordEncryptionContext.getProperty(propertyName);

        if (isValueEncrypted(propertyValue)) {
            LOG.debug("Property [{}] is already encrypted", propertyName);
            return null;
        }

        final byte[] encryptedValue = encryptionUtil.encrypt(propertyValue.getBytes(), secretKey, secretKeySpec);
        final String base64EncryptedValue = Base64.encodeBase64String(encryptedValue);

        final PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName(propertyName);
        passwordEncryptionResult.setPropertyValue(propertyValue);
        passwordEncryptionResult.setBase64EncryptedValue(base64EncryptedValue);
        passwordEncryptionResult.setFormattedBase64EncryptedValue(formatEncryptedValue(base64EncryptedValue));
        return passwordEncryptionResult;
    }

    protected String formatEncryptedValue(String value) {
        return String.format(ENC_START + "%s" + ENC_END, value);
    }

    protected String extractValueFromEncryptedFormat(String encryptedFormat) {
        return StringUtils.substringBetween(encryptedFormat, ENC_START, ENC_END);
    }

    protected boolean isValueEncrypted(final String propertyValue) {
        if (StringUtils.trim(propertyValue).startsWith(ENC_START)) {
            return true;
        }
        return false;
    }

    protected void replacePropertiesInFile(PasswordEncryptionContext passwordEncryptionContext, List<PasswordEncryptionResult> encryptedProperties) {
        final File configurationFile = getConfigurationFile(passwordEncryptionContext);

        LOG.debug("Replacing configured properties in file [{}] with encrypted values");

        final Stream<String> lines;
        try {
            lines = Files.lines(configurationFile.toPath());
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not replace properties: could not read configuration file [%s]", configurationFile), e);
        }

        final List<String> fileLines = lines
                .map(line -> {
                    if (!line.contains("=")) {
                        return line;
                    }
                    final String[] strings = line.split("=");
                    final String propertyName = StringUtils.trim(strings[0]);
                    if (strings.length != 2) {
                        LOG.trace("Property [{}] is empty", propertyName);
                        return line;
                    }
                    final Optional<PasswordEncryptionResult> encryptedValueOptional = encryptedProperties.stream()
                            .filter(encryptionResult -> encryptionResult.getPropertyName().equals(propertyName))
                            .findFirst();
                    if (!encryptedValueOptional.isPresent()) {
                        LOG.trace("Property [{}] is not encrypted", propertyName);
                        return line;
                    }
                    final PasswordEncryptionResult passwordEncryptionResult = encryptedValueOptional.get();
                    LOG.debug("Replacing value for property [{}] with [{}]", propertyName, passwordEncryptionResult.getFormattedBase64EncryptedValue());

                    String newLine = StringUtils.replace(line, passwordEncryptionResult.getPropertyValue(), passwordEncryptionResult.getFormattedBase64EncryptedValue());

                    return newLine;
                })
                .collect(Collectors.toList());

        LOG.debug("Writing encrypted values ");

        final File configurationFileBackup = getConfigurationFileBackup(configurationFile);

        LOG.debug("Backing up file [{}] to file [{}]", configurationFile, configurationFileBackup);
        try {
            FileUtils.copyFile(configurationFile, configurationFileBackup);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not back up file [%s] to [%s]", configurationFile, configurationFileBackup), e);
        }

        try {
            Files.write(configurationFile.toPath(), fileLines);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not write encrypted values to file [%s] ", configurationFile), e);
        }
    }

    protected File getConfigurationFileBackup(File configurationFile) {
        return new File(configurationFile.getParent(), configurationFile.getName() + ".backup-" + dateUtil.getCurrentTime(BACKUP_FILE_FORMATTER));
    }

    protected File getConfigurationFile(PasswordEncryptionContext passwordEncryptionContext) {
        final String propertyFileName = passwordEncryptionContext.getConfigurationFileName();
        return new File(domibusConfigurationService.getConfigLocation() + File.separator + propertyFileName);
    }


}
