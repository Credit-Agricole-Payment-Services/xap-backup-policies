package gca.xap.logger;

import lombok.Builder;
import lombok.ToString;

@ToString
@Builder
public class RetentionConfiguration {

	final long minRetentionInMilliseconds;

	final int minFilesCount;

	final int maxFilesCount;

	public void validate() {
		assertTrue(minRetentionInMilliseconds > 0, "minRetentionInMilliseconds should be > 0");
		assertTrue(minFilesCount > 0, "minFilesCount should be > 0");
		assertTrue(maxFilesCount >= minFilesCount, "maxFilesCount should be >= minFilesCount");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}

}
