package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.poruke.Chat;
import hr.sandrogrzicic.igre.poruke.Poruka;
import hr.sandrogrzicic.igre.server.AbstractIgrač;
import hr.sandrogrzicic.igre.server.AbstractIgračMreža;
import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


class Igrač extends AbstractIgrač {

	protected static int PAKET_MAX_VELIČINA = 1024;
	protected static int SOCKET_TIMEOUT = 5000;
	protected static int PAKET_VELIKI = 4096;
	protected String ime;
	protected boolean unutarSfere;
	protected long bodovi;

	// dostupno mrežnim dretvama
	private final Sfera sfera;

	/** Kreira novu dretvu koja će posluživati upravo spojenog klijeta. */
	public Igrač(final DatagramSocket socket, final DatagramPacket paket, final Server server, final int igračID) {
		super(socket, paket, server, igračID);
		this.sfera = new Sfera();
	}

	/**
	 * Šalje naredbu kojom se forsira stanje aktivnosti sfere igrača.
	 */
	void postaviAktivnostSfere(final boolean aktivnost) throws IOException {
		sfera.setA(aktivnost);

		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(5);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.SFERA_AKTIVNA.id());
		out.writeBoolean(false);
		veza.pošalji(outBAOS);
	}

	/**
	 * Šalje informaciju o sudaru loptice i sfere.
	 * 
	 * @param id
	 *            id igrača
	 * @param x
	 *            koordinata sudara
	 * @param y
	 *            koordinata sudara
	 * @param jačina
	 */
	void sudar(final int id, final double x, final double y, final double jačina) throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(9);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.SUDAR.id());
		out.writeInt(id);
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(jačina);
		veza.pošalji(outBAOS);
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
		veza.pošalji(outBAOS);
	}

	/**
	 * Prosljeđuje primljenu chat poruku serveru.
	 */
	protected void chatPrimljen(final long vrijemeUNIX, final String tekstPoruke) {
		server.poruka(new Chat(this, vrijemeUNIX, tekstPoruke));
	}

	@Override
	protected void igračSpojen(final AbstractIgrač spojenAbstract) throws IOException {
		final Igrač spojen = (Igrač) spojenAbstract;
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(21 + 3 * spojen.getIme().length());
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.IGRAČ_SPOJEN.id());

		out.writeInt(spojen.getID());
		out.writeInt(spojen.getSfera().getBoja().getRGB());
		out.writeFloat((float) spojen.getSfera().getR());
		out.writeFloat((float) spojen.getSfera().getX());
		out.writeFloat((float) spojen.getSfera().getY());
		out.writeUTF(spojen.getIme());
		veza.pošalji(outBAOS);
		System.out.println("[" + igračID + "] uspješno dobio poruku: Igrač [" + spojen.getID() + "] spojen!");
	}

	@Override
	protected void igračOdspojen(final AbstractIgrač odspojen) throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(5);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.IGRAČ_ODSPOJEN.id());

		out.writeInt(odspojen.getID());
		veza.pošalji(outBAOS);
		// System.out.println("[" + igračID + "] uspješno dobio poruku: Igrač [" + odspojen.getID() + "] odspojen!");
	}

	@Override
	protected AbstractIgračMreža inicijalizirajMrežniPodsustav(final AbstractIgrač abstractIgrač) {
		return new IgračMreža(this, veza, server);
	}


	/** Razmjenjuje inicijalne podatke sa klijentom. */
	@Override
	protected void razmijeniInicijalnePodatke() throws IOException {
		final Server _server = (Server) server;
		// port se možda promijenio
		final DatagramPacket paket = new DatagramPacket(new byte[PAKET_MAX_VELIČINA], PAKET_MAX_VELIČINA);
		final DataInputStream in = veza.primi(paket);

		socket.connect(paket.getSocketAddress());

		ime = in.readUTF();
		sfera.setBoja(new Color(in.readInt()));
		sfera.setR(_server.getRadius());
		in.close();

		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(PAKET_VELIKI);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeInt(Server.VERZIJA);
		out.writeInt(igračID);

		out.writeDouble(_server.getRadius());
		final Loptica l = _server.getLoptica();
		out.writeDouble(l.getR());

		out.writeInt(server.getBrojIgrača());
		for (final AbstractIgrač igračAbstract : server.getIgrači()) {
			final Igrač igrač = (Igrač) igračAbstract;

			out.writeInt(igrač.getID());
			final Color boja = igrač.getSfera().getBoja();
			out.writeInt(boja.getRGB());
			out.writeFloat((float) igrač.getSfera().getR());
			out.writeFloat((float) igrač.getSfera().getX());
			out.writeFloat((float) igrač.getSfera().getY());
			out.writeUTF(igrač.getIme());
		}

		sfera.setPos(0.5, 0.5);
		out.writeDouble(sfera.getR());

		veza.pošalji(outBAOS);
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


	@Override
	public String toString() {
		return "[" + igračID + "] [" + ime + "]";
	}

	@Override
	protected void poruka(final Poruka poruka) {
		// TODO Auto-generated method stub

	}


}