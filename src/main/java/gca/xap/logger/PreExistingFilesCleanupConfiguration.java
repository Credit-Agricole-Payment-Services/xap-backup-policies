package gca.xap.logger;

import lombok.Builder;
import lombok.ToString;

import java.io.File;

@ToString
@Builder
public class PreExistingFilesCleanupConfiguration {

	final boolean enabled;

	final File logDirectory;

	final String filenamePattern;

}
