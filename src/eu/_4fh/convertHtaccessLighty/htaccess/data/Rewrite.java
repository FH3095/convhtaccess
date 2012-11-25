package eu._4fh.convertHtaccessLighty.htaccess.data;

public class Rewrite extends DataHandler {
	public Rewrite() {
		super("RewriteEngine", "RewriteBase", "RewriteRule");
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
