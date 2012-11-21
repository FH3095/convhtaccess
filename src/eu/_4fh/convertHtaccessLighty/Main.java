package eu._4fh.convertHtaccessLighty;

import java.io.File;
import java.util.ListIterator;

import eu._4fh.convertHtaccessLighty.config.Config;
import eu._4fh.convertHtaccessLighty.config.options.Domain;

public class Main {
	private Config config;

	private Main() {
		config = new Config();
	}

	public void run(String[] args) {
		config.readConfig(new File("config.xml"));
		ListIterator<Domain> domains = config.getDomains();
	}

	public static void main(String[] args) {
		new Main().run(args);
	}
}
