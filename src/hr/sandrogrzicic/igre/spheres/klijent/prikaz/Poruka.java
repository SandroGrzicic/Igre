package hr.sandrogrzicic.igre.spheres.klijent.prikaz;

import hr.sandrogrzicic.igre.spheres.klijent.Igrač;

import java.text.DateFormat;
import java.util.Date;


/**
 * Predstavlja jednu poruku. Immutable.
 */
public class Poruka {
	private final long timestamp;
	private final PorukaTip tip;
	private final Igrač izvor;
	private final String poruka;

	/**
	 * Konstruira novu poruku zadanog tipa. Parametar (Igrač) izvor se ne mijenja korištenjem ove klase.
	 */
	public Poruka(final long timestamp, final PorukaTip tip, final Igrač izvor, final String poruka) {
		this.timestamp = timestamp;
		this.tip = tip;
		this.izvor = izvor;
		this.poruka = poruka;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public PorukaTip getTip() {
		return tip;
	}

	public Igrač getIzvor() {
		return izvor;
	}

	public String get() {
		return poruka;
	}

	/**
	 * Vraća formatiranu poruku. Vrijeme se formatira po zadanog DateFormatu. Nije thread-safe.
	 */
	public String format(final DateFormat formatter) {
		final StringBuilder sb = new StringBuilder("[");
		sb.append(formatTimestamp(timestamp, formatter));
		sb.append("] <");
		sb.append(izvor.getIme());
		sb.append("> ");
		sb.append(poruka);
		return sb.toString();
	}

	/**
	 * Vraća formatirani timestamp generiran od zadanog vremena u milisekundama.
	 * 
	 * @param vrijemeMS
	 *            vrijeme u milisekundama.
	 * @return formatirani timestamp.
	 */
	private final String formatTimestamp(final long vrijemeMS, final DateFormat formatter) {
		return formatter.format(new Date(vrijemeMS));
	}

}
