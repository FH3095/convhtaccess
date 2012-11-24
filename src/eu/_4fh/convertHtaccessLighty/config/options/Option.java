package eu._4fh.convertHtaccessLighty.config.options;

public class Option extends ConfigNode {
	public final RegexType regexType;
	public final String prefix;
	public final String postfix;

	public Option(final RegexType regexType,final String prefix,final String postfix) {
		this.regexType = regexType;
		this.prefix=prefix;
		this.postfix=postfix;
	}
}
