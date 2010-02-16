package hr.sandrogrzicic.igre.spheres.klijent.efekti;

import java.awt.Graphics2D;

/**
 * Predstavlja grafički efekt koji se može iscrtati.
 * 
 * @author SeriousWorm
 */
public abstract class Efekt {
	protected int iteracija = 0;

	/**
	 * Iscrtava efekt.
	 * @param g
	 *            grafički objekt na kojem će se obaviti crtanje.
	 * @return false ukoliko je efekt gotov.
	 */
	public abstract boolean iscrtaj(final Graphics2D g);

}
