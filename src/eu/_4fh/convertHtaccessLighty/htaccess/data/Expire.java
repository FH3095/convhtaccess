package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class Expire extends DataHandler {
	private Boolean expiresActive;
	private boolean expireAccess;
	private long expireTime;

	public Expire() {
		super("ExpiresActive", "ExpiresByType", "ExpiresDefault");
		expiresActive = null;
		expireAccess = false;
		expireTime = -1;
	}

	@Override
	public void parseCommand(String line) throws ParseException {
		line = line.toLowerCase().trim();
		if (line.startsWith("expiresactive")) {
			String onOff = removeQuotes(line
					.substring("expiresactive".length()).trim().toLowerCase());
			if (onOff.equals("on")) {
				expiresActive = Boolean.TRUE;
			} else if (onOff.equals("off")) {
				expiresActive = Boolean.FALSE;
			} else {
				throw new ParseException("Invalid On/Off-value in " + line);
			}
		} else if (line.startsWith("expiresbytype")) {
			String parts[] = line.split("\\s+");
			System.out
					.println("WARNING: ExpiresByType "
							+ line
							+ " isn't possible in lighty. I will interpret it like ExpiresDefault.");
			calcExpiresValue(removeQuotes(parts[2].trim()));
		} else if (line.startsWith("expiresdefault")) {
			calcExpiresValue(removeQuotes(line.substring(
					"expiresdefault".length()).trim()));
		} else {
			throw new ParseException("Invalid Expire-Definition " + line);
		}
	}

	private void calcExpiresValue(String string) throws ParseException {
		Pattern p = Pattern.compile("^[MA]\\d+$", Pattern.CASE_INSENSITIVE);
		if (!p.matcher(string).matches()) {
			throw new ParseException(
					"Expire-Definition \""
							+ string
							+ "\" is currently not supported. The only supported syntax is <M|A><seconds>");
		}

		String expiresBase = string.substring(0, 1);
		if (expiresBase.equalsIgnoreCase("A")) {
			expireAccess = true;
		}

		long expireTime = Long.valueOf(string.substring(1));
		if (this.expireTime > -1) {
			ParseException e = new ParseException(
					"An Expire-Definition ("
							+ (expireAccess ? "A" : "M")
							+ String.valueOf(expireTime)
							+ ") is already set while processing "
							+ string
							+ ". I will use the smallest values of this and the previous definition. (Access < Modified)");

			if (this.expireTime > expireTime) {
				this.expireTime = expireTime;
			}
			throw e;
		} else {
			this.expireTime = expireTime;
		}
	}

	@Override
	public void write(StringBuffer buf, int nestedLevel) {
		if (Boolean.TRUE.equals(expiresActive)) {
			Main.writeIndentLine(buf, nestedLevel, "expire.url = ( \"\" => \"",
					expireAccess ? "access" : "modification", " plus ",
					String.valueOf(expireTime), " seconds\" )");
		}
	}
}
