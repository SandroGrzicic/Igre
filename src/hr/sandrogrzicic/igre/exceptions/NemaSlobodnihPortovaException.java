package hr.sandrogrzicic.igre.exceptions;

import java.io.IOException;

public class NemaSlobodnihPortovaException extends IOException {
	private static final long	serialVersionUID	= -7602122123578222494L;

	public NemaSlobodnihPortovaException() {
		super("Nema slobodnih portova!");
	}
}
