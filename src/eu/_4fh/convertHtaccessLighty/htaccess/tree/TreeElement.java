package eu._4fh.convertHtaccessLighty.htaccess.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu._4fh.convertHtaccessLighty.htaccess.data.DataHandler;

public class TreeElement {
	private static final Pattern PATH_PART_PATTERN = Pattern.compile("[^\\\\/]+");
	private final String path;
	private final boolean isRegex;
	private final Map<String, TreeElement> childs;
	private final List<DataHandler> handlers;

	public TreeElement() {
		this("", false);
	}

	private TreeElement(final String path, final boolean isRegex) {
		this.path = path;
		this.isRegex = isRegex;
		this.childs = new HashMap<>();
		this.handlers = new ArrayList<>();
	}

	public TreeElement getRegexChild(final String regex) {
		final String modifiedRegex = regex.startsWith("^") ? regex.substring(1) : regex;
		return childs.computeIfAbsent(regex, (key) -> new TreeElement(modifiedRegex, true));
	}

	public TreeElement getLastChild(final String path) {
		if (isRegex) {
			throw new RuntimeException("Cant get childs for regex-element " + this.path + " ; " + path);
		}
		final Matcher pathParts = PATH_PART_PATTERN.matcher(path);
		TreeElement lastChild = null;
		while (pathParts.find()) {
			final String pathPart = pathParts.group();
			lastChild = childs.computeIfAbsent(pathPart, (key) -> new TreeElement(pathPart, false));
		}
		if (lastChild == null) {
			throw new RuntimeException("Invalid path " + path);
		}
		return lastChild;
	}

	public List<DataHandler> getData() {
		return Collections.unmodifiableList(handlers);
	}

	public void addData(final DataHandler handler) {
		handlers.add(handler);
	}

	public Map<String, TreeElement> getChilds() {
		return Collections.unmodifiableMap(childs);
	}

	public String getPath() {
		return path;
	}

	public boolean isRegex() {
		return isRegex;
	}
}
