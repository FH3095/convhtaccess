package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

class HtAccessParser {
	final private StringBuffer buf;
	final private File file;
	final private int nestedLevel;
	private StringBuffer input;
	// static final private String nl = Main.nl;

	public HtAccessParser(final StringBuffer buf, final File htAccessFile,
			final int nestedLevel) {
		this.buf = buf;
		this.file = htAccessFile;
		this.nestedLevel = nestedLevel;
		input = null;
	}

	private TreeNode splitFile() {
		Pattern p = Pattern
				.compile(
						"^\\p{Blank}*<\\p{Blank}*(/\\p{Blank}*){0,1}(IfModule|Files|FilesMatch)(\\W.*)$",
						Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
								| Pattern.UNICODE_CASE);
		Matcher m = p.matcher(input);
		int start = 0;
		TreeNode root = new TreeNode(null, TreeNode.TYPE.ROOT, "");
		TreeNode cur = root;
		while (m.find()) {
			cur.appendLine(input.substring(start, m.start()).trim());

			String patternMatch = input.substring(m.start(), m.end()).trim();
			TreeNode.TYPE type = calcTreeNodeType(patternMatch);
			if (type.ends != null) {
				if (!type.ends.equals(cur.getType())) {
					throw new RuntimeException("Got closing Tag " + type
							+ ", but currently opened tag is " + cur.getType()
							+ ". Malformed .htaccess-File?");
				}
				cur = cur.getParent();
			} else {
				String condition = m.group(3).trim();
				condition = condition.substring(0, condition.length() - 1)
						.trim();
				TreeNode newNode = new TreeNode(cur, type, condition);
				cur.addChild(newNode);
				cur = newNode;
			}

			start = m.end();
		}
		root.appendLine(input.substring(start).trim());
		if (root != cur) {
			throw new RuntimeException(
					"Reached end of HtAccessFile, but currently opened tag is "
							+ cur.getType() + " " + cur.getCondition()
							+ ". Malformed htaccess-File?");
		}

		return root;
	}

	private TreeNode.TYPE calcTreeNodeType(String patternMatch) {
		patternMatch = patternMatch.substring(1).trim().toLowerCase();
		boolean endTag = false;
		if (patternMatch.startsWith("/")) {
			endTag = true;
			patternMatch = patternMatch.substring(1).trim();
		}
		TreeNode.TYPE types[] = TreeNode.TYPE.values();
		for (TreeNode.TYPE type : types) {
			if (!type.equals(TreeNode.TYPE.ROOT)
					&& endTag == (type.ends != null)
					&& Pattern.compile("^" + Pattern.quote(type.text) + "\\W")
							.matcher(patternMatch).find()) {
				return type;
			}
		}
		throw new RuntimeException("Can't identify pattern " + patternMatch);
	}

	public void parse() {
		input = new StringBuffer();
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
		TreeNode rootNode = splitFile();
		doParseNode(rootNode, nestedLevel);
	}

	private void doParseNode(final TreeNode node, final int nestedLevel) {
		new HtAccessDataParser(buf, file, node.getText(), nestedLevel).parse();
		ListIterator<TreeNode> it = node.iterator();
		while (it.hasNext()) {
			doParseNode(it.next(), nestedLevel + 1);
		}
	}

	protected void createPathCondition(StringBuffer buf, File docRoot, File dir) {
		buf.append("$HTTP[\"url\"] =~ \"^");
		String urlDir = dir.getAbsolutePath().substring(
				docRoot.getAbsolutePath().length());
		buf.append(urlDir.replace(File.separator, "/"));
		buf.append("\" {").append(Main.nl);
	}

	@SuppressWarnings("unused")
	static private class TreeNode {
		public enum TYPE {
			ROOT(""), IF_MODULE("IfModule"), FILES("Files"), FILESMATCH(
					"FilesMatch"), END_IF_MODULE(IF_MODULE), END_FILES(FILES), END_FILESMATCH(
					FILESMATCH);
			final public TYPE ends;
			final public String text;
			private TYPE(final TYPE ends) {
				this.text = ends.text;
				this.ends = ends;
			}
			private TYPE(final String text) {
				this.text = text.toLowerCase();
				this.ends = null;
			}
		}
		final private TYPE type;
		final private String condition;
		private String text;
		private List<TreeNode> children;
		final private TreeNode parent;

		public TreeNode(final TreeNode parent, final TYPE type,
				final String condition) {
			this.parent = parent;
			this.type = type;
			this.condition = condition;
			text = "";
			children = new LinkedList<TreeNode>();
			/*
			 * System.out.print("New Node " + type.toString() + " " +
			 * condition); if (parent != null) { System.out.print(" parent: " +
			 * parent.type.toString() + " " + parent.condition); }
			 * System.out.println();
			 */
		}

		public void addChild(final TreeNode node) {
			children.add(node);
		}

		public void delChild(final TreeNode node) {
			children.remove(node);
		}

		public ListIterator<TreeNode> iterator() {
			return children.listIterator();
		}

		public void setText(final String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public void appendLine(final String text) {
			if (text.isEmpty()) {
				return;
			}
			this.text = (this.text + Main.nl + text).trim();
		}

		public TYPE getType() {
			return type;
		}

		public String getCondition() {
			return condition;
		}

		public TreeNode getParent() {
			return parent;
		}
	}
}
