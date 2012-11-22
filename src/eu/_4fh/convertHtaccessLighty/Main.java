package eu._4fh.convertHtaccessLighty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ListIterator;

import eu._4fh.convertHtaccessLighty.config.Config;
import eu._4fh.convertHtaccessLighty.config.options.DocRoot;
import eu._4fh.convertHtaccessLighty.config.options.Domain;
import eu._4fh.convertHtaccessLighty.config.options.DomainOption;
import eu._4fh.convertHtaccessLighty.config.options.Redirect;
import eu._4fh.convertHtaccessLighty.config.options.RegexType;
import eu._4fh.convertHtaccessLighty.htaccess.HtAccessParser;

public class Main {
	private Config config;
	static final public String nl = "\n";

	private Main() {
		config = new Config();
	}

	public void run(String[] args) {
		config.readConfig(new File("config.xml"));
		ListIterator<Domain> domains = config.getDomains();
		DecimalFormat domainIndexFormater = new DecimalFormat("000");

		while (domains.hasNext()) {
			Domain domain = domains.next();
			String data = createDomainEntry(domain);

			File file = new File(domainIndexFormater.format(domain.index) + "-"
					+ domain.name);
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

	protected String createDomainEntry(Domain domain) {
		StringBuffer buf = new StringBuffer();

		createDomainCondition(buf, domain);

		ListIterator<DomainOption> options = domain.domainOptions
				.listIterator();
		while (options.hasNext()) {
			DomainOption option = options.next();
			createDomainOption(buf, option);
			if (option instanceof DocRoot) {
				String docRoot = ((DocRoot) option).docRoot;
				parseHtAccessFiles(buf, new File(docRoot), new File(docRoot));
			}
		}

		buf.append("}").append(nl);

		return buf.toString();
	}

	protected void parseHtAccessFiles(StringBuffer buf, File docRoot, File dir) {
		createPathCondition(buf, docRoot, dir);
		File htAccessFile = new File(dir, ".htaccess");
		if (htAccessFile.exists()) {
			buf.append(new HtAccessParser().parseFile(htAccessFile));
		}
		File subDirs[] = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		for (int i = 0; i < subDirs.length; ++i) {
			parseHtAccessFiles(buf, docRoot, subDirs[i]);
		}
		buf.append("}");
		buf.append(nl);
	}

	protected void createPathCondition(StringBuffer buf, File docRoot, File dir) {
		buf.append("$HTTP[\"url\"] =~ \"^");
		String urlDir = dir.getAbsolutePath().substring(
				docRoot.getAbsolutePath().length());
		buf.append(urlDir.replace(File.separator, "/"));
		buf.append("\" {").append(nl);
	}

	protected void createDomainCondition(StringBuffer buf, Domain domain) {
		buf.append("$HTTP[\"host\"] =");
		if (domain.regexType == RegexType.NONE) {
			buf.append("= \"").append(domain.name).append("\"");
		} else if (domain.regexType == RegexType.WITH_PORT) {
			String tmp = domain.name.replace(".", "\\.");
			buf.append("~ \"^").append(tmp).append("(\\:[0-9]+)?$\"");
		}
		buf.append(" {").append(nl);
	}

	protected void createDomainOption(StringBuffer buf, DomainOption option) {
		if (option instanceof DocRoot) {
			buf.append("server.document-root = \"")
					.append(((DocRoot) option).docRoot).append("\"");
		} else if (option instanceof Redirect) {
			buf.append("url.redirect = ( \".*\" => \"")
					.append(((Redirect) option).redirectTo).append("\" )")
					.append(nl);
			buf.append("url.redirect-code = ")
					.append(((Redirect) option).redirectCode).append(nl);
		} else {
			throw new RuntimeException("Can't handle DomainOption of type "
					+ option.getClass().getCanonicalName());
		}
		buf.append(nl);
	}

	public static void main(String[] args) {
		new Main().run(args);
	}
}
