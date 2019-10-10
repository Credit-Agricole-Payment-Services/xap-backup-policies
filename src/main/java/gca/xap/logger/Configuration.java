package gca.xap.logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@ToString
public class Configuration {

	private static final String SYSPROP_PROP_PREFIX = CompressBackupPolicy.class.getName() + ".";

	private final static Logger LOGGER = Logger.getLogger(Configuration.class.getName());

	private static final String keepAsIsPrefix = "keepAsIs.";

	private static final String keepCompressedPrefix = "keepCompressed.";

	@Getter
	private final RetentionConfiguration keepAsIs;

	@Getter
	private final RetentionConfiguration keepCompressed;

	@Getter
	private final File archivesDirectory;

	@Getter
	private final String filenamePattern;

	@Setter(AccessLevel.PROTECTED)
	private PlaceholderResolver placeholderResolver = new PlaceholderResolver();

	public Configuration(LogManager manager) {
		keepAsIs = RetentionConfiguration.builder()
				.minRetentionInMilliseconds(resolvePolicyProperty(manager, keepAsIsPrefix + "minRetentionInMilliseconds", TimeUnit.MINUTES.convert(30, TimeUnit.MILLISECONDS)))
				.minFilesCount(resolvePolicyProperty(manager, keepAsIsPrefix + "minFilesCount", 2))
				.maxFilesCount(resolvePolicyProperty(manager, keepAsIsPrefix + "maxFilesCount", 10))
				.build();

		keepCompressed = RetentionConfiguration.builder()
				.minRetentionInMilliseconds(resolvePolicyProperty(manager, keepCompressedPrefix + "minRetentionInMilliseconds", TimeUnit.DAYS.convert(7, TimeUnit.MILLISECONDS)))
				.minFilesCount(resolvePolicyProperty(manager, keepCompressedPrefix + "minFilesCount", 4 * 7))
				.maxFilesCount(resolvePolicyProperty(manager, keepCompressedPrefix + "maxFilesCount", 50 * 7))
				.build();

		keepAsIs.validate();
		keepCompressed.validate();

		String archivesDirectoryPath = resolvePolicyProperty(manager, "archivesDirectoryPath", null);
		archivesDirectory = archivesDirectoryPath == null ? null : new File(archivesDirectoryPath);

		filenamePattern = resolvePolicyProperty(manager, "filename-pattern", null);
	}

	public PreExistingFilesCleanupConfiguration toPreExistingFilesCleanupConfiguration() {
		PreExistingFilesCleanupConfiguration.PreExistingFilesCleanupConfigurationBuilder preExistingFilesCleanupConfigurationBuilder = PreExistingFilesCleanupConfiguration
				.builder();
		if (filenamePattern == null) {
			LOGGER.info("filenamePattern is not set, skipping");
			preExistingFilesCleanupConfigurationBuilder.enabled(false);
		} else {
			LOGGER.info("toPreExistingFilesCleanupConfiguration() : filenamePattern = " + filenamePattern);
			File patternFile = new File(fixFilePattern(filenamePattern));
			preExistingFilesCleanupConfigurationBuilder
					.enabled(true)
					.logDirectory(patternFile.getParentFile())
					.filenamePattern(patternFile.getName());
			//
		}
		PreExistingFilesCleanupConfiguration preExistingFilesCleanupConfiguration = preExistingFilesCleanupConfigurationBuilder.build();
		LOGGER.info("preExistingFilesCleanupConfiguration = " + preExistingFilesCleanupConfiguration);
		return preExistingFilesCleanupConfiguration;
	}


	private String fixFilePattern(final String initialCleanupFilenamePattern) {
		String result = initialCleanupFilenamePattern.replace("{host}", placeholderResolver.getHost());
		result = result.replace("{service}", placeholderResolver.getService());
		return result;
	}

	private String resolvePolicyProperty(LogManager manager, String property, String defaultValue) {
		String propertyKey = SYSPROP_PROP_PREFIX + property;
		String propertyValue = System.getProperty(propertyKey, manager.getProperty(propertyKey));
		LOGGER.config("Property " + propertyKey + " value is " + propertyValue);
		if (propertyValue == null) {
			LOGGER.config("Using default value " + defaultValue + " for property " + propertyKey);
			return defaultValue;
		}
		return propertyValue;
	}

	private int resolvePolicyProperty(LogManager manager, String property, int defaultValue) {
		return Integer.parseInt(resolvePolicyProperty(manager, property, Integer.toString(defaultValue)));
	}

	private long resolvePolicyProperty(LogManager manager, String property, long defaultValue) {
		return Long.parseLong(resolvePolicyProperty(manager, property, Long.toString(defaultValue)));
	}

}
