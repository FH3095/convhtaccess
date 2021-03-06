package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class LighttpdOption extends DataHandler {
	private final List<String> out;
	private static final Pattern replacePattern = Pattern.compile("^##(Lighttpd|Lighty):",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	public LighttpdOption() {
		super("##Lighttpd:", "##Lighty:");
		out = new LinkedList<String>();
	}

	@Override
	public void parseCommand(String line) throws ParseException {
		out.add(replacePattern.matcher(line).replaceFirst("").trim());
	}

	public void write(StringBuffer buf, int nestedLevel) {
		Iterator<String> it = out.iterator();
		while (it.hasNext()) {
			String cur = it.next();
			Main.writeIndentLine(buf, nestedLevel, cur);
		}
	}
}
