package hr.sandrogrzicic.igre.spheres.objekti;

import java.awt.Color;

public class Sfera implements Cloneable {
	private static final double DEFAULT_RADIUS = 1;
	private static final Color	DEFAULT_COLOR	= Color.GRAY;

	private double x, y;
	double r;
	private Color boja;
	private boolean a;

	public Sfera() {
		this.boja = DEFAULT_COLOR;
		this.r = DEFAULT_RADIUS;
	}

	public Sfera(final Color boja) {
		this.boja = boja;
	}

	public Sfera(final Color boja, final double r) {
		this.boja = boja;
		this.r = r;
	}

	public Sfera(final Color boja, final double r, final double x, final double y, final boolean a) {
		this.boja = boja;
		this.r = r;
		this.x = x;
		this.y = y;
		this.a = a;
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}

	public final double getR() {
		return r;
	}

	public Color getBoja() {
		return this.boja;
	}
	public void setBoja(final Color boja) {
		this.boja = boja;
	}

	public final void setPos(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public final void setR(final double r) {
		this.r = r;
	}

	public final boolean getA() {
		return a;
	}


	public void setA(final boolean a) {
		this.a = a;
	}

	public void set(final double x, final double y, final double r, final boolean a) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.a = a;
	}

	@Override
	public String toString() {
		return "Sfera [a=" + a + ", r=" + r + ", x=" + x + ", y=" + y + "]";
	}


	/** Vraća kopiju ove sfere koja je neovisna o ovoj sferi. */
	@Override
	public Sfera clone() {
		return new Sfera(new Color(boja.getRGB(), true), r, x, y, a);
	}

}
