package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.io.FileFilter;

import eu._4fh.convertHtaccessLighty.htaccess.HtAccessConverter.TreeNode;

public class HtAccessTreeParser {
	private StringBuffer buf;
	private File root;
	private final String[] inActiveModules;
	private final String[] activeModules;

	public HtAccessTreeParser(StringBuffer buf, File root,
			final String[] inActiveModules, final String[] activeModules) {
		this.buf = buf;
		this.root = root.getAbsoluteFile();
		this.inActiveModules = inActiveModules;
		this.activeModules = activeModules;
	}

	public void parse() {
		File cur = new File(root.getAbsolutePath());
		doParseTree(cur, 2);
	}

	private void doParseTree(final File cur, int nestedLevel) {
		File htAccessFile = new File(cur, ".htaccess");
		HtAccessParser parser = null;
		Callback callback = null;
		if (htAccessFile.canRead()) {
			callback = new Callback(cur, nestedLevel);
			parser = new HtAccessParser(buf, root, htAccessFile,
					inActiveModules, activeModules, callback, nestedLevel);
			System.out.println("----- " + htAccessFile.getAbsolutePath());
			parser.parse();
		} else {
			doParseSubdirs(cur, nestedLevel);
		}
	}

	private void doParseSubdirs(final File cur, int nestedLevel) {
		File subDirs[] = cur.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		for (int i = 0; i < subDirs.length; ++i) {
			doParseTree(subDirs[i], nestedLevel);
		}
	}

	private class Callback implements HtAccessParser.SectionEventListener {
		private final File curDir;
		private final int nestedLevel;
		private Callback(final File curDir, final int nestedLevel) {
			this.curDir = curDir;
			this.nestedLevel = nestedLevel;
		}
		@Override
		public void preStartBlock(StringBuffer buf, TreeNode node) {
		}
		@Override
		public void postStartBlock(StringBuffer buf, TreeNode node) {
		}
		@Override
		public void preEndBlock(StringBuffer buf, TreeNode node) {
			if (node.getType().equals(TreeNode.TYPE.ROOT)) {
				doParseSubdirs(curDir, nestedLevel + 1);
			}
		}
		@Override
		public void postEndBlock(StringBuffer buf, TreeNode node) {
		}
	}
}
