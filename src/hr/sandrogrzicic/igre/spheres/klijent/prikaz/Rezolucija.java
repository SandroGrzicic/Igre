package hr.sandrogrzicic.igre.spheres.klijent.prikaz;

import java.awt.Dimension;

/**
 * Definicije najčešćih rezolucija.
 * 
 * @author SeriousWorm
 */
public enum Rezolucija {
	r400(400, 400),
	r480(480, 480),
	r640(640, 640),
	r750(740, 740),
	r1000(1000, 1000),
	r1200(1200, 1200),
	r1600(1600, 1600),
	r0(0, 0);

	private int w;
	private int h;

	Rezolucija(final int w, final int h) {
		this.w = w;
		this.h = h;
	}

	@Override
	public String toString() {
		if ((w != 0) && (h != 0)) {
			return this.w + "x" + this.h;
		}
		return "Ručno odabrana";
	}

	public Dimension rezolucija() {
		return new Dimension(w, h);
	}
}
