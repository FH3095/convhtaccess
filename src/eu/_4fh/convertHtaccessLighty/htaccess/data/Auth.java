package eu._4fh.convertHtaccessLighty.htaccess.data;

import eu._4fh.convertHtaccessLighty.Main;

public class Auth extends DataHandler {
	private String authType;
	private String authName;
	private String authUserFile;
	private String authGroupFile;
	private String authRequire;

	public Auth() {
		super("AuthType", "AuthName", "AuthUserfile", "AuthGroupFile",
				"require");
		authType = authName = authUserFile = authGroupFile = authRequire = null;
	}

	@Override
	public void parseCommand(String line) throws ParseException {
		String lLine = line.toLowerCase().trim();
		if (lLine.startsWith("authtype")) {
			authType = removeQuotes(line.substring("authtype".length()).trim());
			if (!authType.equalsIgnoreCase("digest")
					&& !authType.equalsIgnoreCase("basic")) {
				throw new ParseException("Unknown AuthType " + authType);
			}
		} else if (lLine.startsWith("authname")) {
			authName = removeQuotes(line.substring("authname".length()).trim());
		} else if (lLine.startsWith("authuserfile")) {
			authUserFile = removeQuotes(line.substring("authuserfile".length())
					.trim());
		} else if (lLine.startsWith("authgroupfile")) {
			authGroupFile = removeQuotes(line.substring(
					"authgroupfile".length()).trim());
		} else if (lLine.startsWith("require")) {
			authRequire = extractRequire(line);
		} else {
			throw new ParseException("Not yet implemented: " + line);
		}
	}

	private String extractRequire(String line) throws ParseException {
		String parts[] = line.split("\\s+");
		boolean firstPart = true;
		StringBuffer ret = new StringBuffer();

		for (int i = 1; i < parts.length; ++i) {
			if (parts[i].isEmpty()) {
				continue;
			}
			parts[i] = removeQuotes(parts[i].trim()).trim();
			if (firstPart) {
				firstPart = false;
				if (parts[i].equalsIgnoreCase("group")) {
					throw new ParseException(
							"Lighty doesn't implement \"require group\".");
				} else if (parts[i].equalsIgnoreCase("valid-user")) {
					return "valid-user";
				} else if (!parts[i].equalsIgnoreCase("user")) {
					throw new ParseException("Unknown require-type " + parts[i]);
				}
			} else {
				if (ret.length() > 0) {
					ret.append("|");
				}
				ret.append("user=").append(parts[i]);
			}
		}
		return ret.toString();
	}

	@Override
	public void write(StringBuffer buf, int nestedLevel) {
		if (authType != null) {
			if (authType.equals("basic")) {
				Main.writeIndentLine(buf, nestedLevel,
						"auth.backend = \"htpasswd\"");
				Main.writeIndentLine(buf, nestedLevel,
						"auth.backend.htpasswd.userfile = \"", authUserFile,
						"\"");

			} else {
				Main.writeIndentLine(buf, nestedLevel,
						"auth.backend = \"htdigest\"");
				Main.writeIndentLine(buf, nestedLevel,
						"auth.backend.htdigest.userfile = \"", authUserFile,
						"\"");
			}
			if (authGroupFile != null) {
				Main.writeIndentLine(buf, nestedLevel,
						"# groupfile is not yet implemented.");
				Main.writeIndentLine(buf, nestedLevel, "#",
						"auth.backend.plain.groupfile => \"", authGroupFile,
						"\"");
			}
			Main.writeIndentLine(buf, nestedLevel, "auth.require = ( \"\" =>");
			Main.writeIndentLine(buf, nestedLevel + 1, "(");
			Main.writeIndentLine(buf, nestedLevel + 2, "\"method\" => \"",
					authType, "\",");
			Main.writeIndentLine(buf, nestedLevel + 2, "\"realm\" => \"",
					authName, "\",");
			Main.writeIndentLine(buf, nestedLevel + 2, "\"require\" => \"",
					authRequire, "\",");
			Main.writeIndentLine(buf, nestedLevel + 1, ")");
			Main.writeIndentLine(buf, nestedLevel, ")");
		}
	}
}
