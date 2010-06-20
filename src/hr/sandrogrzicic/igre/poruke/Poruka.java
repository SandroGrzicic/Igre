package hr.sandrogrzicic.igre.poruke;

import hr.sandrogrzicic.igre.server.AbstractIgrač;

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

	public final AbstractIgrač getIzvor() {
		return izvor;
	}

}
