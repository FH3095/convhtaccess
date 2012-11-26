package eu._4fh.convertHtaccessLighty.htaccess.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
	public void parseCommand(String line) throws ParseException {
		line = line.trim();
		String lLine = line.toLowerCase();
		if (lLine.startsWith("rewriteengine")) {
			String onOff = removeQuotes(line
					.substring("rewriteengine".length()).trim());
			if (onOff.equalsIgnoreCase("on")) {
				engineOn = Boolean.TRUE;
			} else if (onOff.equalsIgnoreCase("off")) {
				engineOn = Boolean.FALSE;
			} else {
				throw new ParseException(
						"Unknown Parameter for RewriteEngine: " + onOff);
			}
		} else if (lLine.startsWith("rewritebase")) {
			this.base = Main.quoteRegexString(removeQuotes(line.substring(
					"rewritebase".length()).trim()));
		} else if (lLine.startsWith("rewriterule")) {
			if (base == null) {
				throw new ParseException(
						"RewriteRule without RewriteBase is not allowed.");
			}
			if (ignoreNextRewriteRule) {
				ignoreNextRewriteRule = false;
				throw new ParseException("Ignored \"" + line
						+ "\" because of previous RewriteCond.");
			}
			String parts[] = line.split("\\s+");
			String rule = removeQuotes(parts[1].trim());
			String target = removeQuotes(parts[2].trim());
			rules.add(new RulePair(rule, target));
		} else if (lLine.startsWith("rewritecond")) {
			ignoreNextRewriteRule = true;
			throw new ParseException(
					"Found RewriteCond \""
							+ line
							+ "\". Lighty doesn't implement RewriteCond, so this RewriteCond AND the next RewriteRule are ignored.");
		} else {
			throw new ParseException("Unknown Rewrite-Command " + line);
		}
	}

	@Override
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
				Main.writeIndentLine(buf, nestedLevel, "url.redirect = ( \"",
						rule, "\" => \"", dest, "\" )");
			}
		}
	}

	static private class RulePair {
		public RulePair(String rule, String dest) {
			this.rule = rule;
			this.dest = dest;
		}
		public String rule;
		public String dest;
	}
}
