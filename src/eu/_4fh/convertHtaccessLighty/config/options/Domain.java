package eu._4fh.convertHtaccessLighty.config.options;

import java.util.ArrayList;
import java.util.Arrays;

public class Domain extends ConfigNode {
	final public String name;
	final public RegexType regexType;
	final public ArrayList<DomainOption> domainOptions;

	public Domain(String name, RegexType regexType, DomainOption... options) {
		this.name = name;
		this.regexType = regexType;
		this.domainOptions = new ArrayList<DomainOption>(Arrays.asList(options));
	}
}
