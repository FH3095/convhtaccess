package eu._4fh.convertHtaccessLighty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ListIterator;

import eu._4fh.convertHtaccessLighty.config.Config;
import eu._4fh.convertHtaccessLighty.config.options.DocRoot;
import eu._4fh.convertHtaccessLighty.config.options.Domain;
import eu._4fh.convertHtaccessLighty.config.options.DomainOption;
import eu._4fh.convertHtaccessLighty.config.options.Redirect;
import eu._4fh.convertHtaccessLighty.htaccess.HtAccessTreeParser;

public class Main {
	private Config config;
	static final public String nl = "\n";
	static final public String indent = "\t";

	static public void writeIndentLine(final StringBuffer buf,
			final int nestedLevel, final String... content) {
		for (int i = 0; i < nestedLevel; ++i) {
			buf.append(indent);
		}
		for (int i = 0; i < content.length; ++i) {
			buf.append(content[i]);
		}
		buf.append(nl);
	}

	static public String quoteRegexString(String str) {
		return "\\Q" + str.replace("\\", "\\\\") + "\\E";
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

			File file = new File(domain.filePrefix
					+ domainIndexFormater.format(domain.index) + "-"
					+ domain.name + domain.filePostfix);
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(data);
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException("Can't write file "
						+ file.getAbsolutePath(), e);
			}
		}
	}

	protected String createDomain(Domain domain) {
		StringBuffer buf = new StringBuffer();

		createDomainCondition(buf, domain);
		writeIndentLine(buf, 1, domain.textPrefix);

		ListIterator<DomainOption> options = domain.domainOptions
				.listIterator();
		while (options.hasNext()) {
			DomainOption option = options.next();
			createDomainOption(buf, option);
			if (option instanceof DocRoot) {
				String docRoot = ((DocRoot) option).docRoot;
				new HtAccessTreeParser(buf, new File(docRoot),
						config.getInActiveModules(), config.getActiveModules())
						.parse();
			}
		}

		writeIndentLine(buf, 1, domain.textPostfix);
		writeIndentLine(buf, 0, "}");

		return buf.toString();
	}

	protected void createDomainCondition(StringBuffer buf, Domain domain) {
		String regex = "";
		switch (domain.regexType) {
		case NONE:
			regex = "= \"" + domain.name + "\"";
			break;
		case WITH_PORT:
			regex = "~ \"^" + quoteRegexString(domain.name) + "(\\:[0-9]+)?$\"";
			break;
		default:
			throw new RuntimeException("Got invalid regexType: "
					+ domain.regexType);
		}
		writeIndentLine(buf, 0, "$HTTP[\"host\"] =", regex, " {");
	}

	protected void createDomainOption(StringBuffer buf, DomainOption option) {
		if (option instanceof DocRoot) {
			writeIndentLine(buf, 1, "server.document-root = \"",
					((DocRoot) option).docRoot, "\"");
		} else if (option instanceof Redirect) {
			Redirect redirect = (Redirect) option;
			writeIndentLine(buf, 1, "url.redirect = ( \"/(.*)\" => \"",
					redirect.redirectTo,
					(redirect.redirectWithPath ? "$1" : ""), "\" )");
			writeIndentLine(buf, 1, "url.redirect-code = ",
					Short.toString(redirect.redirectCode));
		} else {
			throw new RuntimeException("Can't handle DomainOption of type "
					+ option.getClass().getCanonicalName());
		}
	}

	public static void main(String[] args) {
		new Main().run(args);
	}
}
