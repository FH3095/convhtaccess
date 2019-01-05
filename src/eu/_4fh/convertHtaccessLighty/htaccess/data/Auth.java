package eu._4fh.convertHtaccessLighty.htaccess.data;

import eu._4fh.convertHtaccessLighty.Main;

public class Auth extends DataHandler {
	private String authType;
	private String authName;
	private String authUserFile;

	public Auth() {
		super("AuthType", "AuthName", "AuthUserfile", "require");
		authType = authName = authUserFile = null;
	}

	@Override
	public void parseCommand(final String line) throws ParseException {
		final String command = getCommandFromLine(line);
		switch (command) {
		case "authtype":
			authType = getCommandParameter(line, "authtype").toLowerCase();
			if (!authType.equals("basic") && !authType.equals("digest")) {
				throw new RuntimeException("Can only handle authtype basic. nginx cant handle digest. " + line);
			}
			break;
		case "authname":
			authName = getCommandParameter(line, "authname");
			break;
		case "authuserfile":
			authUserFile = getCommandParameter(line, "authuserfile");
			break;
		case "require":
			final String param = getCommandParameter(line, "require");
			if (!param.equalsIgnoreCase("valid-user")) {
				throw new RuntimeException("Can only use require valid-user. " + line);
			}
		default:
			throw new ParseException("Not yet implemented: " + line);
		}
	}

	public void write(StringBuffer buf, int nestedLevel) {
		if (authType != null) {
			if (authType.equals("basic")) {
				Main.writeIndentLine(buf, nestedLevel, "auth.backend = \"htpasswd\"");
				Main.writeIndentLine(buf, nestedLevel, "auth.backend.htpasswd.userfile = \"", authUserFile, "\"");

			} else {
				Main.writeIndentLine(buf, nestedLevel, "auth.backend = \"htdigest\"");
				Main.writeIndentLine(buf, nestedLevel, "auth.backend.htdigest.userfile = \"", authUserFile, "\"");
			}
			Main.writeIndentLine(buf, nestedLevel, "auth.require = ( \"\" =>");
			Main.writeIndentLine(buf, nestedLevel + 1, "(");
			Main.writeIndentLine(buf, nestedLevel + 2, "\"method\" => \"", authType, "\",");
			Main.writeIndentLine(buf, nestedLevel + 2, "\"realm\" => \"", authName, "\",");
			Main.writeIndentLine(buf, nestedLevel + 2, "\"require\" => \"valid-user\",");
			Main.writeIndentLine(buf, nestedLevel + 1, ")");
			Main.writeIndentLine(buf, nestedLevel, ")");
		}
	}
}
