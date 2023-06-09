package eu.domibus.core.earchive.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.util.FileSystemUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchiveFileStorage {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorage.class);

    private volatile File storageDirectory;

    private final Object storageDirectoryLock = new Object();

    private final Domain domain;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final FileSystemUtil fileSystemUtil;

    public EArchiveFileStorage(Domain domain, DomibusPropertyProvider domibusPropertyProvider, FileSystemUtil fileSystemUtil) {
        this.domain = domain;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.fileSystemUtil = fileSystemUtil;
    }

    @PostConstruct
    public void init() {
        final String eArchiveActive = domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
        if (BooleanUtils.isNotTrue(BooleanUtils.toBooleanObject(eArchiveActive))) {
            LOG.info("eArchiving is not activated for domain [{}]", domain);
            return;
        }
        final String location = domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
        if (StringUtils.isBlank(location)) {
            throw new ConfigurationException("No file system storage defined for eArchiving but the eArchiving is activated.");
        }

        Path path = getPath(location);

        storageDirectory = path.toFile();
        LOG.info("Initialized eArchiving folder on path [{}] for domain [{}]", path, domain);
    }

    private Path getPath(String location) {
        Path path = fileSystemUtil.createLocation(location);

        if (path == null) {
            throw new ConfigurationException("There was an error initializing the eArchiving folder but the eArchiving is activated.");
        }
        return path;
    }

    public File getStorageDirectory() {
        if (storageDirectory == null) {
            synchronized (storageDirectoryLock) {
                if (storageDirectory == null) {
                    init();
                }
            }
        }
        return storageDirectory;
    }
}
