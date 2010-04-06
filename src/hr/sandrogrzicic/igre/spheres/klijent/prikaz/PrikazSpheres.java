package hr.sandrogrzicic.igre.spheres.klijent.prikaz;

/**
 * Sučelje za SeriousSpheres Prikaze Klijenta igre.
 * 
 * @author Sandro Gržičić
 */
public interface PrikazSpheres extends Prikaz {

	/**
	 * Inicijalizira ovaj Prikaz.
	 * 
	 * @param radiusMax
	 *            najveći dopušteni radijus sfere.
	 */
	void setInit(double radiusMax);

}
