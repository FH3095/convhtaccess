package eu._4fh.convertHtaccessLighty.config.options;

import java.util.ArrayList;
import java.util.Arrays;

public class Domain {
	final public String name;
	final public short index;
	final public ArrayList<DomainOption> domainOptions;
	final public String filePrefix;
	final public String filePostfix;
	final public String textPrefix;
	final public String textPostfix;

	public Domain(String name, final short index, final String filePrefix, final String filePostfix, String textPrefix,
			String textPostfix, DomainOption... options) {
		this.name = name;
		this.index = index;
		this.filePrefix = filePrefix;
		this.filePostfix = filePostfix;
		this.textPrefix = textPrefix;
		this.textPostfix = textPostfix;
		this.domainOptions = new ArrayList<DomainOption>(Arrays.asList(options));
	}
}
