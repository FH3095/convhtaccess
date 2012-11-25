package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.LinkedList;
import java.util.List;

abstract public class DataHandler {
	private final String commands[];

	public DataHandler(final String... commands) {
		List<String> commandsList = new LinkedList<String>();
		for (String command : commands) {
			commandsList.add(command.toLowerCase());
		}
		this.commands = commandsList.toArray(new String[1]);
	}

	public boolean canParseLine(String line) {
		line = line.toLowerCase().trim();
		for (String command : commands) {
			if (line.startsWith(command)) {
				return true;
			}
		}
		return false;
	}

	public abstract void parseCommand(final String line) throws ParseException;
	public abstract void write(final StringBuffer buf, final int nestedLevel);

	static protected enum TriBool {
		NONE, TRUE, FALSE;
	}

	static public class ParseException extends Exception {
		private static final long serialVersionUID = -7276965014282474777L;
		public ParseException() {
			super();
		}
		public ParseException(final String msg) {
			super(msg);
		}
	}
}
