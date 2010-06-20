package hr.sandrogrzicic.igre.spheres.klijent;

import hr.sandrogrzicic.igre.klijent.AbstractKlijent;
import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.klijent.mreža.MrežaPrimanje;
import hr.sandrogrzicic.igre.spheres.klijent.mreža.MrežaSlanjeSpheres;
import hr.sandrogrzicic.igre.spheres.klijent.prikaz.Prikaz;
import hr.sandrogrzicic.igre.spheres.klijent.prikaz.PrikazSpheres;
import hr.sandrogrzicic.igre.spheres.klijent.prikaz.PrikazSpheresJFrame;
import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;



/**
 * Klijent za igru Serious Spheres. Kontroler.
 * 
 * @author SeriousWorm
 */
public class Klijent extends AbstractKlijent {
	private final Loptica loptica;
	private final Sfera sfera;
	private PrikazSpheres prikaz;

	public Klijent(final String hostname, final int port) {
		super(hostname, port);
		loptica = new Loptica();
		sfera = new Sfera();
		igrač.setSfera(sfera);
		igraAktivna = true;
	}

	@Override
	protected void pokreni() {
		// stvara prikaz; svi bitni parametri (npr. ime igrača) su postavljeni nakon ove naredbe.
		this.prikaz = new PrikazSpheresJFrame(this, igrač);
		// TODO: ukloniti "glavni" prikaz
		this.prikazGlavni = prikaz;

		spojiSeNaServer();

		new Thread(prikazGlavni).start();

		mrežaPrimanje = new MrežaPrimanje(this, udp);
		mrežaSlanje = new MrežaSlanjeSpheres(this, udp);
		mrežaPrimanje.start();
		mrežaSlanje.start();
		igraAktivna(true);

	}

	/**
	 * Spajanje na server te izmjena inicijalnih podataka vezanih uz igru.
	 */
	@Override
	protected void initConnection() throws IOException {
		assert (igrač.getIme() != null) : "Ime igrača nije postavljeno!";

		super.initConnection();

		// pošalji korisničke podatke (ime, boja sfere)
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(1024);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeUTF(igrač.getIme());
		out.writeInt(igrač.getSfera().getBoja().getRGB());
		udp.pošalji(outBAOS);

		// primi podatke od servera
		final DataInputStream in = udp.primi();

		final int verzijaServer = in.readInt();
		if (VERZIJA != verzijaServer) {
			prikazGlavni.prikažiGrešku("Nekompatibilna verzija (klijent: [" + VERZIJA + "], server: [" + verzijaServer + "])!", null);
			System.exit(5);
		}
		igrač.setID(in.readInt());

		radiusMax = in.readDouble();
		loptica.setR(in.readDouble());

		igrači.clear();

		final int brojIgrača = in.readInt();
		for (int i = 0; i < brojIgrača; i++) {
			final int id = in.readInt();
			igrači.put(id, new Igrač(id, new Sfera(new Color(in.readInt()),
					in.readFloat(), in.readFloat(), in.readFloat(), false), in.readUTF()));
		}

		igrači.put(igrač.getID(), igrač);

		igrač.getSfera().set(0.5, 0.5, in.readDouble(), false);

		((PrikazSpheres) prikazGlavni).setInit(radiusMax);

	}


	/**
	 * Pokreće klijent sa defaultnim postavkama.
	 */
	public static void main(final String[] args) {
		// final String host = "sworm.no-ip.com";
		final String host = "192.168.0.7";
		final int port = 7710;
		new Klijent(host, port).pokreni();
	}


	@SuppressWarnings("incomplete-switch")
	@Override
	public void onPrimljenPaket(final DataInputStream paket) throws IOException {
		paket.mark(paket.available());

		for (final Prikaz p : prikazi) {
			p.onPrimljenPaket(paket);
			paket.reset();
		}

		final int id;
		switch (Akcije.get(paket.readByte())) {
		case IGRAČ_SPOJEN:
			id = paket.readInt();
			igrači.put(id, new Igrač(id, new Sfera(new Color(paket.readInt()),
					paket.readFloat(), paket.readFloat(), paket.readFloat(), false), paket.readUTF()));
			// System.err.println("DEBUG: Spojen igrač [" + id + "]! Broj igrača: [" + igrači.size() + "]");
			break;
		case IGRAČ_ODSPOJEN:
			id = paket.readInt();
			igrači.remove(id);
			// System.err.println("DEBUG: Odspojen igrač [" + id + "]! Broj igrača: [" + igrači.size() + "]");
			break;
		}
	}

	public Loptica getLoptica() {
		return loptica;
	}

	public Sfera getSfera() {
		return sfera;
	}

}
