package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.Main;

public class HtAccessConverter {
	static final private Pattern PATTERN_FIND_HTACCESS_TAGS = Pattern
			.compile(
					"^\\p{Blank}*<\\p{Blank}*(/\\p{Blank}*){0,1}(IfModule|Files|FilesMatch)(\\W.*)$",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
							| Pattern.UNICODE_CASE);

	final private File file;
	final private String[] inActiveModules;
	final private String[] activeModules;

	public HtAccessConverter(final File file, final String[] inActiveModules,
			final String[] activeModules) {
		this.file = file;
		this.inActiveModules = inActiveModules;
		this.activeModules = activeModules;
	}

	public TreeNode splitFile(final StringBuffer input) {
		Matcher m = PATTERN_FIND_HTACCESS_TAGS.matcher(input);
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
				if (!condition.endsWith(">")) {
					throw new RuntimeException("Found Tag " + type
							+ " with condition " + condition
							+ " which doesn't end with a closing tag (>).");
				}
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
							+ cur.toString() + ". Malformed htaccess-File?");
		}
		return root;
	}

	public void flatenTree(final TreeNode root) {
		ListIterator<TreeNode> it = root.iterator();
		while (it.hasNext()) {
			TreeNode cur = it.next();
			switch (cur.getType()) {
				case IF_MODULE :
					if (checkIfModule(cur)) {
						ListIterator<TreeNode> copyIt = cur.iterator();
						while (copyIt.hasNext()) {
							TreeNode child = copyIt.next();
							child.setParent(cur.getParent());
							cur.getParent().addChild(child);
						}
						cur.getParent().appendLine(cur.getText());
					}
					if (!cur.getParent().delChild(cur)) {
						throw new RuntimeException("Tried to remove "
								+ cur.toString() + " from parent "
								+ cur.getParent()
								+ ", but parent didn't contained these node.");
					}
					it = root.iterator();
					break;

				case FILES :
				case FILES_MATCH :
					flatenTree(cur);
					if (!cur.getParent().getType().equals(TreeNode.TYPE.ROOT)) {
						throw new RuntimeException(
								"Found "
										+ cur.toString()
										+ " with parent "
										+ cur.getParent().toString()
										+ ", but for "
										+ cur.getType().toString()
										+ " the only allowed parent is "
										+ TreeNode.TYPE.ROOT.toString()
										+ ". Malformed .htaccess-File? (It isn't allowed to wrap any tag into any other tag, EXCEPT IfModule-Tags.)");
					}
					break;

				default :
					throw new RuntimeException("Found invalid node "
							+ cur.toString() + " while faltening tree");
			}
		}
	}

	private boolean checkIfModule(TreeNode node) {
		for (String module : inActiveModules) {
			if (node.getCondition().equalsIgnoreCase(module)) {
				return false;
			}
		}
		for (String module : activeModules) {
			if (node.getCondition().equalsIgnoreCase(module)) {
				return true;
			}
		}
		System.out.println("WARNING: File " + file.getAbsolutePath()
				+ " checks for non-defined module " + node.getCondition()
				+ ". Defaults to not installed.");
		return false;
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

	static public class TreeNode {
		public enum TYPE {
			ROOT(""), IF_MODULE("IfModule"), FILES("Files"), FILES_MATCH(
					"FilesMatch"), END_IF_MODULE(IF_MODULE), END_FILES(FILES), END_FILES_MATCH(
					FILES_MATCH);
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
		static final private Pattern PATTERN_SPLIT_LINE = Pattern
				.compile("[\\r\\n]+");
		final private TYPE type;
		final private String condition;
		private StringBuffer text;
		private List<TreeNode> children;
		private TreeNode parent;

		public TreeNode(final TreeNode parent, final TYPE type,
				final String condition) {
			this.parent = parent;
			this.type = type;
			this.condition = formatCondition(condition.trim()).trim();
			text = new StringBuffer("");
			children = new LinkedList<TreeNode>();
		}

		private String formatCondition(final String condition) {
			boolean startQuote = condition.startsWith("\"");
			boolean endQuote = condition.endsWith("\"");
			if (startQuote != endQuote) {
				throw new RuntimeException("Found node with condition "
						+ condition
						+ " which starts xor ends with quotes. Invalid tag?");
			}
			if (startQuote) {
				return condition.substring(1, condition.length() - 1);
			}
			return condition;
		}

		public void addChild(final TreeNode node) {
			children.add(node);
		}

		public boolean delChild(final TreeNode node) {
			return children.remove(node);
		}

		public ListIterator<TreeNode> iterator() {
			return children.listIterator();
		}

		public void setText(final String text) {
			this.text = new StringBuffer(text.trim());
		}

		public String getText() {
			return text.toString();
		}

		public void appendLine(final String text) {
			if (text.isEmpty()) {
				return;
			}
			String textParts[] = PATTERN_SPLIT_LINE.split(text);
			for (String textPart : textParts) {
				textPart = textPart.trim();
				if (textPart.isEmpty()) {
					continue;
				}
				this.text.append(Main.nl).append(textPart.trim());
			}
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

		public void setParent(final TreeNode node) {
			this.parent = node;
		}

		public String toString() {
			StringBuffer ret = new StringBuffer();
			ret.append("<").append(type.ends != null ? "/" : "")
					.append(type.text).append(" ").append(condition)
					.append(">");
			return ret.toString();
		}
	}
}
