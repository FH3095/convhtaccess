package eu._4fh.convertHtaccessLighty.config;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu._4fh.convertHtaccessLighty.config.options.ConfigNode;
import eu._4fh.convertHtaccessLighty.config.options.DocRoot;
import eu._4fh.convertHtaccessLighty.config.options.Domain;
import eu._4fh.convertHtaccessLighty.config.options.DomainOption;
import eu._4fh.convertHtaccessLighty.config.options.Option;
import eu._4fh.convertHtaccessLighty.config.options.Redirect;
import eu._4fh.convertHtaccessLighty.config.options.RegexType;

public class Config {
	private RegexType defaultRegexType;
	private String defaultFilePrefix;
	private String defaultFilePostfix;
	private ArrayList<Domain> domains;
	private String[] inActiveModules;
	private String[] activeModules;

	public Config() {
		defaultRegexType = null;
		defaultFilePrefix = null;
		defaultFilePostfix = null;
		inActiveModules = null;
		activeModules = null;
		domains = new ArrayList<Domain>();
	}

	public void readConfig(File configFile) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			dbFactory.setNamespaceAware(true);
			dbFactory.setValidating(true);
			dbFactory.setAttribute(
					"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					XMLConstants.W3C_XML_SCHEMA_NS_URI); // Force to use XSD
															// instead of DTD
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException arg0) throws SAXException {
					System.out.println("WARNING while parsing config:");
					arg0.printStackTrace(System.err);
				}
				@Override
				public void fatalError(SAXParseException arg0)
						throws SAXException {
					throw new RuntimeException(
							"Fatal Error while parsing config", arg0);
				}
				@Override
				public void error(SAXParseException arg0) throws SAXException {
					throw new RuntimeException("Error while parsing config",
							arg0);
				}
			});
			Document doc = builder.parse(configFile);
			doc.normalizeDocument();
			Node root = doc.getElementsByTagNameNS("", "Domains").item(0);
			for (Node cur = root.getFirstChild(); cur != null; cur = cur
					.getNextSibling()) {
				if (cur.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				insertConfigNode(xmlNodeToConfigNode(cur));
				cur = cur.getNextSibling();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading config", e);
		}
	}

	public RegexType getDefaultRegexType() {
		return defaultRegexType;
	}

	public String getDefaultFilePrefix() {
		return defaultFilePrefix;
	}

	public String getDefaultFilePostfix() {
		return defaultFilePostfix;
	}

	public ListIterator<Domain> getDomains() {
		return domains.listIterator();
	}

	public String[] getInActiveModules() {
		return inActiveModules;
	}

	public String[] getActiveModules() {
		return activeModules;
	}

	protected void insertConfigNode(ConfigNode node) {
		if (node instanceof Option) {
			if (defaultRegexType != null) {
				throw new RuntimeException(
						"Found more than one Option-Node in config!");
			}
			defaultRegexType = ((Option) node).regexType;
			defaultFilePrefix = ((Option) node).prefix;
			defaultFilePostfix = ((Option) node).postfix;
		} else if (node instanceof Domain) {
			domains.add((Domain) node);
		} else {
			throw new RuntimeException("Can't insert ConfigNode of type "
					+ node.getClass().getCanonicalName());
		}
	}

	protected ConfigNode xmlNodeToConfigNode(Node node) {
		if (node.getLocalName().equals("Options")) {
			RegexType type = parseRegexTypeAttribute(node.getAttributes()
					.getNamedItem("regexType").getNodeValue());
			String prefix = node.getAttributes().getNamedItem("prefix")
					.getNodeValue();
			String postfix = node.getAttributes().getNamedItem("postfix")
					.getNodeValue();
			parseOptions(node);
			return new Option(type, prefix, postfix);
		} else if (node.getLocalName().equals("Domain")) {
			RegexType type = getDefaultRegexType();
			if (node.getAttributes().getNamedItem("regexType") != null) {
				type = parseRegexTypeAttribute(node.getAttributes()
						.getNamedItem("regexType").getNodeValue());
			}
			String filePrefix = getDefaultFilePrefix();
			if (node.getAttributes().getNamedItem("prefix") != null) {
				filePrefix = node.getAttributes().getNamedItem("prefix")
						.getNodeValue();
			}
			String filePostfix = getDefaultFilePostfix();
			if (node.getAttributes().getNamedItem("postfix") != null) {
				filePostfix = node.getAttributes().getNamedItem("postfix")
						.getNodeValue();
			}
			String name = node.getAttributes().getNamedItem("name")
					.getNodeValue();
			short index = Short.parseShort(node.getAttributes()
					.getNamedItem("index").getNodeValue());
			DomainOption[] domainOptions = parseDomainOptions(node);
			return new Domain(name, index, filePrefix, filePostfix, type,
					domainOptions);
		}
		throw new RuntimeException("Found unsupported Node in Config-File: "
				+ node.getNodeName() + " ; " + node.toString());
	}
	protected DomainOption[] parseDomainOptions(Node node) {
		List<DomainOption> ret = new LinkedList<DomainOption>();
		for (Node cur = node.getFirstChild(); cur != null; cur = cur
				.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			ret.add(parseDomainOption(cur));
		}
		return ret.toArray(new DomainOption[]{});
	}

	protected DomainOption parseDomainOption(Node node) {
		if (node.getLocalName().equals("Redirect")) {
			short code = Short.parseShort(node.getAttributes()
					.getNamedItem("code").getNodeValue());
			return new Redirect(node.getTextContent().trim(), code);
		} else if (node.getLocalName().equals("DocRoot")) {
			return new DocRoot(node.getTextContent().trim());
		}
		throw new RuntimeException("Can't parse invalid Domain-Sub-Node "
				+ node.getNodeName() + " ; " + node.toString());
	}

	protected void parseOptions(Node node) {
		for (Node cur = node.getFirstChild(); cur != null; cur = cur
				.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (cur.getLocalName().equals("ApacheModules")) {
				parseApacheModules(cur);
			}
		}
	}

	protected void parseApacheModules(Node node) {
		List<String> resultActive = new LinkedList<String>();
		List<String> resultInActive = new LinkedList<String>();
		for (Node cur = node.getFirstChild(); cur != null; cur = cur
				.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (cur.getAttributes().getNamedItem("active").getTextContent()
					.trim().equalsIgnoreCase("true")) {
				resultActive.add(cur.getTextContent().trim());
			} else {
				resultInActive.add(cur.getTextContent().trim());
			}
		}
		inActiveModules = resultInActive.toArray(new String[1]);
		activeModules = resultActive.toArray(new String[1]);
	}

	protected RegexType parseRegexTypeAttribute(String attribute) {
		if (attribute.equals("none")) {
			return RegexType.NONE;
		} else if (attribute.equals("withPort")) {
			return RegexType.WITH_PORT;
		}
		throw new RuntimeException(
				"Found ConfigAttribute RegexType with invalid string.");
	}
}
