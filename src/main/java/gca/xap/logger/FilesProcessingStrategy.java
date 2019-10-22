package gca.xap.logger;

import java.io.File;
import java.util.Collection;

public interface FilesProcessingStrategy {
	void processFilesList(Collection<File> files, RetentionConfiguration retentionConfiguration, FileCallback fileCallback);
}
