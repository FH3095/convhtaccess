package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ListIterator;

import eu._4fh.convertHtaccessLighty.Main;
import eu._4fh.convertHtaccessLighty.htaccess.HtAccessConverter.TreeNode;

public class HtAccessParser {
	final private StringBuffer buf;
	final private File docRoot;
	final private File file;
	final private int nestedLevel;
	final private SectionEventListener callback;
	final private String[] inActiveModules;
	final private String[] activeModules;

	public HtAccessParser(final StringBuffer buf, final File docRoot,
			final File htAccessFile, final String[] inActiveModules,
			final String[] activeModules, final SectionEventListener callback) {
		this.buf = buf;
		this.docRoot = docRoot;
		this.file = htAccessFile;
		this.activeModules = activeModules;
		this.inActiveModules = inActiveModules;
		this.callback = callback;
		this.nestedLevel = 2;
	}

	protected HtAccessParser(final StringBuffer buf, final File docRoot,
			final File htAccessFile, final String[] inActiveModules,
			final String[] activeModules, final SectionEventListener callback,
			final int nestedLevel) {
		this.buf = buf;
		this.docRoot = docRoot;
		this.file = htAccessFile;
		this.activeModules = activeModules;
		this.inActiveModules = inActiveModules;
		this.callback = callback;
		this.nestedLevel = nestedLevel;
	}

	public void parse() {
		StringBuffer input = new StringBuffer();
		try {
			FileReader reader = new FileReader(file);
			char inputBuf[] = new char[512];
			while (reader.read(inputBuf) > 0) {
				input.append(inputBuf);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Can't find file "
					+ file.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RuntimeException("Error while reading file "
					+ file.getAbsolutePath(), e);
		}

		input = new StringBuffer(input.toString().trim()); // Cut off trash
															// data from
															// char[512]
		input.append(Main.nl);
		input.trimToSize();

		HtAccessConverter converter = new HtAccessConverter(file,
				inActiveModules, activeModules);
		TreeNode rootNode = converter.splitFile(input);
		converter.flatenTree(rootNode);

		doParseNode(rootNode, nestedLevel);
	}

	private void doParseNode(final TreeNode node, int nestedLevel) {
		HtAccessDataParser parser = null;
		if (!node.getText().isEmpty()
				|| node.getType().equals(TreeNode.TYPE.ROOT)) {
			if (node.getType().equals(TreeNode.TYPE.ROOT)) {
				Main.writeIndentLine(buf, 0, "# ", file.getAbsolutePath());
			}

			if (callback != null) {
				callback.preStartBlock(buf, node);
			}

			createPathCondition(node, nestedLevel);

			if (callback != null) {
				callback.postStartBlock(buf, node);
			}

			parser = new HtAccessDataParser(buf, node.getText(), nestedLevel,
					file);
			parser.parse();
			nestedLevel++;
			Main.writeIndentLine(buf, 0, "");
		}

		ListIterator<TreeNode> it = node.iterator();
		while (it.hasNext()) {
			doParseNode(it.next(), nestedLevel);
		}

		if (parser != null) {
			if (callback != null) {
				callback.preEndBlock(buf, node);
			}

			Main.writeIndentLine(buf, nestedLevel - 2, "}");

			if (callback != null) {
				callback.postEndBlock(buf, node);
			}
		}
	}

	private void createPathCondition(final TreeNode node, int nestedLevel) {
		File dir = file.getParentFile();
		if (!dir.getAbsolutePath().startsWith(docRoot.getAbsolutePath())) {
			throw new RuntimeException("WebPath " + dir.getAbsolutePath()
					+ " doesn't start with docRoot-Dir "
					+ docRoot.getAbsolutePath());
		}

		String webPath = dir.getAbsolutePath().substring(
				docRoot.getAbsolutePath().length());
		webPath = webPath.replace(File.separator, "/");
		if (!webPath.startsWith("/")) {
			webPath = "/" + webPath;
		}
		if (!webPath.endsWith("/")) {
			webPath += "/";
		}

		String treePath = "";
		switch (node.getType()) {
			case FILES : {
				String condition = Main.quoteRegexString(node.getCondition());
				treePath = condition + treePath;
			}
				break;

			case FILES_MATCH :
				treePath = node.getCondition() + treePath;
				break;

			case ROOT :
				break;
			default :
				throw new RuntimeException("Got unhandled Tag-Type "
						+ node.getType() + " from tag " + node.toString());
		}

		Main.writeIndentLine(buf, nestedLevel - 1, "$HTTP[\"url\"] =~ \"^",
				Main.quoteRegexString(webPath + treePath), "\" {");
	}

	static public interface SectionEventListener {
		public void preStartBlock(StringBuffer buf, TreeNode node);
		public void postStartBlock(StringBuffer buf, TreeNode node);
		public void preEndBlock(StringBuffer buf, TreeNode node);
		public void postEndBlock(StringBuffer buf, TreeNode node);
	}
}
