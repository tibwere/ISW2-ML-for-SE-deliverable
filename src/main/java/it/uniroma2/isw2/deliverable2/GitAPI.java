package it.uniroma2.isw2.deliverable2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class GitAPI {

	private static final String SHELL = "/bin/sh";
	private static final String COMMAND_FLAG = "-c";
	private static final String CLONE_CMD = "/usr/bin/git clone https://github.com/apache/%s.git";
	private static final String GET_SHA_AND_DATE_CMD = "/usr/bin/git log --pretty=format:\"%H %ad\" --date=iso-strict --reverse";
	private static final String CHECKOUT_CMD = "/usr/bin/git checkout %s";

	private File pwd;
	private String projectName;

	public GitAPI(String projectName) {
		this.projectName = projectName;
	}

	public void cloneRepository() throws IOException, InterruptedException {
		final String TMP_PREFIX = "ISW2-DELIVERABLE-2";
		File parent = new File(Files.createTempDirectory(TMP_PREFIX).toAbsolutePath().toString());

		String command = String.format(CLONE_CMD, this.projectName.toLowerCase());
		this.execAndSync(parent, command);
		SimpleLogger.logInfo("Cloned {0} (URL: {1}, Output directory: {2}) ",
				new Object[] { projectName, command.split(" ")[2], this.pwd });

		this.pwd = new File(new StringBuilder(parent.getAbsolutePath()).append("/")
				.append(this.projectName.toLowerCase()).toString());
	}

	public void setupTargetRevision(LocalDateTime targetDate) throws IOException, InterruptedException {
		Process p = this.exec(GET_SHA_AND_DATE_CMD);
		BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
		DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		String line = null;
		String sha = null;

		while ((line = output.readLine()) != null) {
			String[] parts = line.split(" ");
			sha = parts[0];
			String dateStr = parts[1];

			LocalDateTime date = LocalDateTime.parse(dateStr, fmt);
			if (date.isAfter(targetDate)) {
				SimpleLogger.logInfo("Commit [SHA: \"{0}\" date: {1}]; Target date: {2} => Target revision found",
						new Object[] { sha, dateStr, targetDate });
				p.destroy();
				break;
			}
		}

		String checkoutCommand = String.format(CHECKOUT_CMD, sha);
		this.execAndSync(checkoutCommand);
		SimpleLogger.logInfo("Checkout at target version (SHA: {0})", sha);
	}

	private Process exec(File pwd, String command) throws IOException {
		SimpleLogger.logInfo("Executing \"{0}\"... (Current PWD: {1})",
				new Object[] { command, pwd.getAbsolutePath() });
		ProcessBuilder pb = new ProcessBuilder(SHELL, COMMAND_FLAG, command);
		pb.directory(pwd);

		return pb.start();
	}

	private void execAndSync(File pwd, String command) throws IOException, InterruptedException {
		Process p = this.exec(pwd, command);
		p.waitFor();
	}

	private Process exec(String command) throws IOException {
		return exec(this.pwd, command);
	}

	private void execAndSync(String command) throws IOException, InterruptedException {
		execAndSync(this.pwd, command);
	}

	public List<String> recuresivelyGetFileNames() throws IOException {
		List<String> filenames = new ArrayList<>();

		try (Stream<Path> stream = Files.walk(Paths.get(this.pwd.getAbsolutePath()))) {
			stream.filter(Files::isRegularFile).forEach(element -> {
				String filename = element.toAbsolutePath().toString();
				if (filename.endsWith(".java"))
					filenames.add(filename);
			});
		}

		SimpleLogger.logInfo("Found {0} java files", filenames.size());
		return filenames;
	}

	public void removeLocalRepository() throws IOException {
		
		try (Stream<Path> stream = Files.walk(Paths.get(pwd.getParentFile().getAbsolutePath()))) {
			stream.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
		
		SimpleLogger.logInfo("Deleted working directory");

	}
}
