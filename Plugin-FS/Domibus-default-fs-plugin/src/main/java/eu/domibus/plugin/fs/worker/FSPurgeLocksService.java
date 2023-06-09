package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSFileNameHelper;
import eu.domibus.plugin.fs.FSFilesManager;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for the purge of orphan/zombie lock files
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class FSPurgeLocksService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeLocksService.class);

    private final FSDomainService fsDomainService;

    private final FSFilesManager fsFilesManager;

    private final FSFileNameHelper fsFileNameHelper;

    private final FSPluginProperties fsPluginProperties;

    public FSPurgeLocksService(FSDomainService fsDomainService, FSFilesManager fsFilesManager, FSFileNameHelper fsFileNameHelper, FSPluginProperties fsPluginProperties) {
        this.fsDomainService = fsDomainService;
        this.fsFilesManager = fsFilesManager;
        this.fsFileNameHelper = fsFileNameHelper;
        this.fsPluginProperties = fsPluginProperties;
    }

    public void purge() {
        LOG.debug("Purging orphan lock files....");

        final String domain = fsDomainService.getFSPluginDomain();

        purgeForDomain(domain);
    }

    protected void purgeForDomain(String domain) {
        Integer expirationLimit = fsPluginProperties.getLocksPurgeExpired(domain);
        if (expirationLimit == 0) {
            LOG.debug("Exiting purge lock files for domain [{}] as the expiration limit is set to 0.", domain);
            return;
        }

        FileObject[] files = null;
        try (FileObject targetFolder = fsFilesManager.getEnsureChildFolder(fsFilesManager.setUpFileSystem(domain), FSFilesManager.OUTGOING_FOLDER)) {
            files = fsFilesManager.findAllDescendantFiles(targetFolder);
            LOG.trace("Found [{}] files: [{}]", files.length, files);

            List<FileObject> lockFiles = Arrays.stream(files)
                    .filter(file -> fsFileNameHelper.isLockFile(file.getName().getBaseName()))
                    .filter(file -> fsFilesManager.isFileOlderThan(file, expirationLimit))
                    .collect(Collectors.toList());
            LOG.debug("Found [{}] locked file names: [{}]", lockFiles.size(), lockFiles.stream().map(file -> file.getName().getBaseName()).toArray());

            for (FileObject lockFile : lockFiles) {
                String dataFileName = fsFileNameHelper.stripLockSuffix(targetFolder.getName().getRelativeName(lockFile.getName()));
                if (!fsFilesManager.fileExists(targetFolder, dataFileName)) {
                    LOG.debug("File [{}] does not exists so delete the corresponding lock file.", dataFileName);
                    fsFilesManager.deleteFile(lockFile);
                }
            }
        } catch (FileSystemException ex) {
            LOG.error("Error purging orphan lock files", ex);
        } catch (FSSetUpException ex) {
            LOG.error("Error setting up folders for domain [{}]", domain, ex);
        } finally {
            if (files != null) {
                fsFilesManager.closeAll(files);
            }
        }
    }
}
