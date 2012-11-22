package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.io.FileFilter;

public class HtAccessTreeParser {
	private StringBuffer buf;
	private File root;
	public HtAccessTreeParser(StringBuffer buf, File root) {
		this.buf = buf;
		this.root = root.getAbsoluteFile();
	}

	public void parse() {
		File cur = new File(root.getAbsolutePath());
		doParseTree(cur, 0);
	}

	private void doParseTree(final File cur, int nestedLevel) {
		File htAccessFile = new File(cur, ".htaccess");
		if (htAccessFile.canRead()) {
			new HtAccessParser(buf, htAccessFile, nestedLevel).parse();
			nestedLevel++;
		}
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
}
