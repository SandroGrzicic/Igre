package hr.sandrogrzicic.igre.spheres.klijent.prikaz;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Bazna klasa za sve Prikaze.
 * 
 * @author Sandro Gržičić
 */
public interface Prikaz extends Runnable {

	/** Prikazuje zadanu poruku korisniku. */
	void prikažiTekst(final String poruka, final String naslov);

	/** Generira upit korisniku. Vraća korisnikov odgovor. */
	boolean prikažiUpit(final String poruka, final String naslov);

	/** Javlja korisniku da je došlo do pogreške. */
	void prikažiGrešku(final String poruka, final Exception e);

	/** Osvježi lokalne kopije postavki. */
	public void promjenaPostavki();

	void setBodovi(final long bodovi);

	void sudar(final int id, final double x, final double y, final double jačina);

	/** Prima te obrađuje dolazni paket s podacima. */
	void onPrimljenPaket(DataInputStream paket) throws IOException;

	void setIgraAktivna(boolean igraAktivna);

}
