package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

class HtAccessDataParser {
	private final StringBuffer buf;
	private final String data;
	private final int nestedLevel;
	private final File src;

	protected HtAccessDataParser(final StringBuffer buf, final String data,
			final int nestedLevel, final File src) {
		this.buf = buf;
		this.data = data;
		this.nestedLevel = nestedLevel;
		this.src = src;
	}

	public void parse() {
		String lines[] = data.split(Main.nl);
		for (int i = 0; i < lines.length; ++i) {
			parseLine(lines[i]);
		}
	}

	protected void parseLine(final String line) {
		String lineBuf = line.trim().toLowerCase();
		if (lineBuf.startsWith("#")) {
			parseComment(line);
		} else if (lineBuf.startsWith("options")) {
			parseOptions(line);
		} else if (lineBuf.isEmpty()) {
			// Ignore
		} else if (lineBuf.startsWith("allow")) {
			parseAllow(line);
		} else if (lineBuf.startsWith("deny")) {
			parseDeny(line);
		} else if (lineBuf.startsWith("order")) {
			parseOrder(line);
		} else {
			System.out.println(formatError("Unknown command", line));
		}
	}

	private void parseOrder(String line) {
		System.out
				.println(formatError(
						"Found Order-Command, but with lighty there is no way to use Allow-Commands. So Order-Commands are senseless and therefor ignored.",
						line));
	}

	private void parseAllow(String line) {
		System.out
				.println(formatError(
						"Found Allow-Command, but lighty only knows Deny-Commands, so there is no way to implement Allow-Commands.",
						line));
	}

	private void parseDeny(String line) {
		Matcher m = Pattern.compile("^deny\\s+from\\s+all$",
				Pattern.CASE_INSENSITIVE).matcher(line);
		if (!m.matches()) {
			System.out.println(formatError(
					"Currently only \"deny from all\" is implemented.", line));
			return;
		}
		Main.writeIndentLine(buf, nestedLevel, "url.access-deny = (\"\")");
	}

	private void parseComment(final String line) {
		Main.writeIndentLine(buf, nestedLevel, line);
	}

	protected void parseOptions(String line) {
		String[] parts = line.toLowerCase().replace("  ", " ").split(" ", 0);
		for (int i = 1; i < parts.length; ++i) {
			char start = ' ';
			if (parts[i].startsWith("+")) {
				start = '+';
			} else if (parts[i].startsWith("-")) {
				start = '-';
			}
			if (start != ' ') {
				parts[i] = parts[i].substring(1);
			}

			if (parts[i].equals("none")) {
				// Ignore
			} else if (parts[i].equals("execcgi")) {
				// Ignore
			} else if (parts[i].equals("followsymlinks")) {
				// Ignore
			} else if (parts[i].equals("indexes")) {
				Main.writeIndentLine(buf, nestedLevel,
						"dir-listing.activate = \"", (start == '+'
								|| start == ' ' ? "enable\"" : "disable\""));
			} else {
				System.out.println(formatError("Unknown option " + parts[i],
						line));
			}
		}
	}

	protected String formatError(String error, String line) {
		return "Error while parsing file " + src.getAbsolutePath() + " line \'"
				+ line + "\': " + error;
	}
}
