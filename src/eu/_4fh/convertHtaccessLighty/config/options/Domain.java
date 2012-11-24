package eu._4fh.convertHtaccessLighty.config.options;

import java.util.ArrayList;
import java.util.Arrays;

public class Domain extends ConfigNode {
	final public String name;
	final public short index;
	final public RegexType regexType;
	final public ArrayList<DomainOption> domainOptions;
	final public String filePrefix;
	final public String filePostfix;

	public Domain(String name, final short index, final String filePrefix,
			final String filePostfix, RegexType regexType,
			DomainOption... options) {
		this.name = name;
		this.index = index;
		this.filePrefix = filePrefix;
		this.filePostfix = filePostfix;
		this.regexType = regexType;
		this.domainOptions = new ArrayList<DomainOption>(Arrays.asList(options));
	}
}
