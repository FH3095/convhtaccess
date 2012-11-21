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
	private ArrayList<Domain> domains;

	public Config() {
		defaultRegexType = null;
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

	public ListIterator<Domain> getDomains() {
		return domains.listIterator();
	}

	protected void insertConfigNode(ConfigNode node) {
		if (node instanceof Option) {
			if (defaultRegexType != null) {
				throw new RuntimeException(
						"Found more than one Option-Node in config!");
			}
			defaultRegexType = ((Option) node).regexType;
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
			return new Option(type);
		} else if (node.getLocalName().equals("Domain")) {
			RegexType type = getDefaultRegexType();
			if (node.getAttributes().getNamedItem("regexType") != null) {
				type = parseRegexTypeAttribute(node.getAttributes()
						.getNamedItem("regexType").getNodeValue());
			}
			String name = node.getAttributes().getNamedItem("name")
					.getNodeValue();
			DomainOption[] domainOptions = parseDomainOptions(node);
			return new Domain(name, type, domainOptions);
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
			return new Redirect(node.getNodeValue(), code);
		} else if (node.getLocalName().equals("DocRoot")) {
			return new DocRoot(node.getNodeValue());
		}
		throw new RuntimeException("Can't parse invalid Domain-Sub-Node "
				+ node.getNodeName() + " ; " + node.toString());
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
