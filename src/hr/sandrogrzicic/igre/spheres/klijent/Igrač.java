package hr.sandrogrzicic.igre.spheres.klijent;

import hr.sandrogrzicic.igre.spheres.objekti.Sfera;

public class Igrač {
	private int ID;
	private Sfera sfera;
	private String ime;

	public Igrač() {
	}

	public Igrač(final int ID, final Sfera sfera) {
		this.ID = ID;
		this.sfera = sfera;
	}

	public Igrač(final int ID, final Sfera sfera, final String ime) {
		this.ID = ID;
		this.sfera = sfera;
		this.ime = ime;
	}


	public int getID() {
		return ID;
	}

	public Sfera getSfera() {
		return sfera;
	}

	public String getIme() {
		return ime;
	}

	public void setIme(final String ime) {
		this.ime = ime;
	}

	public void setSfera(final Sfera sfera) {
		this.sfera = sfera;
	}

	public void setID(final int iD) {
		ID = iD;
	}

	@Override
	public String toString() {
		return "Igrač [ID=" + ID + ", ime=" + ime + ", sfera=" + sfera + "]";
	}

}
