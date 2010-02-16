package hr.sandrogrzicic.igre.spheres.klijent.prikaz;


/**
 * Bazna klasa za SeriousSpheres Prikaze Klijenta igre.
 * 
 * @author Sandro Gržičić
 */
public interface PrikazSpheres extends Prikaz {

	/**
	 * Inicijalizira ovaj Prikaz.
	 * 
	 * @param id
	 *            igrača.
	 * @param radiusMax
	 *            najveći dopušteni radijus sfere.
	 */
	void setInit(double radiusMax);

}
