package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import eu._4fh.convertHtaccessLighty.htaccess.tree.TreeElement;

public class TreeScanner {
	private final File rootPath;
	private final TreeElement root;

	public TreeScanner(final String rootPath) {
		this.rootPath = new File(rootPath);
		if (!this.rootPath.isDirectory()) {
			throw new RuntimeException("Path \"" + rootPath + "\" is not a directory");
		}
		this.root = new TreeElement();
	}

	public void scan() {
		scan(rootPath);
	}

	private void scan(final File path) {
		final List<File> subDirs = new ArrayList<>();
		final File[] subFiles = path.listFiles(new ScanDirFilter());
		for (final File subFile : subFiles) {
			if (subFile.isDirectory()) {
				subDirs.add(subFile);
			} else {
				final Path subPath = rootPath.toPath().relativize(subFile.toPath());
				System.out.println("----- " + rootPath.toString() + " -> " + subPath.toString());
				final TreeElement child = root.getLastChild(subPath.toString());
				new HtAccessScanner(child, subFile).scan();
			}
		}

		for (final File subDir : subDirs) {
			scan(subDir);
		}
	}

	public TreeElement getRoot() {
		return root;
	}

	private static final class ScanDirFilter implements FileFilter {
		private static final String HTACCESS_NAME = ".htaccess";

		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory() || HTACCESS_NAME.equals(pathname.getName())) {
				return true;
			}
			return false;
		}

	}
}
