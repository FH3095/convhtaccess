package eu._4fh.convertHtaccessLighty.config.options;

public class Redirect extends DomainOption {
	public final String redirectTo;
	public final short redirectCode;

	public Redirect(final String redirectTo, final short redirectCode) {
		this.redirectTo = redirectTo;
		this.redirectCode = redirectCode;
	}
}
