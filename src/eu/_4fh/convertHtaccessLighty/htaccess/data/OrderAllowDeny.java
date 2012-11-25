package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class OrderAllowDeny extends DataHandler {
	private boolean orderDenyLast;
	private boolean denyAll;

	public OrderAllowDeny() {
		super("order", "allow", "deny");
		orderDenyLast = false;
		denyAll = false;
	}

	private void parseOrder(String line) throws ParseException {
		Matcher matcherAllowDeny = Pattern.compile(
				"^\\s+order\\s+allow\\s*,\\s*deny\\s+$",
				Pattern.CASE_INSENSITIVE).matcher(line);
		Matcher matcherDenyAllow = Pattern.compile(
				"^\\s+order\\s+allow\\s*,\\s*deny\\s+$",
				Pattern.CASE_INSENSITIVE).matcher(line);
		if (matcherAllowDeny.matches()) {
			orderDenyLast = true;
		} else if (matcherDenyAllow.matches()) {
			orderDenyLast = false;
		} else {
			throw new ParseException("Found invalid Order-Command " + line);
		}
	}

	private void parseAllow(String line) throws ParseException {
		throw new ParseException(
				"Found Allow-Command, but lighty only knows Deny-Commands, so there is no way to implement Allow-Commands.");
	}

	private void parseDeny(String line) throws ParseException {
		Matcher m = Pattern.compile("^deny\\s+from\\s+all$",
				Pattern.CASE_INSENSITIVE).matcher(line);
		if (!m.matches()) {
			throw new ParseException(
					"Currently only \"deny from all\" is implemented.");
		}
		denyAll = true;
	}

	@Override
	public void parseCommand(String line) throws ParseException {
		line = line.toLowerCase().trim();
		if (line.startsWith("order")) {
			parseOrder(line);
		} else if (line.startsWith("allow")) {
			parseAllow(line);
		} else if (line.startsWith("deny")) {
			parseDeny(line);
		} else {
			throw new RuntimeException(
					"Called OrderAllowDeny with invalid line " + line);
		}
	}

	@Override
	public void write(StringBuffer buf, int nestedLevel) {
		if (denyAll || orderDenyLast) {
			Main.writeIndentLine(buf, nestedLevel, "url.access-deny = (\"\")");
		}
	}
}
