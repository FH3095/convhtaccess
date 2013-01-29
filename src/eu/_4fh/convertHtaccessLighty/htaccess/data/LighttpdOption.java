package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class LighttpdOption extends DataHandler {
	List<String> out;
	Pattern replacePattern;

	public LighttpdOption() {
		super("##Lighttpd:", "##Lighty:");
		out = new LinkedList<String>();
		replacePattern = Pattern.compile("^##(Lighttpd|Lighty):",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}

	@Override
	public void parseCommand(String line) throws ParseException {
		out.add(replacePattern.matcher(line).replaceFirst("").trim());
	}

	@Override
	public void write(StringBuffer buf, int nestedLevel) {
		Iterator<String> it = out.iterator();
		while (it.hasNext()) {
			String cur = it.next();
			Main.writeIndentLine(buf, nestedLevel, cur);
		}
	}
}
