package gca.xap.logger;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

public class DefaultFilesProcessingStrategyTest {

	@Test
	public void should_support_empty_list() {
		DefaultFilesProcessingStrategy strategy = new DefaultFilesProcessingStrategy();

		RetentionConfiguration retentionConfiguration = RetentionConfiguration.builder().minFilesCount(2).maxFilesCount(7).minRetentionInMilliseconds(60000).build();
		strategy.processFilesList(new ArrayList<>(), retentionConfiguration, null);
	}

	@Test
	public void should_support_OneItem_non_existing_list() {
		DefaultFilesProcessingStrategy strategy = new DefaultFilesProcessingStrategy();

		List<File> files = new ArrayList<>();
		File file1 = mock(File.class, DefaultUnexpectedMockInvocationAnswer.singleton);
		files.add(file1);

		doReturn(false).when(file1).exists();

		RetentionConfiguration retentionConfiguration = RetentionConfiguration.builder().minFilesCount(2).maxFilesCount(7).minRetentionInMilliseconds(60000).build();
		strategy.processFilesList(files, retentionConfiguration, null);
	}

	@Test
	public void should_support_OneItem_existing_list() throws IOException {
		DefaultFilesProcessingStrategy strategy = new DefaultFilesProcessingStrategy();

		List<File> files = new ArrayList<>();
		File file1 = mock(File.class, DefaultUnexpectedMockInvocationAnswer.singleton);
		files.add(file1);

		doReturn(true).when(file1).exists();

		FileCallback fileCallback = mock(FileCallback.class);
		doThrow(new UnsupportedMockUsageError()).when(fileCallback).process(any(File.class));

		RetentionConfiguration retentionConfiguration = RetentionConfiguration.builder().minFilesCount(2).maxFilesCount(7).minRetentionInMilliseconds(60000).build();
		strategy.processFilesList(files, retentionConfiguration, fileCallback);
	}

	@Test
	public void should_support_MinimumFilesCountReached_existing_list() throws IOException {
		DefaultFilesProcessingStrategy strategy = new DefaultFilesProcessingStrategy();

		List<File> files = new ArrayList<>();
		File file1 = mock(File.class, DefaultUnexpectedMockInvocationAnswer.singleton);
		File file2 = mock(File.class, DefaultUnexpectedMockInvocationAnswer.singleton);
		files.add(file1);
		files.add(file2);

		doReturn(true).when(file1).exists();

		FileCallback fileCallback = mock(FileCallback.class);
		doThrow(new UnsupportedMockUsageError()).when(fileCallback).process(any(File.class));

		RetentionConfiguration retentionConfiguration = RetentionConfiguration.builder().minFilesCount(2).maxFilesCount(7).minRetentionInMilliseconds(60000).build();
		strategy.processFilesList(files, retentionConfiguration, fileCallback);
	}

	@Test
	public void should_support_MinimumFilesCountExceeded_existing_list() throws IOException {
		DefaultFilesProcessingStrategy strategy = new DefaultFilesProcessingStrategy();

		int minFilesCount = 3;
		int actualFilesCount = minFilesCount + 2;
		long retentionInMilliseconds = 60000;

		long now = System.currentTimeMillis();
		long actualFilesAge = 90000;

		FileCallback fileCallback = mock(FileCallback.class);
		List<File> files = Collections.synchronizedList(new LinkedList<>());
		for (int i = 0; i < actualFilesCount; i++) {
			File file = mock(File.class, DefaultUnexpectedMockInvocationAnswer.singleton);
			doReturn(true).when(file).exists();
			doReturn((now += 10) - actualFilesAge).when(file).lastModified();
			if (i < actualFilesCount - minFilesCount) {
				doNothing().when(fileCallback).process(same(file));
			}
			files.add(file);
		}

		RetentionConfiguration retentionConfiguration = RetentionConfiguration.builder().minFilesCount(minFilesCount).maxFilesCount(minFilesCount + 5).minRetentionInMilliseconds(retentionInMilliseconds).build();
		strategy.processFilesList(files, retentionConfiguration, fileCallback);

		verify(fileCallback, times(actualFilesCount - minFilesCount)).process(any(File.class));
	}

	@Test
	public void should_support_MaximumFilesCountExceeded_existing_list_and_files_are_old() throws IOException {
		DefaultFilesProcessingStrategy strategy = new DefaultFilesProcessingStrategy();

		int minFilesCount = 3;
		int maxFilesCount = minFilesCount + 5;
		int actualFilesCount = maxFilesCount + 2;
		long retentionInMilliseconds = 60000;

		long now = System.currentTimeMillis();
		long actualFilesAge = 90000;

		FileCallback fileCallback = mock(FileCallback.class);
		List<File> files = Collections.synchronizedList(new LinkedList<>());
		for (int i = 0; i < actualFilesCount; i++) {
			File file = mock(File.class, DefaultUnexpectedMockInvocationAnswer.singleton);
			doReturn(true).when(file).exists();
			doReturn((now += 10) - actualFilesAge).when(file).lastModified();
			if (i < actualFilesCount - minFilesCount) {
				doNothing().when(fileCallback).process(same(file));
			}
			files.add(file);
		}

		RetentionConfiguration retentionConfiguration = RetentionConfiguration.builder().minFilesCount(minFilesCount).maxFilesCount(maxFilesCount).minRetentionInMilliseconds(retentionInMilliseconds).build();
		strategy.processFilesList(files, retentionConfiguration, fileCallback);

		verify(fileCallback, times(actualFilesCount - minFilesCount)).process(any(File.class));
	}


	@Test
	public void should_support_MaximumFilesCountExceeded_existing_list_and_file_are_new() throws IOException {
		DefaultFilesProcessingStrategy strategy = new DefaultFilesProcessingStrategy();

		int minFilesCount = 3;
		int maxFilesCount = minFilesCount + 5;
		int actualFilesCount = maxFilesCount + 2;
		long retentionInMilliseconds = 60000;

		long now = System.currentTimeMillis();
		long actualFilesAge = 30000;

		FileCallback fileCallback = mock(FileCallback.class);
		List<File> files = Collections.synchronizedList(new LinkedList<>());
		for (int i = 0; i < actualFilesCount; i++) {
			File file = mock(File.class, DefaultUnexpectedMockInvocationAnswer.singleton);
			doReturn(true).when(file).exists();
			doReturn((now += 10) - actualFilesAge).when(file).lastModified();
			if (i < actualFilesCount - minFilesCount) {
				doNothing().when(fileCallback).process(same(file));
			}
			files.add(file);
		}

		RetentionConfiguration retentionConfiguration = RetentionConfiguration.builder().minFilesCount(minFilesCount).maxFilesCount(maxFilesCount).minRetentionInMilliseconds(retentionInMilliseconds).build();
		strategy.processFilesList(files, retentionConfiguration, fileCallback);

		verify(fileCallback, times(actualFilesCount - maxFilesCount)).process(any(File.class));
	}


	public static class UnsupportedMockUsageError extends Error {

	}

}
