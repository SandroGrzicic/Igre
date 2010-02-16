package hr.sandrogrzicic.igre.spheres;


/**
 * @author Sandro Gržičić
 *
 */
public enum Akcije {
	POMAK_LOPTICE(0),
	POMAK_SFERE(1),
	IGRAČ_SPOJEN(2),
	IGRAČ_ODSPOJEN(3),
	IGRAČ_IZAŠAO(4),
	SFERA_AKTIVNA(5),
	CHAT_PORUKA(6),
	SERVER_UGASI(7),
	PODACI_NISKI_PRIORITET(8),
	SUDAR(9);

	private byte id;

	Akcije(final int akcijaID) {

		this.id = (byte) akcijaID;
	}


	public byte id() {
		return id;
	}

	/**
	 * Vraća enum sa zadanim identifikatorom.
	 */
	public static Akcije get(final byte identifikator) {
		return values()[identifikator];
	}
}