package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class Expire extends DataHandler {
	private static final Pattern EXPIRES_PATTERN = Pattern.compile("^[MA]\\d+$", Pattern.CASE_INSENSITIVE);
	private Boolean expiresActive;
	private boolean expireAccess;
	private long expireTime;

	public Expire() {
		super("ExpiresActive", "ExpiresDefault");
		expiresActive = null;
		expireAccess = false;
		expireTime = -1;
	}

	@Override
	public void parseCommand(final String line) throws ParseException {
		final String command = getCommandFromLine(line);
		switch (command) {
		case "expiresactive":
			expiresActive = parseBoolean(getCommandParameter(line, "expiresactive"));
			break;
		case "expiresdefault":
			calcExpiresValue(getCommandParameter(line, "expiresdefault"));
			break;
		default:
			throw new ParseException("Not yet implemented: " + line);
		}
	}

	private void calcExpiresValue(final String string) throws ParseException {
		if (!EXPIRES_PATTERN.matcher(string).matches()) {
			throw new ParseException("Expire-Definition \"" + string
					+ "\" is currently not supported. The only supported syntax is <M|A><seconds>");
		}

		final String expiresBase = string.substring(0, 1);
		if (expiresBase.equalsIgnoreCase("A")) {
			expireAccess = true;
		}

		final long expireTime = Long.valueOf(string.substring(1));
		if (this.expireTime > -1) {
			ParseException e = new ParseException("An Expire-Definition (" + (expireAccess ? "A" : "M")
					+ String.valueOf(expireTime) + ") is already set while processing " + string
					+ ". I will use the smallest values of this and the previous definition. (Access < Modified)");

			if (this.expireTime > expireTime) {
				this.expireTime = expireTime;
			}
			throw e;
		} else {
			this.expireTime = expireTime;
		}
	}

	public void write(StringBuffer buf, int nestedLevel) {
		if (Boolean.TRUE.equals(expiresActive)) {
			Main.writeIndentLine(buf, nestedLevel, "expire.url = ( \"\" => \"",
					expireAccess ? "access" : "modification", " plus ", String.valueOf(expireTime), " seconds\" )");
		}
	}
}
