package eu._4fh.convertHtaccessLighty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ListIterator;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.config.Config;
import eu._4fh.convertHtaccessLighty.config.options.DocRoot;
import eu._4fh.convertHtaccessLighty.config.options.Domain;
import eu._4fh.convertHtaccessLighty.config.options.DomainOption;
import eu._4fh.convertHtaccessLighty.config.options.Redirect;
import eu._4fh.convertHtaccessLighty.htaccess.TreeScanner;
import eu._4fh.convertHtaccessLighty.htaccess.data.DataHandler;

public class Main {
	static final public String nl = "\n";
	static final public String indent = "\t";
	static final private Main instance = new Main();
	private final Config config;

	static public void writeIndentLine(final StringBuffer buf, final int nestedLevel, final String... content) {
		for (int i = 0; i < nestedLevel; ++i) {
			buf.append(indent);
		}
		for (int i = 0; i < content.length; ++i) {
			buf.append(content[i]);
		}
		buf.append(nl);
	}

	static public Config getConfig() {
		return instance.config;
	}

	private Main() {
		config = new Config();
	}

	public void run(String[] args) {
		config.readConfig(new File("config.xml"));
		ListIterator<Domain> domains = config.getDomains();
		DecimalFormat domainIndexFormater = new DecimalFormat("000");

		while (domains.hasNext()) {
			Domain domain = domains.next();
			String data = createDomain(domain);

			File file = new File(domain.filePrefix + domainIndexFormater.format(domain.index) + "-" + domain.name
					+ domain.filePostfix);
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(data);
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException("Can't write file " + file.getAbsolutePath(), e);
			}
		}
	}

	protected String createDomain(Domain domain) {
		StringBuffer buf = new StringBuffer();

		createDomainCondition(buf, domain);
		writeIndentLine(buf, 0, domain.textPrefix);

		ListIterator<DomainOption> options = domain.domainOptions.listIterator();
		while (options.hasNext()) {
			DomainOption option = options.next();
			createDomainOption(buf, option);
			if (option instanceof DocRoot) {
				String docRoot = ((DocRoot) option).docRoot;
				//new HtAccessTreeParser(buf, new File(docRoot), config.getInActiveModules(), config.getActiveModules()).parse();
				new TreeScanner(docRoot).scan();
			}
		}

		writeIndentLine(buf, 0, domain.textPostfix);
		writeIndentLine(buf, 0, "}");

		return buf.toString();
	}

	protected void createDomainCondition(StringBuffer buf, Domain domain) {
		String regex = "~ \"^" + Pattern.quote(domain.name) + "(\\:[0-9]+)?$\"";
		writeIndentLine(buf, 0, "$HTTP[\"host\"] =", regex, " {");
	}

	protected void createDomainOption(StringBuffer buf, DomainOption option) {
		if (option instanceof DocRoot) {
			writeIndentLine(buf, 1, "server.document-root = \"", ((DocRoot) option).docRoot, "\"");
		} else if (option instanceof Redirect) {
			Redirect redirect = (Redirect) option;
			writeIndentLine(buf, 1, "url.redirect = ( \"/(.*)\" => \"", redirect.redirectTo,
					(redirect.redirectWithPath ? "$1" : ""), "\" )");
			writeIndentLine(buf, 1, "url.redirect-code = ", Short.toString(redirect.redirectCode));
		} else {
			throw new RuntimeException("Can't handle DomainOption of type " + option.getClass().getCanonicalName());
		}
	}

	public static final String removeQuotes(String line) throws DataHandler.ParseException {
		boolean startQuotes = line.startsWith("\"");
		boolean endQuotes = line.endsWith("\"");
		if (startQuotes != endQuotes) {
			throw new DataHandler.ParseException(
					"Tried to extract parameter from \"" + line + "\" which starts xor ends with quotes.");
		}
		if (startQuotes) {
			line = line.substring(1, line.length() - 1);
		}
		return line;
	}

	public static void main(String[] args) {
		instance.run(args);
	}
}
