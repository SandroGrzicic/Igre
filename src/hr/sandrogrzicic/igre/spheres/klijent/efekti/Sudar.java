package hr.sandrogrzicic.igre.spheres.klijent.efekti;

import java.awt.Color;
import java.awt.Graphics2D;

public class Sudar extends Efekt {

	private static final double FAKTOR_PRETVORBE_SUDARA = 100;
	private final int x;
	private final int y;
	private final int jačina;
	private final int bojaSfere;

	public Sudar(final double x, final double y, final double jačina, final Color bojaSfere) {
		this.x = (int) Math.round(x);
		this.y = (int) Math.round(y);
		this.jačina = (int) Math.round(FAKTOR_PRETVORBE_SUDARA * Math.sqrt(jačina));

		this.bojaSfere = bojaSfere.getRGB();
	}

	@Override
	public boolean iscrtaj(final Graphics2D g) {
		++iteracija;

		if (iteracija > jačina) {
			return false;
		}

		final int alpha = 224 - 223 * iteracija / jačina;
		g.setColor(new Color(bojaSfere + (alpha << 24), true));

		g.fillOval(x, (int) (y + iteracija * 0.75), iteracija, iteracija);
		g.fillOval(x, (int) (y - iteracija * 0.75), iteracija, iteracija);
		g.fillOval((int) (x - iteracija * 0.75), y, iteracija, iteracija);
		g.fillOval((int) (x + iteracija * 0.75), y, iteracija, iteracija);
		return true;
	}

}
