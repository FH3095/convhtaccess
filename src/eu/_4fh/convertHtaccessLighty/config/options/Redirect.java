package eu._4fh.convertHtaccessLighty.config.options;

public class Redirect extends DomainOption {
	public final String redirectTo;
	public final short redirectCode;
	public final boolean redirectWithPath;

	public Redirect(final String redirectTo, final short redirectCode,
			final boolean redirectWithPath) {
		this.redirectTo = redirectTo;
		this.redirectCode = redirectCode;
		this.redirectWithPath = redirectWithPath;
	}
}
