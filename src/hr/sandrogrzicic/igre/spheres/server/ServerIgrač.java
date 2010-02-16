package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.exceptions.NemaSlobodnihPortovaException;
import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;
import hr.sandrogrzicic.igre.utility.UDP;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;


class ServerIgrač extends Thread {

	private static final int PAKET_MAX_VELIČINA = 1024;
	private static final int SOCKET_TIMEOUT = 5000;
	private static final int PAKET_VELIKI = 4096;
	private final int igračID;
	private final Server server;
	private String ime;
	private boolean unutarSfere;
	private DatagramSocket socket;
	private final SocketAddress adresa;
	private ServerIgračPrimanje primanje;
	private ServerIgračSlanjeRijetko slanjeRijetko;
	private ServerIgračSlanjeČesto slanjeČesto;
	private long bodovi;

	// dostupno mrežnim dretvama
	private final Sfera sfera;
	private UDP udp;

	/** Kreira novu dretvu koja će posluživati upravo spojenog klijeta. */
	public ServerIgrač(final DatagramSocket socket, final DatagramPacket paket, final Server server, final int igračID) {
		this.socket = socket;
		this.server = server;
		this.igračID = igračID;
		this.adresa = paket.getSocketAddress();
		this.sfera = new Sfera();
	}

	/**
	 * Šalje naredbu kojom se forsira stanje aktivnosti sfere igrača.
	 */
	public void postaviAktivnostSfere(final boolean aktivnost) throws IOException {
		sfera.setA(aktivnost);

		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(5);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.SFERA_AKTIVNA.id());
		out.writeBoolean(false);
		udp.pošalji(outBAOS);
	}

	/**
	 * Šalje informaciju o sudaru loptice i sfere.
	 * 
	 * @param x
	 *            koordinata sudara
	 * @param y
	 *            koordinata sudara
	 * @param y2
	 */
	void sudar(final int id, final double x, final double y, final double jačina) throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(9);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.SUDAR.id());
		out.writeInt(id);
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(jačina);
		udp.pošalji(outBAOS);
	}

	/**
	 * Šalje igraču novu chat poruku koju je poslao drugi igrač.
	 */
	public void chatPoruka(final int id, final long vrijemeUNIX, final String poruka) throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(10 + 3 * poruka.length());
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.CHAT_PORUKA.id());
		out.writeInt(id);
		out.writeLong(vrijemeUNIX);
		out.writeUTF(poruka);
		udp.pošalji(outBAOS);
	}

	/**
	 * Prosljeđuje primljenu chat poruku serveru.
	 */
	public void chatPrimljen(final long vrijemeUNIX, final String poruka) {
		server.chatPoruka(this, vrijemeUNIX, poruka);
	}

	void igračSpojen(final ServerIgrač spojen) throws IOException {
		if (spojen == this) {
			return;
		}
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(38 + 3 * spojen.getIme().length());
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.IGRAČ_SPOJEN.id());
		out.writeInt(spojen.getID());
		out.writeInt(spojen.getSfera().getBoja().getRGB());
		out.writeDouble(spojen.getSfera().getR());
		out.writeFloat((float) spojen.getSfera().getX());
		out.writeFloat((float) spojen.getSfera().getY());
		out.writeUTF(spojen.getIme());
		udp.pošalji(outBAOS);
	}

	void igračOdspojen(final ServerIgrač igrač) throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(8);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.IGRAČ_ODSPOJEN.id());
		out.writeInt(igrač.getID());
		udp.pošalji(outBAOS);
		System.out.println("[" + igračID + "] uspješno dobio poruku: Igrač [" + igrač.getID() + "] odspojen!");
	}

	@Override
	public void run() {
		Thread.currentThread().setName(getClass().getCanonicalName() + this);

		try {
			spojiIgrača();
		} catch (final NemaSlobodnihPortovaException e) {
			System.err.println("Nedovoljno slobodnih portova za spajanje klijenta #[" + igračID + "]!");
			assert (socket.getLocalPort() == Server.SERVER_PORT);
			return;
		} catch (final SocketTimeoutException ste) {
			System.err.println("Istekao timeout čekajući daljnje podatke od [" + adresa + "]");
			socket.close();
			return;
		} catch (final IOException io) {
			io.printStackTrace();
			return;
		}

		server.igračSpojen(this);

		synchronized (server) {
			server.getIgrači().add(this);
		}

		primanje = new ServerIgračPrimanje(this, udp, sfera);
		slanjeRijetko = new ServerIgračSlanjeRijetko(this, udp, server.getIgrači());
		slanjeČesto = new ServerIgračSlanjeČesto(this, udp, server.getLoptica());
		primanje.setDaemon(true);
		slanjeRijetko.setDaemon(true);
		slanjeČesto.setDaemon(true);
		// započni sa izvođenjem mrežnih dretvi.
		primanje.start();
		slanjeRijetko.start();
		slanjeČesto.start();
	}

	/** Inicijalizacija "veze" sa klijentom. */
	private void spojiIgrača() throws IOException {
		final DatagramSocket socketPrivatni = UDP.pronađiSlobodniPort(Server.SERVER_PORT, Server.SERVER_PORT_MAX);

		final byte[] portBytes = String.valueOf(socketPrivatni.getLocalPort()).getBytes();
		socket.send(new DatagramPacket(portBytes, portBytes.length, adresa));

		socket = socketPrivatni;
		socket.setSoTimeout(SOCKET_TIMEOUT);
		udp = new UDP(socket, PAKET_MAX_VELIČINA);

		razmijeniInicijalnePodatke();

		System.out.println("Igrač [" + igračID + "] [" + ime + "] uspješno spojen na port [" + socket.getLocalPort() + "]!");
	}

	/** Razmjenjuje inicijalne podatke sa klijentom. */
	private void razmijeniInicijalnePodatke() throws IOException {
		// port se možda promijenio
		final DatagramPacket paket = new DatagramPacket(new byte[PAKET_MAX_VELIČINA], PAKET_MAX_VELIČINA);
		final DataInputStream in = udp.primi(paket);

		socket.connect(paket.getSocketAddress());

		ime = in.readUTF();
		sfera.setBoja(new Color(in.readInt()));
		sfera.setR(server.getRadius());
		in.close();

		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(PAKET_VELIKI);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeInt(Server.VERZIJA);
		out.writeInt(igračID);

		out.writeDouble(server.getRadius());
		final Loptica l = server.getLoptica();
		out.writeDouble(l.getR());

		out.writeInt(server.getBrojIgrača());

		for (final ServerIgrač igrač : server.getIgrači()) {
			out.writeInt(igrač.getID());
			final Color boja = igrač.getSfera().getBoja();
			out.writeInt(boja.getRGB());
			out.writeDouble(igrač.getSfera().getR());
			out.writeFloat((float) igrač.getSfera().getX());
			out.writeFloat((float) igrač.getSfera().getY());
			out.writeUTF(igrač.getIme());
		}

		sfera.setPos(Math.random(), Math.random());
		out.writeFloat((float) sfera.getX());
		out.writeFloat((float) sfera.getY());
		out.writeDouble(sfera.getR());

		udp.pošalji(outBAOS);
	}

	int getID() {
		return igračID;
	}

	Sfera getSfera() {
		return sfera;
	}

	final String getIme() {
		return ime;
	}

	void setUnutarSfere(final boolean unutarSfere) {
		this.unutarSfere = unutarSfere;
	}

	boolean getUnutarSfere() {
		return unutarSfere;
	}

	/** Vraća trenutnu lokalnu kopiju bodova. */
	long getBodovi() {
		return bodovi;
	}
	/** Postavlja lokalnu kopiju bodova na zadanu vrijednost. */
	void setBodovi(final long bodoviNovi) {
		this.bodovi = bodoviNovi;
	}

	/** Postavlja lokalnu kopiju bodova na zadanu vrijednost. */
	void setBodovi(final double bodoviNovi) {
		this.bodovi = (long) bodoviNovi;
	}

	/** Javlja serveru i mrežnim dretvama da je ovaj igrač odspojen. */
	void odspojen() {
		server.igračOdspojen(this);
		primanje.igračOdspojen();
		slanjeRijetko.igračOdspojen();
		slanjeČesto.igračOdspojen();
		// DEBUG
		// System.exit(0);
	}

	@Override
	public String toString() {
		return "[ID: " + igračID + "] [ime: " + ime + "]";
	}

	/** Vraća DatagramSocket preko kojega je ova dretva spojena sa igračem. */
	public DatagramSocket getSocket() {
		return udp.getSocket();
	}

}