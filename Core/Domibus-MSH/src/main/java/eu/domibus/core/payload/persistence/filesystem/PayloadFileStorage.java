package eu.domibus.core.payload.persistence.filesystem;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ATTACHMENT_STORAGE_LOCATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION;

/**
 * @author Ioana Dragusanu
 * @author Martini Federico
 * @version 2.0
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PayloadFileStorage {

    public static final String ATTACHMENT_STORAGE_LOCATION = DOMIBUS_ATTACHMENT_STORAGE_LOCATION;
    public static final String TEMPORARY_ATTACHMENT_STORAGE_LOCATION = DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadFileStorage.class);

    private File storageDirectory = null;

    private final Domain domain;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    public PayloadFileStorage(Domain domain) {
        this.domain = domain;
    }

    @PostConstruct
    public void init() {
        final String location = domibusPropertyProvider.getProperty(this.domain, ATTACHMENT_STORAGE_LOCATION);
        if (StringUtils.isBlank(location)) {
            LOG.warn("No file system storage defined. This is fine for small attachments but might lead to database issues when processing large payloads");
            return;
        }

        Path path = createLocation(location).normalize();
        if (path == null) {
            LOG.warn("There was an error initializing the payload folder, so Domibus will be using the database");
            return;
        }

        storageDirectory = path.toFile();
        LOG.info("Initialized payload folder on path [{}] for domain [{}]", path, this.domain);
    }

    public File getStorageDirectory() {
        return storageDirectory;
    }

    public void reset() {
        storageDirectory = null;
        init();
    }

    /**
     * It attempts to create the directory whenever is not present.
     * It works also when the location is a symbolic link.
     */
    protected Path createLocation(String path) {
        Path payloadPath;
        try {
            payloadPath = Paths.get(path).normalize();
            if (!payloadPath.isAbsolute()) {
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Relative path [" + payloadPath + "] is forbidden. Please provide absolute path for payload storage");
            }
            // Checks if the path exists, if not it creates it
            if (Files.notExists(payloadPath)) {
                Files.createDirectories(payloadPath);
                LOG.info("The payload folder [{}] has been created!", payloadPath.toAbsolutePath());
            } else {
                if (Files.isSymbolicLink(payloadPath)) {
                    payloadPath = Files.readSymbolicLink(payloadPath);
                }

                if (!Files.isWritable(payloadPath)) {
                    throw new IOException("Write permission for payload folder " + payloadPath.toAbsolutePath() + " is not granted.");
                }
            }
        } catch (IOException ioEx) {
            LOG.error("Error creating/accessing the payload folder [{}]", path, ioEx);

            // Takes temporary folder by default if it faces any issue while creating defined path.
            payloadPath = Paths.get(System.getProperty("java.io.tmpdir")).normalize();
            LOG.warn(WarningUtil.warnOutput("The temporary payload folder " + payloadPath.toAbsolutePath() + " has been selected!"));
        }
        return payloadPath;
    }
}
