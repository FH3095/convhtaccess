package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

abstract public class DataHandler {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
	private static final Pattern NON_SPACE_PATTERN = Pattern.compile("\\S+");
	private final String commands[];

	public DataHandler(final String... commands) {
		List<String> commandsList = new LinkedList<String>();
		for (String command : commands) {
			commandsList.add(command.toLowerCase());
		}
		this.commands = commandsList.toArray(new String[1]);
	}

	public boolean canParseLine(String line) {
		line = line.trim();
		if (line.isEmpty()) {
			return false;
		}
		String searchFor;
		try {
			searchFor = getCommandFromLine(line);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		for (final String command : commands) {
			if (command.equals(searchFor)) {
				return true;
			}
		}
		return false;
	}

	static final protected String getCommandFromLine(String line) throws ParseException {
		line = line.trim();
		final Matcher matcher = NON_SPACE_PATTERN.matcher(line);
		if (!matcher.find()) {
			throw new ParseException("Cant find command in line " + line);
		}
		return matcher.group().toLowerCase();
	}

	static final protected String getCommandParameter(String line, String begin) throws ParseException {
		line = line.trim();
		begin = begin.trim();
		line = SPACE_PATTERN.matcher(line).replaceAll(" ");
		begin = SPACE_PATTERN.matcher(begin).replaceAll(" ");
		if (!line.toLowerCase().startsWith(begin.toLowerCase())) {
			throw new ParseException("Begin \'" + begin + "\' doesnt match in line " + line);
		}
		return Main.removeQuotes(line.substring(begin.length()).trim());
	}

	static final protected List<String> getCommandParameters(String line, String begin) throws ParseException {
		final List<String> result = new ArrayList<>();
		line = line.trim();
		begin = begin.trim();
		line = SPACE_PATTERN.matcher(line).replaceAll(" ");
		begin = SPACE_PATTERN.matcher(begin).replaceAll(" ");
		if (!line.toLowerCase().startsWith(begin.toLowerCase())) {
			throw new ParseException("Begin \'" + begin + "\' doesnt match in line " + line);
		}
		line = line.substring(begin.length()).trim();
		final Matcher parameters = NON_SPACE_PATTERN.matcher(line);
		while (parameters.find()) {
			final String part = parameters.group();
			if (part.startsWith("\"")) {
				String completeResult = part;
				while (parameters.find()) {
					completeResult = completeResult + " " + parameters.group();
					if (completeResult.endsWith("\"")) {
						break;
					}
				}
				if (!completeResult.endsWith("\"")) {
					throw new ParseException("Cant find ending quotation mark in line " + begin + " " + line);
				}
				result.add(completeResult);
			} else {
				result.add(parameters.group());
			}
		}
		return Collections.unmodifiableList(result);
	}

	static final protected boolean parseBoolean(final String text) throws ParseException {
		final String onOff = Main.removeQuotes(text).trim().toLowerCase();
		if (onOff.equals("on")) {
			return true;
		} else if (onOff.equals("off")) {
			return false;
		} else {
			throw new ParseException("Invalid On/Off-value in " + text);
		}
	}

	public abstract void parseCommand(final String line) throws ParseException;

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
