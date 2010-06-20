package hr.sandrogrzicic.igre.server;

import java.util.List;

/**
 * Predstavlja logiku igre, tj. dretvu koja se konstantno izvršava u obliku petlje na serveru.
 * 
 * @author Sandro Gržičić
 */
public abstract class AbstractIgra implements Runnable {
	protected AbstractServer server;
	protected List<AbstractIgrač> igrači;

	public abstract void pokreni();

	public abstract void zaustavi();

	/** Sadržava logiku igre. */
	public abstract void run();

}
