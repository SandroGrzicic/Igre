package hr.sandrogrzicic.igre.spheres.objekti;


public class Loptica {
	private static final double DEFAULT_RADIUS = 0.01;
	private double r, x, y, xv, yv;

	public Loptica() {
		this.r = DEFAULT_RADIUS;
	}

	public Loptica(final double r) {
		this.r = r;
	}

	public Loptica(final double r, final double x, final double y, final double xv, final double yv) {
		this.r = r;
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
	}


	public final double getXv() {
		return xv;
	}

	public final void setXv(final double xv) {
		this.xv = xv;
	}

	public final double getYv() {
		return yv;
	}

	public final void setYv(final double yv) {
		this.yv = yv;
	}

	public final double getR() {
		return r;
	}

	public final void setR(final double r) {
		this.r = r;
	}

	public final double getX() {
		return x;
	}

	public final void setX(final double x) {
		this.x = x;
	}

	public final double getY() {
		return y;
	}

	public final void setY(final double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Loptica [r=" + r + ", x=" + x + ", xv=" + xv + ", y=" + y + ", yv=" + yv + "]";
	}

	public void set(final double x, final double y, final double xv, final double yv) {
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
	}

	public void setPos(final double x, final double y) {
		this.x = x;
		this.y = y;
	}


}
