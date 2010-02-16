package hr.sandrogrzicic.igre.spheres.klijent;

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
public class KlijentSpheres extends Klijent {
	private final Loptica loptica;
	private final Sfera sfera;
	private PrikazSpheres prikazSpheres;

	public KlijentSpheres() {
		super();
		loptica = new Loptica();
		sfera = new Sfera();
		igrač.setSfera(sfera);
		igraAktivna = true;

	}

	@Override
	protected void pokreni(final String[] argumenti) {
		// stvara prikaz; svi bitni parametri (npr. ime igrača) su postavljeni nakon ove naredbe.
		this.prikazSpheres = new PrikazSpheresJFrame(this, igrač, argumenti);
		this.prikazGlavni = prikazSpheres;

		spojiSeNaServer();

		new Thread(prikazGlavni).start();

		mrežaPrimanje = new MrežaPrimanje(this, udp);
		mrežaSlanje = new MrežaSlanjeSpheres(this, udp);
		mrežaPrimanje.start();
		mrežaSlanje.start();

	}

	/** Spajanje na server te izmjena inicijalnih podataka vezanih uz igru. */
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

		getIgrači().clear();

		final int brojIgrača = in.readInt();
		for (int i = 0; i < brojIgrača; i++) {
			final int id = in.readInt();
			igrači.put(id, new Igrač(id, new Sfera(new Color(in.readInt()),
					in.readDouble(), in.readFloat(), in.readFloat(), false), in.readUTF()));
		}

		getIgrači().put(igrač.getID(), igrač);

		igrač.getSfera().set(in.readFloat(), in.readFloat(), in.readDouble(), false);

		((PrikazSpheres) prikazGlavni).setInit(radiusMax);

	}


	/**
	 * Pokreće klijent sa defaultnim postavkama.
	 */
	public static void main(final String[] args) {
		new KlijentSpheres().pokreni(args);
	}


	@SuppressWarnings("incomplete-switch")
	@Override
	public void primljenPaket(final DataInputStream paket) throws IOException {
		paket.mark(paket.available());

		for (final Prikaz p : prikazi) {
			p.primljenPaket(paket);
			paket.reset();
		}

		int id;
		switch (Akcije.get(paket.readByte())) {
		case IGRAČ_SPOJEN:
			id = paket.readInt();
			synchronized (igrači) {
				igrači.put(id, new Igrač(id, new Sfera(new Color(paket.readInt()),
						paket.readDouble(), paket.readFloat(), paket.readFloat(), false), paket.readUTF()));
			}
			System.err.println("DEBUG: Spojen igrač [" + id + "]! Broj igrača: [" + igrači.size() + "]");
			break;
		case IGRAČ_ODSPOJEN:
			id = paket.readInt();
			synchronized (igrači) {
				igrači.remove(id);
			}
			System.err.println("DEBUG: Odspojen igrač [" + id + "]! Broj igrača: [" + igrači.size() + "]");
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