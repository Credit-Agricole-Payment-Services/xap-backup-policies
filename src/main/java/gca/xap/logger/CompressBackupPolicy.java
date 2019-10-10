package gca.xap.logger;

import com.gigaspaces.logger.BackupPolicy;
import lombok.AccessLevel;
import lombok.Setter;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class CompressBackupPolicy implements BackupPolicy {

	private final static Logger LOGGER = Logger.getLogger(CompressBackupPolicy.class.getName());

	/**
	 * list is thread-safe
	 */
	private final List<File> trackedFiles = Collections.synchronizedList(new LinkedList<>());

	private final AtomicBoolean initialCleanupPerformed = new AtomicBoolean(false);

	@Setter(AccessLevel.PROTECTED)
	private CompressThenDeleteFileCallback compressFileCallback;

	@Setter(AccessLevel.PROTECTED)
	private DeleteFileCallback deleteFileCallback;

	@Setter(AccessLevel.PROTECTED)
	private FilesFinder filesFinder = new DefaultFilesFinder();

	@Setter(AccessLevel.PROTECTED)
	private FilesProcessingStrategy filesProcessingStrategy = new DefaultFilesProcessingStrategy();

	@Setter(AccessLevel.PROTECTED)
	private Configuration configuration;

	private PreExistingFilesCleanupConfiguration preExistingFilesCleanupConfiguration;

	@Setter(AccessLevel.PROTECTED)
	private ExecutorService executorService = Executors.newSingleThreadExecutor(task -> {

		Thread newThread = new Thread(() -> {
			// catch any Exception in order to print it
			try {
				task.run();
			} catch (Throwable e) {
				LOGGER.log(Level.SEVERE, "Failed to execute task.", e);
				e.printStackTrace(System.err);
			}
		});

		String originalThreadName = newThread.getName();
		// rename the thread for better Thread Dumps
		newThread.setName(CompressBackupPolicy.class.getSimpleName() + "-" + originalThreadName);

		return newThread;
	});

	public CompressBackupPolicy() {
		LogManager manager = LogManager.getLogManager();
		configuration = new Configuration(manager);

		preExistingFilesCleanupConfiguration = configuration.toPreExistingFilesCleanupConfiguration();

		compressFileCallback = new GzipThenDeleteFileCallback(configuration.getArchivesDirectory());
		deleteFileCallback = new DeleteFileCallback();
	}

	@Override
	public void track(File file) {
		LOGGER.info("Tracking file: " + file.getAbsolutePath());
		if (!trackedFiles.contains(file)) {
			trackedFiles.add(file);
		}
		if (initialCleanupPerformed.compareAndSet(false, true)) {
			if (preExistingFilesCleanupConfiguration.enabled) {
				executorService.submit(new CatchingExceptionRunnable(() -> cleanupPreExistingLogFiles(preExistingFilesCleanupConfiguration.logDirectory, preExistingFilesCleanupConfiguration.filenamePattern)));
				executorService.submit(new CatchingExceptionRunnable(() -> cleanupPreExistingCompressedFiles(preExistingFilesCleanupConfiguration.logDirectory, preExistingFilesCleanupConfiguration.filenamePattern)));
			}
		}
		executorService.submit(new CatchingExceptionRunnable(this::cleanupTrackedFiles));
	}

	void cleanupTrackedFiles() {
		filesProcessingStrategy.processFilesList(trackedFiles, configuration.getKeepAsIs(), compressFileCallback);
	}

	void cleanupPreExistingLogFiles(File logDirectory, String filenamePattern) {
		List<File> existingLogFilesList = filesFinder.findFiles(logDirectory, filenamePattern);
		filesProcessingStrategy.processFilesList(existingLogFilesList, configuration.getKeepAsIs(), compressFileCallback);
	}

	void cleanupPreExistingCompressedFiles(File logDirectory, String filenamePattern) {
		filenamePattern = filenamePattern + "\\." + compressFileCallback.getFileExtension();
		{
			List<File> existingCompressedLogFilesList = filesFinder.findFiles(logDirectory, filenamePattern);
			filesProcessingStrategy.processFilesList(existingCompressedLogFilesList, configuration.getKeepCompressed(), deleteFileCallback);
		}
		{
			List<File> existingCompressedLogFilesList = filesFinder.findFiles(configuration.getArchivesDirectory(), filenamePattern);
			filesProcessingStrategy.processFilesList(existingCompressedLogFilesList, configuration.getKeepCompressed(), deleteFileCallback);
		}
	}

}
