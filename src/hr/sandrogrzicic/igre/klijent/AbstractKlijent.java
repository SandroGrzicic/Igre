package hr.sandrogrzicic.igre.klijent;

import hr.sandrogrzicic.igre.exceptions.NevaljaniPaketException;
import hr.sandrogrzicic.igre.spheres.klijent.Igrač;
import hr.sandrogrzicic.igre.spheres.klijent.Postavke;
import hr.sandrogrzicic.igre.spheres.klijent.mreža.MrežaPrimanje;
import hr.sandrogrzicic.igre.spheres.klijent.mreža.MrežaSlanje;
import hr.sandrogrzicic.igre.spheres.klijent.prikaz.Prikaz;
import hr.sandrogrzicic.igre.spheres.server.Server;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Klijent za igru. Kontroler u MVC paradigmi.
 * 
 * @author Sandro Gržičić
 */
public abstract class AbstractKlijent {
	public static final int VERZIJA = Server.VERZIJA;
	private static final byte[] CONNECTION_STRING = "Bok!".getBytes();
	private static final int PAKET_MAX_VELIČINA = 1024;
	private static final int SOCKET_TIMEOUT = 5000;
	protected boolean igraAktivna = false;
	protected final Igrač igrač;
	protected final Map<Integer, Igrač> igrači;
	protected double radiusMax;
	private SocketAddress adresa;
	private InetSocketAddress adresaServera;
	protected final EnumMap<Postavke, Object> postavke;
	protected final UDP udp;

	protected MrežaPrimanje mrežaPrimanje;
	protected MrežaSlanje mrežaSlanje;
	protected Prikaz prikazGlavni;

	/**
	 * Kreira novi Klijent. Stvara i pokreće dretve za slanje i primanje mrežnih podataka te prikaznu dretvu.
	 */
	public AbstractKlijent() {
		// this.adresaServera = new InetSocketAddress(hostname, port);
		this.udp = new UDP(PAKET_MAX_VELIČINA);
		this.igrač = new Igrač();
		this.igrači = new ConcurrentHashMap<Integer, Igrač>(16, 0.5f, 1);
		this.postavke = new EnumMap<Postavke, Object>(Postavke.class);
	}

	/** Pokreće klijent. Prikaz i mreža se aktiviraju. */
	protected abstract void pokreni();

	/** Pokušava se spojiti na server. Ukoliko je spajanje neuspješno, generira upit za ponovnim spajanjem. */
	protected void spojiSeNaServer() {
		try {
			initConnection();
		} catch (final IOException e) {
			if (prikazGlavni.prikažiUpit("Neuspješno spajanje na server.\nPokušati ponovo?", "Neuspješno spajanje")) {
				spojiSeNaServer();
			} else {
				System.exit(1);
			}
		}
	}

	/**
	 * Spajanje na server.
	 */
	protected void initConnection() throws IOException {
		// inicijalni socket za spajanje na server (listen port)
		adresaServera = new InetSocketAddress((String) postavke.get(Postavke.SERVER_HOSTNAME), (Integer) postavke.get(Postavke.SERVER_PORT));
		udp.otvoriSocket();
		udp.setTimeout(SOCKET_TIMEOUT);
		// "Bok!" paket serveru
		udp.send(new DatagramPacket(CONNECTION_STRING, 4, adresaServera));
		// primi pravi port servera
		final DatagramPacket paket = new DatagramPacket(new byte[PAKET_MAX_VELIČINA], PAKET_MAX_VELIČINA);
		udp.receive(paket);
		final int port;
		try {
			port = Integer.parseInt(new String(paket.getData(), 0, paket.getLength()));
		} catch (final NumberFormatException nfe) {
			throw new NevaljaniPaketException(paket.getData(), paket.getLength());
		}
		// spoji se na pravi port
		adresa = new InetSocketAddress(paket.getAddress(), port);
		udp.connect(adresa);
	}

	public void izgubljenaVeza() {
		igraAktivna(false);
		if (prikazGlavni.prikažiUpit("Izgubljena veza sa serverom.\n Spojiti se ponovo?", "Izgubljena veza sa serverom")) {
			spojiSeNaServer();
			igraAktivna(true);
		} else {
			System.exit(2);
		}
	}

	/** Aktivira ili deaktivira izvođenje igre. */
	protected void igraAktivna(final boolean novoStanje) {
		mrežaPrimanje.setIgraAktivna(novoStanje);
		mrežaSlanje.setIgraAktivna(novoStanje);
		igraAktivna = novoStanje;
		prikazGlavni.setIgraAktivna(novoStanje);
	}

	public boolean isIgraAktivna() {
		return igraAktivna;
	}


	/** Vraća zadanu postavku. */
	public Object getPostavka(final Postavke postavka) {
		return postavke.get(postavka);
	}

	/** Postavlja postavku na zadanu vrijednost. Null vrijednosti nisu dozvoljene. */
	public void setPostavka(final Postavke postavka, final Object vrijednost) {
		if (vrijednost == null) {
			throw new IllegalArgumentException("Postavka ne smije biti null!");
		}
		postavke.put(postavka, vrijednost);
	}

	void setBodovi(final long bodovi) {
		prikazGlavni.setBodovi(bodovi);
	}

	public final void chatPošalji(final String poruka) {
		try {
			mrežaSlanje.pošaljiPoruku(poruka);
		} catch (final IOException io) {
			izgubljenaVeza();
		}
	}

	public Map<Integer, Igrač> getIgrači() {
		return igrači;
	}

	/** Gasi igru i JVM; prije gašenja šalje paket serveru da je igrač izašao ukoliko je to moguće. */
	public void ugasiIgru() {
		if (mrežaSlanje != null) {
			mrežaSlanje.klijentIzašao();
		}
		System.exit(0);
	}

	/** Zove se kada je primljen paket. Izvedene klase mogu procesirati dobiveni paket. */
	public abstract void onPrimljenPaket(DataInputStream paket) throws IOException;

}