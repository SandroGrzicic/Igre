package hr.sandrogrzicic.igre.server;

import hr.sandrogrzicic.igre.utility.UDP;

public abstract class AbstractIgračMreža {
	protected final AbstractIgrač igrač;
	protected final UDP veza;
	protected final AbstractServer server;

	/**
	 * Kreira novi mrežni podsustav za ovog igrača.
	 */
	public AbstractIgračMreža(final AbstractIgrač igrač, final UDP veza, final AbstractServer server) {
		this.igrač = igrač;
		this.veza = veza;
		this.server = server;
	}

	/** Javlja igraču spojenom na ovu konekciju da je odspojen, bez obzira je li već prekinuta konekcija. */
	public abstract void igračOdspojen();

}
