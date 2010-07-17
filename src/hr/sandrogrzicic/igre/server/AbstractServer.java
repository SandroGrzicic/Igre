package hr.sandrogrzicic.igre.server;


import hr.sandrogrzicic.igre.poruke.Poruka;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Glavni server za igru.
 * 
 * @author Sandro Gržičić
 */
public abstract class AbstractServer {
	static final int SERVER_PORT = 7710;
	static final int SERVER_PORT_MAX = 7750;
	static final int SERVER_MAX_IGRAČA = 16;

	/** Maksimalan broj konekcija s jednog IP-a */
	static final Short MAKSIMALAN_BROJ_KLONOVA = 8;

	protected final AbstractListener listener;
	protected List<AbstractIgrač> igrači = new CopyOnWriteArrayList<AbstractIgrač>();

	protected AbstractIgra serverIgra;
	protected ScheduledExecutorService serverIgraExecutor;
	protected ScheduledFuture<?> serverIgraFuture;
	protected boolean igraPokrenuta;

	/**
	 * Glavni poslužitelj igre. Osluškuje zahtjeve za spajanjem te kreira nove dretve koje upravljaju vezama prema igračima.
	 */
	public AbstractServer() {
		listener = inicijalizirajListenera();

		try {
			listener.bind();
		} catch (final SocketException e) {
			System.err.println("Port [" + SERVER_PORT + "] je zauzet!");
			System.exit(1);
		}

		serverIgraExecutor = inicijalizajExecutora();
		serverIgra = inicijalizirajIgru();
		// pokreniIgru(); // igra se pokreće kada se spoji prvi igrač

		Executors.newSingleThreadExecutor().execute(listener);

		System.out.println("INFO: Server pokrenut na portu [" + SERVER_PORT + "].");
	}

	/** Inicijalizara Executora koji će se koristiti za izvršavanje logike igre. */
	protected abstract ScheduledExecutorService inicijalizajExecutora();

	/** Inicijalizira logiku igre. */
	protected abstract AbstractIgra inicijalizirajIgru();

	/** Inicijalizira listenera za konekcije na server. */
	protected abstract AbstractListener inicijalizirajListenera();

	/** Pokreće izvršavanje igre. */
	protected void pokreniIgru() {
		igrači.clear();
		serverIgra.pokreni();
		igraPokrenuta = true;
	}

	/** Zaustavlja izvršavanje igre nakon što se izvrši trenutna iteracija ili odmah. */
	protected void zaustaviIgru(final boolean odmah) {
		serverIgra.zaustavi();
		serverIgraFuture.cancel(odmah);
		igraPokrenuta = false;
	}

	/**
	 * Šalje generičku poruku svim igračima.
	 */
	public void poruka(final Poruka poruka) {
		System.out.println("* " + poruka);
		for (final AbstractIgrač igrač : igrači) {
			try {
				igrač.poruka(poruka);
			} catch (final IOException io) {
				igračOdspojen(igrač);
			}
		}
	}

	/** Javlja igračima da je novi igrač spojen. */
	public void igračSpojen(final AbstractIgrač spojen) {
		if (igrači.size() == 0) {
			setIgraPokrenuta(true);
		}

		for (final AbstractIgrač igrač : igrači) {
			try {
				igrač.igračSpojen(spojen);
			} catch (final IOException e) {
				igrač.odspojen();
			}
		}

		igrači.add(spojen);
	}

	/** Javlja igračima da je neki igrač odspojen. */
	public void igračOdspojen(final AbstractIgrač odspojen) {
		System.out.println("- " + odspojen + " odspojen!");

		igrači.remove(odspojen);

		listener.klijentOdspojen(odspojen.getSocket().getInetAddress());

		if (igrači.size() == 0) {
			setIgraPokrenuta(false);
		}

		for (final AbstractIgrač igrač : igrači) {
			try {
				igrač.igračOdspojen(odspojen);
			} catch (final IOException e) {
				igrač.odspojen();
			}
		}
	}

	public final int getBrojIgrača() {
		return igrači.size();
	}

	public List<AbstractIgrač> getIgrači() {
		return igrači;
	}

	/** Zaustavlja ili pokreće igru. */
	synchronized public void setIgraPokrenuta(final boolean igraPokrenuta) {
		if (this.igraPokrenuta && !igraPokrenuta) {
			zaustaviIgru(false);
			System.out.println("Igra zaustavljena!");
		} else if (!this.igraPokrenuta && igraPokrenuta) {
			pokreniIgru();
			System.out.println("Igra pokrenuta!");
		}
	}

	public boolean isIgraPokrenuta() {
		return igraPokrenuta;
	}

}
