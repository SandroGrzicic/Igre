package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.spheres.objekti.Loptica;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Glavni server za igru.
 * 
 * @author Sandro Gržičić
 */
public final class Server {
	public static final int VERZIJA = 21;
	static final long KAŠNJENJE_MREŽA_RIJETKO = 100;
	static final long KAŠNJENJE_MREŽA_ČESTO = 25;
	static final long KAŠNJENJE_IGRA = 100;
	static final TimeUnit KAŠNJENJE_IGRA_TIMEUNIT = TimeUnit.MICROSECONDS;
	static final int SERVER_PORT = 7710;
	static final int SERVER_PORT_MAX = 7750;
	static final int SERVER_MAX_IGRAČA = 16;
	/** Maksimalan broj konekcija s jednog IP-a */
	static final Short MAKSIMALAN_BROJ_KLONOVA = 8;

	private final List<ServerIgrač> igrači = new CopyOnWriteArrayList<ServerIgrač>();
	private final ServerListener serverListener;
	private final ServerIgra serverIgra;

	/**
	 * Glavni poslužitelj igre. Osluškuje zahtjeve za spajanjem te kreira nove dretve koje upravljaju vezama prema igračima.
	 */
	public Server() {
		serverListener = new ServerListener(this, SERVER_PORT);
		try {
			serverListener.bind();
		} catch (final SocketException e) {
			System.err.println("Port [" + SERVER_PORT + "] je zauzet!");
			System.exit(1);
		}

		serverIgra = new ServerIgra(this, igrači);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(serverIgra, 0, KAŠNJENJE_IGRA, KAŠNJENJE_IGRA_TIMEUNIT);

		Executors.newSingleThreadExecutor().execute(serverListener);

		System.out.println("INFO: Server pokrenut na portu [" + SERVER_PORT + "].");
	}

	/**
	 * Šalje svim igračima podatke o sudaru loptice i sfere.
	 * 
	 * @param id igrača koji je izvor sudara
	 * @param x koordinata sudara
	 * @param y koordinata sudara
	 * @param jačina sudara
	 */
	void sudar(final int id, final double x, final double y, final double jačina) {
		for (final ServerIgrač igrač : igrači) {
			try {
				igrač.sudar(id, x, y, jačina);
			} catch (final IOException e) {
				igračOdspojen(igrač);
			}
		}
	}

	/** Postavlja nove bodove. */
	void bodovi(final double bodovi) {
		for (final ServerIgrač igrač : igrači) {
			igrač.setBodovi(Math.ceil(bodovi));
		}
	}

	/**
	 * Javlja igračima da je stigla nova chat poruka.
	 */
	void chatPoruka(final ServerIgrač igračIzvor, final long vrijemeUNIX, final String poruka) {
		System.out.println("* Chat [" + vrijemeUNIX + "] [" + igračIzvor.getID() + "]: [" + poruka + "]");
		for (final ServerIgrač igrač : igrači) {
			if (igrač.getID() == igračIzvor.getID()) {
				continue;
			}
			try {
				igrač.chatPoruka(igračIzvor.getID(), vrijemeUNIX, poruka);
			} catch (final IOException e) {
				igračOdspojen(igrač);
			}
		}
	}

	/** Javlja igračima da je novi igrač spojen. */
	void igračSpojen(final ServerIgrač spojen) {
		setIgraPokrenuta(true);

		for (final ServerIgrač igrač : igrači) {
			try {
				igrač.igračSpojen(spojen);
			} catch (final IOException e) {
				igračOdspojen(igrač);
			}
		}
	}

	/** Javlja igračima da je neki igrač odspojen. */
	void igračOdspojen(final ServerIgrač odspojen) {
		System.out.println("- Igrač [" + odspojen.getID() + "] [" + odspojen.getIme() + "] odspojen!");
		igrači.remove(odspojen);

		serverListener.klijentOdspojen(odspojen.getSocket().getInetAddress());

		if (igrači.size() == 0) {
			setIgraPokrenuta(false);
		}

		for (final ServerIgrač igrač : igrači) {
			try {
				igrač.igračOdspojen(odspojen);
			} catch (final IOException ignorable) {
				// DEBUG
				assert (false) : "Igrač odspojen dok je primao igračOdspojen poruku!";

				// igračOdspojen(igrač.getID());
			}
		}
	}

	final Loptica getLoptica() {
		return serverIgra.getLoptica();
	}

	final double getRadius() {
		return serverIgra.getRadius();
	}

	final int getBrojIgrača() {
		return igrači.size();
	}

	final List<ServerIgrač> getIgrači() {
		return igrači;
	}

	final boolean isIgraPokrenuta() {
		return serverIgra.isIgraPokrenuta();
	}

	synchronized public void setIgraPokrenuta(final boolean igraPokrenuta) {
		serverIgra.setIgraPokrenuta(igraPokrenuta);
	}

	/**
	 * Main metoda. Kreira novu instancu Servera.
	 */
	public static void main(final String args[]) {
		new Server();
	}

}
