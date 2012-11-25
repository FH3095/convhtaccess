package eu._4fh.convertHtaccessLighty.htaccess.data;

import eu._4fh.convertHtaccessLighty.Main;

public class Options extends DataHandler {
	private Boolean indexes;
	public Options() {
		super("Options");
		indexes = null;
	}

	@Override
	public void parseCommand(String line) throws ParseException {
		String[] parts = line.toLowerCase().replace("  ", " ").trim()
				.split(" ", 0);
		String error = "Unknown options: ";
		boolean errorOccured = false;
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
				indexes = Boolean.FALSE;
			} else if (parts[i].equals("indexes")) {
				if (start == '+' || start == ' ') {
					indexes = Boolean.TRUE;
				} else {
					indexes = Boolean.FALSE;
				}
			} else {
				errorOccured = true;
				error += parts[i] + ",";
			}
		}

		if (errorOccured) {
			throw new ParseException(error);
		}
	}

	@Override
	public void write(StringBuffer buf, int nestedLevel) {
		if (indexes != null) {
			Main.writeIndentLine(buf, nestedLevel, "dir-listing.activate = \"",
					(indexes.equals(Boolean.TRUE) ? "enable\"" : "disable\""));
		}
	}

	public Boolean getIndexes() {
		return indexes;
	}
}
