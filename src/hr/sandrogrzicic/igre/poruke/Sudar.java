package hr.sandrogrzicic.igre.poruke;

import hr.sandrogrzicic.igre.server.AbstractIgrač;

public class Sudar extends Poruka {
	private final double x;
	private final double y;
	private final double jačina;

	public Sudar(final AbstractIgrač izvor, final double x, final double y, final double jačina) {
		super(izvor);
		this.x = x;
		this.y = y;
		this.jačina = jačina;
	}

	@Override
	public String toString() {
		return "Sudar jačine " + jačina + ", pos=(" + x + ", " + y + ")], [" + izvor + "]";
	}

}
