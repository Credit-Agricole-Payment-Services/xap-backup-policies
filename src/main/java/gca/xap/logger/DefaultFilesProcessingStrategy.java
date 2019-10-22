package gca.xap.logger;

import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultFilesProcessingStrategy implements FilesProcessingStrategy {

	private final static Logger LOGGER = Logger.getLogger(DefaultFilesProcessingStrategy.class.getName());

	@ToString
	private static final class ProcessingReport {
		private final AtomicInteger initialFilesCount = new AtomicInteger(0);
		private final AtomicInteger finalFilesCount = new AtomicInteger(0);

		private final AtomicInteger fileNotFoundCount = new AtomicInteger(0);
		private final AtomicInteger fileOldEnoughToBeProcessCount = new AtomicInteger(0);
		private final AtomicInteger fileToProcessCount = new AtomicInteger(0);
		private final AtomicInteger fileProcessErrorCount = new AtomicInteger(0);
	}

	@Override
	public void processFilesList(Collection<File> files, RetentionConfiguration retentionConfiguration, FileCallback fileCallback) {
		LOGGER.info("processFilesList() : files.size() = " + files.size() + ", retentionConfiguration = " + retentionConfiguration);
		final ProcessingReport processingReport = new ProcessingReport();
		processingReport.initialFilesCount.set(files.size());
		Iterator iterator = files.iterator();
		while (files.size() > retentionConfiguration.minFilesCount && iterator.hasNext()) {
			File currentFile = (File) iterator.next();
			// if currentFile does not exists on the filesystem, we just untrack it (maybe it has been deleted externally)
			if (!currentFile.exists()) {
				processingReport.fileNotFoundCount.incrementAndGet();
				iterator.remove();
				break;
			}

			if (!isFileOldEnoughToBeProcess(currentFile, retentionConfiguration.minRetentionInMilliseconds)) {
				if (files.size() <= retentionConfiguration.maxFilesCount) {
					break;
				}
			} else {
				processingReport.fileOldEnoughToBeProcessCount.incrementAndGet();
			}

			try {
				processingReport.fileToProcessCount.incrementAndGet();
				fileCallback.process(currentFile);
			} catch (RuntimeException | IOException e) {
				processingReport.fileProcessErrorCount.incrementAndGet();
				LOGGER.log(Level.SEVERE, "Failed to process file: " + currentFile.getAbsolutePath(), e);
				e.printStackTrace(System.err);
			} finally {
				iterator.remove();
			}
		}
		processingReport.finalFilesCount.set(files.size());
		LOGGER.info("processFilesList() : processingReport = " + processingReport);
	}

	private boolean isFileOldEnoughToBeProcess(File currentFile, long retentionInMilliseconds) {
		final long now = System.currentTimeMillis();
		return currentFile.lastModified() <= now - retentionInMilliseconds;
	}

}
