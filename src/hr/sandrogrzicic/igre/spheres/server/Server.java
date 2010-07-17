package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.server.AbstractIgra;
import hr.sandrogrzicic.igre.server.AbstractIgrač;
import hr.sandrogrzicic.igre.server.AbstractListener;
import hr.sandrogrzicic.igre.server.AbstractServer;
import hr.sandrogrzicic.igre.spheres.objekti.Loptica;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Glavni server za igru.
 * 
 * @author Sandro Gržičić
 */
public class Server extends AbstractServer {
	public static final int VERZIJA = 23;
	static final long KAŠNJENJE_MREŽA_RIJETKO = 100;
	static final long KAŠNJENJE_MREŽA_ČESTO = 25;
	static final long KAŠNJENJE_IGRA = 150;
	static final TimeUnit KAŠNJENJE_IGRA_TIMEUNIT = TimeUnit.MICROSECONDS;
	static final int SERVER_PORT = 7710;
	static final int SERVER_PORT_MAX = 7750;
	static final int SERVER_MAX_IGRAČA = 16;
	/** Maksimalan broj konekcija s jednog IP-a */
	static final Short MAKSIMALAN_BROJ_KLONOVA = 8;
	@Override
	protected ScheduledExecutorService inicijalizajExecutora() {
		return Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	protected AbstractIgra inicijalizirajIgru() {
		return new Igra(this, igrači);
	}

	@Override
	protected AbstractListener inicijalizirajListenera() {
		return new Listener(this, SERVER_PORT);
	}

	@Override
	protected void pokreniIgru() {
		super.pokreniIgru();
		serverIgraFuture = serverIgraExecutor.scheduleAtFixedRate(serverIgra, 0, KAŠNJENJE_IGRA, KAŠNJENJE_IGRA_TIMEUNIT);
	}

	/** Postavlja nove bodove. */
	void bodovi(final double bodovi) {
		for (final AbstractIgrač igrač : igrači) {
			((Igrač) igrač).setBodovi(Math.ceil(bodovi));
		}
	}

	final Loptica getLoptica() {
		return ((Igra) serverIgra).getLoptica();
	}

	final double getRadius() {
		return ((Igra) serverIgra).getRadius();
	}

	/**
	 * Main metoda. Kreira novu instancu Servera.
	 */
	public static void main(final String args[]) {
		new Server();
	}

}
