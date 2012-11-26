package eu._4fh.convertHtaccessLighty.htaccess.data;

public class Auth extends DataHandler {
	public Auth() {
		super("AuthType", "AuthName", "AuthUserfile", "require");
	}

	@Override
	public void parseCommand(String line) throws ParseException {
		// TODO Auto-generated method stub
		throw new ParseException("Not yet implemented: " + line);
	}

	@Override
	public void write(StringBuffer buf, int nestedLevel) {
		// TODO Auto-generated method stub
	}
}
