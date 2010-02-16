package hr.sandrogrzicic.igre.exceptions;

import hr.sandrogrzicic.igre.spheres.Akcije;

import java.io.IOException;


public class NeočekivanaAkcijaException extends IOException {
	private static final long	serialVersionUID	= 6013764291458553859L;

	public NeočekivanaAkcijaException(final Akcije akcija) {
		super("Dobivena neočekivana akcija [" + akcija + "]!");
	}

	public NeočekivanaAkcijaException(final Akcije akcija, final int id) {
		super("Dobivena neočekivana akcija [" + akcija + "] od igrača [" + id + "]!");
	}

}
