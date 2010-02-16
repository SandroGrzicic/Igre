package hr.sandrogrzicic.igre.exceptions;

import java.io.IOException;

public class NevaljaniPaketException extends IOException {
	private static final long	serialVersionUID	= 2696007828900963630L;

	public NevaljaniPaketException() {
		super();
	}

	public NevaljaniPaketException(final byte[] sadržaj, final int len) {
		super("Dobiven nevaljani paket [" + new String(sadržaj, 0, len) + "]!");
	}

}
