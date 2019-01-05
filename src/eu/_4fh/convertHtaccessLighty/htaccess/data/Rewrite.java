package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class Rewrite extends DataHandler {
	private Boolean engineOn;
	private String base;
	private List<RulePair> rules;
	private boolean ignoreNextRewriteRule;

	public Rewrite() {
		super("RewriteEngine", "RewriteBase", "RewriteCond", "RewriteRule");
		engineOn = null;
		base = null;
		rules = new LinkedList<RulePair>();
		ignoreNextRewriteRule = false;
	}

	@Override
	public void parseCommand(final String line) throws ParseException {
		final String command = getCommandFromLine(line);
		switch (command) {
		case "rewriteengine":
			engineOn = parseBoolean(getCommandParameter(line, "rewriteengine"));
			break;
		case "rewritebase":
			base = Pattern.quote(getCommandParameter(line, "rewritebase"));
			break;
		case "rewritecond":
			ignoreNextRewriteRule = true;
			throw new ParseException("Found RewriteCond \"" + line
					+ "\". RewriteCond not implemented, so this RewriteCond AND the next RewriteRule are ignored.");
		case "rewriterule":
			if (base == null) {
				throw new ParseException("RewriteRule without RewriteBase is not allowed.");
			}
			if (ignoreNextRewriteRule) {
				ignoreNextRewriteRule = false;
				throw new ParseException("Ignored \"" + line + "\" because of previous RewriteCond.");
			}
			final List<String> parts = getCommandParameters(line, "rewriterule");
			final String rule = parts.get(0);
			final String target = parts.get(1);
			if (target.equals("-")) {
				throw new ParseException("Ignored \"" + line + "\" because you can't implement this in lighty-config.");
			}
			rules.add(new RulePair(rule, target));
			break;
		default:
			throw new ParseException("Unknown Rewrite-Command " + line);
		}
	}

	public void write(StringBuffer buf, int nestedLevel) {
		if (Boolean.TRUE.equals(engineOn)) {
			Iterator<RulePair> it = rules.iterator();
			while (it.hasNext()) {
				RulePair pair = it.next();
				String rule = pair.rule;
				String dest = pair.dest;
				if (rule.startsWith("^")) {
					rule = "^" + base + rule.substring(1);
				}
				Main.writeIndentLine(buf, nestedLevel, "url.redirect = ( \"", rule, "\" => \"", dest, "\" )");
			}
		}
	}

	static private class RulePair {
		public RulePair(final String rule, final String dest) {
			this.rule = rule;
			this.dest = dest;
		}

		public final String rule;
		public final String dest;
	}
}
