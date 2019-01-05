package eu._4fh.convertHtaccessLighty.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu._4fh.convertHtaccessLighty.config.options.DocRoot;
import eu._4fh.convertHtaccessLighty.config.options.Domain;
import eu._4fh.convertHtaccessLighty.config.options.DomainOption;
import eu._4fh.convertHtaccessLighty.config.options.OptionsPostfix;
import eu._4fh.convertHtaccessLighty.config.options.OptionsPrefix;
import eu._4fh.convertHtaccessLighty.config.options.Redirect;

public class Config {
	private String defaultFilePrefix;
	private String defaultFilePostfix;
	private ArrayList<Domain> domains;
	private String[] inActiveModules;
	private String[] activeModules;
	private Map<String, String> templates;

	public Config() {
		defaultFilePrefix = null;
		defaultFilePostfix = null;
		inActiveModules = null;
		activeModules = null;
		domains = new ArrayList<Domain>();
		templates = new HashMap<String, String>();
	}

	public void readConfig(File configFile) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			dbFactory.setValidating(true);
			dbFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					XMLConstants.W3C_XML_SCHEMA_NS_URI); // Force to use XSD instead of DTD

			DocumentBuilder builder = dbFactory.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException arg0) throws SAXException {
					System.out.println("WARNING while parsing config:");
					arg0.printStackTrace(System.err);
				}

				@Override
				public void fatalError(SAXParseException arg0) throws SAXException {
					throw new RuntimeException("Fatal Error while parsing config", arg0);
				}

				@Override
				public void error(SAXParseException arg0) throws SAXException {
					throw new RuntimeException("Error while parsing config", arg0);
				}
			});
			Document doc = builder.parse(configFile);
			doc.normalizeDocument();
			Node root = doc.getElementsByTagNameNS("", "Domains").item(0);
			for (Node cur = root.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
				if (cur.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				xmlNodeToConfig(cur);
				cur = cur.getNextSibling();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading config", e);
		}
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

	protected void xmlNodeToConfig(Node node) {
		if (node.getLocalName().equals("Options")) {
			defaultFilePrefix = node.getAttributes().getNamedItem("prefix").getNodeValue();
			defaultFilePostfix = node.getAttributes().getNamedItem("postfix").getNodeValue();
			parseOptions(node);
		} else if (node.getLocalName().equals("Templates")) {
			parseTemplates(node);
		} else if (node.getLocalName().equals("Domain")) {
			String filePrefix = getDefaultFilePrefix();
			if (node.getAttributes().getNamedItem("prefix") != null) {
				filePrefix = node.getAttributes().getNamedItem("prefix").getNodeValue();
			}
			String filePostfix = getDefaultFilePostfix();
			if (node.getAttributes().getNamedItem("postfix") != null) {
				filePostfix = node.getAttributes().getNamedItem("postfix").getNodeValue();
			}
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			short index = Short.parseShort(node.getAttributes().getNamedItem("index").getNodeValue());
			List<DomainOption> domainOptions = parseDomainOptions(node);

			StringBuilder optionsPrefix = new StringBuilder();
			StringBuilder optionsPostfix = new StringBuilder();
			Iterator<DomainOption> domainOptionsIt = domainOptions.iterator();
			while (domainOptionsIt.hasNext()) {
				DomainOption option = domainOptionsIt.next();
				boolean found = false;
				if (option instanceof OptionsPrefix) {
					optionsPrefix.append(((OptionsPrefix) option).options.trim()).append("\n");
					found = true;

				} else if (option instanceof OptionsPostfix) {
					optionsPostfix.append(((OptionsPostfix) option).options.trim()).append("\n");
					found = true;
				}
				if (found) {
					domainOptionsIt.remove();
					domainOptionsIt = domainOptions.iterator();
				}
			}
			domains.add(new Domain(name, index, filePrefix, filePostfix, optionsPrefix.toString(),
					optionsPostfix.toString(), domainOptions.toArray(new DomainOption[] {})));
		} else {
			throw new RuntimeException(
					"Found unsupported Node in Config-File: " + node.getNodeName() + " ; " + node.toString());
		}
	}

	protected List<DomainOption> parseDomainOptions(Node node) {
		List<DomainOption> ret = new LinkedList<DomainOption>();
		for (Node cur = node.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			ret.add(parseDomainOption(cur));
		}
		return ret;
	}

	protected DomainOption parseDomainOption(Node node) {
		if (node.getLocalName().equals("Redirect")) {
			short code = Short.parseShort(node.getAttributes().getNamedItem("code").getNodeValue());
			boolean withPath = Boolean
					.parseBoolean(node.getAttributes().getNamedItem("redirectWithPath").getNodeValue());
			return new Redirect(node.getTextContent().trim(), code, withPath);
		} else if (node.getLocalName().equals("DocRoot")) {
			return new DocRoot(node.getTextContent().trim());
		} else if (node.getLocalName().equals("OptionsPrefix")) {
			return new OptionsPrefix(node.getTextContent().trim());
		} else if (node.getLocalName().equals("OptionsPostfix")) {
			return new OptionsPostfix(node.getTextContent().trim());
		} else if (node.getLocalName().equals("OptionsTemplatePrefix")
				|| node.getLocalName().equals("OptionsTemplatePostfix")) {
			return parseOptionsTemplate(node);
		}
		throw new RuntimeException(
				"Can't parse invalid Domain-Sub-Node " + node.getNodeName() + " ; " + node.toString());
	}

	private DomainOption parseOptionsTemplate(Node node) {
		if (!templates.containsKey(node.getAttributes().getNamedItem("name").getTextContent())) {
			throw new RuntimeException("Can't find Template"
					+ node.getAttributes().getNamedItem("name").getTextContent() + " ; " + node.toString());
		}

		List<String> parameter = new ArrayList<String>();
		for (Node cur = node.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			parameter.add(cur.getTextContent());
		}

		String result = templates.get(node.getAttributes().getNamedItem("name").getTextContent());
		for (int i = 0; i < parameter.size(); ++i) {
			result = result.replaceAll("([^\\\\])\\$" + (i + 1) + "(\\D)", "$1" + parameter.get(i) + "$2");
		}

		if (node.getLocalName().equals("OptionsTemplatePrefix")) {
			return new OptionsPrefix(result);
		} else if (node.getLocalName().equals("OptionsTemplatePostfix")) {
			return new OptionsPostfix(result);
		}
		throw new RuntimeException(
				"Invalid node for parseOptionsTemplate: " + node.getLocalName() + " ; " + node.toString());
	}

	protected void parseOptions(Node node) {
		for (Node cur = node.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (cur.getLocalName().equals("ApacheModules")) {
				parseApacheModules(cur);
			}
		}
	}

	protected void parseTemplates(Node node) {
		for (Node cur = node.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			templates.put(cur.getAttributes().getNamedItem("name").getNodeValue(), cur.getTextContent());
		}
	}

	protected void parseApacheModules(Node node) {
		List<String> resultActive = new LinkedList<String>();
		List<String> resultInActive = new LinkedList<String>();
		for (Node cur = node.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
			if (cur.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (cur.getAttributes().getNamedItem("active").getTextContent().trim().equalsIgnoreCase("true")) {
				resultActive.add(cur.getTextContent().trim());
			} else {
				resultInActive.add(cur.getTextContent().trim());
			}
		}
		inActiveModules = resultInActive.toArray(new String[1]);
		activeModules = resultActive.toArray(new String[1]);
	}
}
