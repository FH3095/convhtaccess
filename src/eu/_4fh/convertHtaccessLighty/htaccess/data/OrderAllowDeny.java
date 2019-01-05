package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class OrderAllowDeny extends DataHandler {
	private static final Pattern DENY_FROM_ALL = Pattern.compile("^\\s*deny\\s+from\\s+all\\s*$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern ORDER_ALLOW_DENY = Pattern.compile("^\\s*order\\s+allow\\s*,\\s*deny\\s*$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern ORDER_DENY_ALLOW = Pattern.compile("^\\s*order\\s+deny\\s*,\\s*allow\\s*$",
			Pattern.CASE_INSENSITIVE);
	private boolean orderDenyLast;
	private boolean denyAll;

	public OrderAllowDeny() {
		super("order", "allow", "deny");
		orderDenyLast = false;
		denyAll = false;
	}

	private boolean checkOrderIsDenyLast(final String line) throws ParseException {
		final Matcher matcherAllowDeny = ORDER_ALLOW_DENY.matcher(line);
		final Matcher matcherDenyAllow = ORDER_DENY_ALLOW.matcher(line);
		if (matcherAllowDeny.matches()) {
			return true;
		} else if (matcherDenyAllow.matches()) {
			return false;
		} else {
			throw new ParseException("Found invalid Order-Command " + line);
		}
	}

	@Override
	public void parseCommand(final String line) throws ParseException {
		final String command = getCommandFromLine(line);
		switch (command) {
		case "order":
			orderDenyLast = checkOrderIsDenyLast(line);
			break;
		case "allow":
			throw new ParseException(
					"Found Allow-Command, but lighty only knows Deny-Commands, so there is no way to implement Allow-Commands.");
		case "deny":
			if (!DENY_FROM_ALL.matcher(line).matches()) {
				throw new ParseException("Currently only \"deny from all\" is implemented. " + line);
			}
			denyAll = true;
			break;
		default:
			throw new ParseException("Not yet implemented: " + line);
		}
	}

	public void write(StringBuffer buf, int nestedLevel) {
		if (denyAll || orderDenyLast) {
			Main.writeIndentLine(buf, nestedLevel, "url.access-deny = (\"\")");
		}
	}
}
