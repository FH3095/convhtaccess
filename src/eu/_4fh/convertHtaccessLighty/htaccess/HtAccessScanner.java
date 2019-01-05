package eu._4fh.convertHtaccessLighty.htaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import eu._4fh.convertHtaccessLighty.Main;
import eu._4fh.convertHtaccessLighty.config.Config;
import eu._4fh.convertHtaccessLighty.htaccess.data.Auth;
import eu._4fh.convertHtaccessLighty.htaccess.data.DataHandler;
import eu._4fh.convertHtaccessLighty.htaccess.data.DataHandler.ParseException;
import eu._4fh.convertHtaccessLighty.htaccess.data.Expire;
import eu._4fh.convertHtaccessLighty.htaccess.data.LighttpdOption;
import eu._4fh.convertHtaccessLighty.htaccess.data.NginxOption;
import eu._4fh.convertHtaccessLighty.htaccess.data.Options;
import eu._4fh.convertHtaccessLighty.htaccess.data.OrderAllowDeny;
import eu._4fh.convertHtaccessLighty.htaccess.data.Rewrite;
import eu._4fh.convertHtaccessLighty.htaccess.tree.TreeElement;

public class HtAccessScanner {
	private final List<DataHandler> handlers;
	private final TreeElement tree;
	private final File file;

	public HtAccessScanner(final TreeElement tree, final File file) {
		this.tree = tree;
		this.file = file;
		final List<DataHandler> handlerList = new ArrayList<DataHandler>();
		handlerList.add(new OrderAllowDeny());
		handlerList.add(new Rewrite());
		handlerList.add(new Options());
		handlerList.add(new Auth());
		handlerList.add(new Expire());
		handlerList.add(new LighttpdOption());
		handlerList.add(new NginxOption());
		handlers = Collections.unmodifiableList(handlerList);
	}

	public void scan() {
		try (final Scanner scanner = new Scanner(file)) {
			scan(tree, null, scanner, false);
		} catch (FileNotFoundException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private void scan(final TreeElement tree, final BLOCKS block, final Scanner scanner, boolean ignoreContent)
			throws ParseException {
		final Pattern blockEndPattern = block != null ? block.endPattern : null;
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			if (blockEndPattern != null && blockEndPattern.matcher(line).matches()) {
				return;
			}

			Matcher matcher;
			if ((matcher = BLOCKS.FILES.startPattern.matcher(line)).matches()) {
				final String file = Main.removeQuotes(matcher.group(1));
				final TreeElement nextTree;
				if (file.equals("*")) {
					nextTree = tree;
				} else {
					nextTree = tree.getLastChild(file);
				}
				scan(nextTree, BLOCKS.FILES, scanner, ignoreContent);
			} else if ((matcher = BLOCKS.FILES_MATCH.startPattern.matcher(line)).matches()) {
				final String regex = Main.removeQuotes(matcher.group(1));
				final TreeElement nextTree = tree.getRegexChild(regex);
				scan(nextTree, BLOCKS.FILES_MATCH, scanner, ignoreContent);
			} else if ((matcher = BLOCKS.IF_MODULE.startPattern.matcher(line)).matches()) {
				final String module = Main.removeQuotes(matcher.group(1));
				scan(tree, BLOCKS.IF_MODULE, scanner, (!parseIfModule(module)) || ignoreContent);
			} else if (!ignoreContent) {
				parseLine(tree, line);
			}
		}
		if (block != null) {
			throw new RuntimeException(
					"Cant parse file \"" + file.toString() + "\". File contains not closed block " + block.blockType);
		}
	}

	private void parseLine(final TreeElement tree, final String line) {
		if (line.isEmpty()) {
			return;
		}

		final DataHandler handler = findDataHandler(line);
		if (handler != null) {
			try {
				handler.parseCommand(line);
			} catch (DataHandler.ParseException e) {
				System.out.println(e.getLocalizedMessage());
			}
		} else if (line.startsWith("#")) { // Needs to be after findDataHandler (because of ##Lighty / LighttpdOption)
			// Nothing to do
		} else {
			System.out.println(formatError("Unknown command", line));
		}
	}

	private DataHandler findDataHandler(final String line) {
		for (DataHandler handler : handlers) {
			if (handler.canParseLine(line)) {
				return handler;
			}
		}
		return null;
	}

	private String formatError(String error, String line) {
		return "Error while parsing file " + file.getAbsolutePath() + " line \'" + line + "\': " + error;
	}

	private boolean parseIfModule(String module) {
		final Config config = Main.getConfig();
		final Set<String> activeModules = Arrays.asList(config.getActiveModules()).stream()
				.map(str -> str.toLowerCase()).collect(Collectors.toSet());
		final Set<String> inactiveModules = Arrays.asList(config.getInActiveModules()).stream()
				.map(str -> str.toLowerCase()).collect(Collectors.toSet());

		final boolean inverse;
		if (module.startsWith("!")) {
			inverse = true;
			module = module.substring(1).trim();
		} else {
			inverse = false;
		}
		module = module.toLowerCase();

		if (activeModules.contains(module)) {
			return !inverse;
		} else if (inactiveModules.contains(module)) {
			return inverse;
		} else {
			System.out.println("WARNING: File " + file.getAbsolutePath() + " checks for non-defined module "
					+ (inverse ? "!" : "") + module + ". Defaults to not installed.");
			return inverse;
		}
	}

	private static enum BLOCKS {
		IF_MODULE("IfModule"), FILES("Files"), FILES_MATCH("FilesMatch");
		public final String blockType;
		public final Pattern startPattern;
		public final Pattern endPattern;

		private BLOCKS(final String blockType) {
			this.blockType = blockType;
			startPattern = Pattern.compile("^\\s*<" + blockType + "\\s+(\\S+)\\s*>\\s*$",
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			endPattern = Pattern.compile("^\\s*</" + blockType + "\\s*>\\s*$",
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		}
	}
}
