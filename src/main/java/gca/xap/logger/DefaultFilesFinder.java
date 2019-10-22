package gca.xap.logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DefaultFilesFinder implements FilesFinder {

	private final static Logger LOGGER = Logger.getLogger(DefaultFilesFinder.class.getName());

	private final static Comparator<File> filesComparator = Comparator
			.comparingLong(File::lastModified)
			.thenComparing(File::getName);

	@Override
	public List<File> findFiles(File directory, String filenamePattern) {
		File[] matchingFiles = directory.listFiles((parentDirectory, filename) -> filename.matches(filenamePattern));
		if (matchingFiles != null) {
			LOGGER.info("Found " + matchingFiles.length + " files matching the pattern " + filenamePattern);
			return Arrays
					.stream(matchingFiles)
					.sorted(
							filesComparator
					)
					.collect(Collectors.toList());
		} else {
			LOGGER.info("Found no files matching the pattern " + filenamePattern);
			return new ArrayList<>();
		}
	}

}
