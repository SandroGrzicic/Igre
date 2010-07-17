package hr.sandrogrzicic.igre.poruke;

import hr.sandrogrzicic.igre.server.AbstractIgrač;

import java.io.ByteArrayOutputStream;

public abstract class Poruka {
	protected final AbstractIgrač izvor;

	/**
	 * Generira novu poruku.
	 * 
	 * @param izvor
	 *            igrač koji je generirao poruku
	 * @param timestamp
	 *            UNIX (u sekundama)
	 */
	protected Poruka(final AbstractIgrač izvor) {
		this.izvor = izvor;
	}

	/**
	 * Vraća izvor ove poruke.
	 */
	public final AbstractIgrač getIzvor() {
		return izvor;
	}

	/**
	 * Vraća ByteArrayOutputStream koji se može koristiti za konstruiranje paketa pri slanju preko mreže.
	 */
	public abstract ByteArrayOutputStream getBAOS();

}
