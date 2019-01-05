package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class Options extends DataHandler {
	private Boolean indexes;

	public Options() {
		super("Options");
		indexes = null;
	}

	@Override
	public void parseCommand(final String line) throws ParseException {
		final List<String> parts = getCommandParameters(line, "options", Pattern.compile("[^\\s,]+"));
		String error = "Unknown options: ";
		boolean errorOccured = false;
		for (final Iterator<String> it = parts.iterator(); it.hasNext();) {
			String part = it.next();
			char start = ' ';
			if (part.startsWith("+")) {
				start = '+';
			} else if (part.startsWith("-")) {
				start = '-';
			}
			if (start != ' ') {
				part = part.substring(1);
			}

			if (part.equals("none")) {
				indexes = Boolean.FALSE;
			} else if (part.equalsIgnoreCase("indexes")) {
				if (start == '+' || start == ' ') {
					indexes = Boolean.TRUE;
				} else {
					indexes = Boolean.FALSE;
				}
			} else {
				errorOccured = true;
				error += part + ",";
			}
		}

		if (errorOccured) {
			throw new ParseException(error);
		}
	}

	public void write(StringBuffer buf, int nestedLevel) {
		if (indexes != null) {
			Main.writeIndentLine(buf, nestedLevel, "dir-listing.activate = \"",
					(indexes.equals(Boolean.TRUE) ? "enable\"" : "disable\""));
		}
	}
}
