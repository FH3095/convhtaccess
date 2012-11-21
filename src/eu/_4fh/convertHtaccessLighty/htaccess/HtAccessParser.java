package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HtAccessParser {
	private StringBuffer buf;
	private File file;

	public HtAccessParser() {
		buf = new StringBuffer();
	}

	public void parseFile(File file) {
		this.file = file;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			while (line != null) {
				line = reader.readLine();
				parseLine(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Can't find file "
					+ file.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RuntimeException("Error while reading file "
					+ file.getAbsolutePath(), e);
		}
	}

	protected void parseLine(final String line) {
		String lineBuf = line.trim().toLowerCase();
		if (lineBuf.startsWith("#")) {
			parseComment(line);
		} else if (lineBuf.startsWith("options")) {
			parseOptions(line);
		}
	}

	protected void parseComment(String line) {
		buf.append(line);
		buf.append("\n");
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
				buf.append("dir-listing.activate = \"");
				if (start == '+' || start == ' ') {
					buf.append("enable\"");
				} else {
					buf.append("disable\"");
				}
				buf.append("\n");
			} else {
				System.err.println(formatError("Unknown option " + parts[i],
						line));
			}
		}
	}
	protected String formatError(String error, String line) {
		return "Error while parsing file " + file.getAbsolutePath()
				+ " with line \'" + line + "\': " + error;
	}
}
