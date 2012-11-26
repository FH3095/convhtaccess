package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import eu._4fh.convertHtaccessLighty.Main;
import eu._4fh.convertHtaccessLighty.htaccess.data.Auth;
import eu._4fh.convertHtaccessLighty.htaccess.data.DataHandler;
import eu._4fh.convertHtaccessLighty.htaccess.data.Expire;
import eu._4fh.convertHtaccessLighty.htaccess.data.Options;
import eu._4fh.convertHtaccessLighty.htaccess.data.OrderAllowDeny;
import eu._4fh.convertHtaccessLighty.htaccess.data.Rewrite;

public class HtAccessDataParser {
	private final StringBuffer buf;
	private final String data;
	private final int nestedLevel;
	private final File src;
	private final DataHandler handler[];

	protected HtAccessDataParser(final StringBuffer buf, final String data,
			final int nestedLevel, final File src) {
		this.buf = buf;
		this.data = data;
		this.nestedLevel = nestedLevel;
		this.src = src;
		List<DataHandler> handlerList = new LinkedList<DataHandler>();
		handlerList.add(new OrderAllowDeny());
		handlerList.add(new Rewrite());
		handlerList.add(new Options());
		handlerList.add(new Auth());
		handlerList.add(new Expire());
		handler = handlerList.toArray(new DataHandler[1]);
	}

	public void parse() {
		String lines[] = data.split(Main.nl);
		for (int i = 0; i < lines.length; ++i) {
			parseLine(lines[i]);
		}
		for (DataHandler handler : this.handler) {
			handler.write(buf, nestedLevel);
		}
	}

	protected void parseLine(final String line) {
		String lineBuf = line.trim().toLowerCase();
		DataHandler handler = findDataHandler(line);
		if (handler != null) {
			try {
				handler.parseCommand(line);
			} catch (DataHandler.ParseException e) {
				System.out.println(e.getLocalizedMessage());
			}
		} else if (lineBuf.startsWith("#")) {
			parseComment(line);
		} else if (lineBuf.isEmpty()) {
			// Ignore
		} else {
			System.out.println(formatError("Unknown command", line));
		}
	}

	private DataHandler findDataHandler(final String line) {
		for (DataHandler handler : this.handler) {
			if (handler.canParseLine(line)) {
				return handler;
			}
		}
		return null;
	}

	private void parseComment(final String line) {
		Main.writeIndentLine(buf, nestedLevel, line);
	}

	protected String formatError(String error, String line) {
		return "Error while parsing file " + src.getAbsolutePath() + " line \'"
				+ line + "\': " + error;
	}
}
