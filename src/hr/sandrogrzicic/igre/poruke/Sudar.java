package hr.sandrogrzicic.igre.poruke;

import hr.sandrogrzicic.igre.server.AbstractIgrač;
import hr.sandrogrzicic.igre.spheres.Akcije;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Sudar extends Poruka {
	private final double x;

	private final double y;
	private final double jačina;

	private ByteArrayOutputStream baos;

	public Sudar(final AbstractIgrač izvor, final double x, final double y, final double jačina) {
		super(izvor);
		this.x = x;
		this.y = y;
		this.jačina = jačina;
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}

	public final double getJačina() {
		return jačina;
	}

	@Override
	public String toString() {
		return "Sudar jačine " + jačina + ", pos=(" + x + ", " + y + ")], [" + izvor + "]";
	}

	@Override
	public ByteArrayOutputStream getBAOS() {
		if (baos == null) {
			baos = new ByteArrayOutputStream(29);
			final DataOutputStream dos = new DataOutputStream(baos);

			try {
				dos.writeByte(Akcije.SUDAR.id());
				dos.writeInt(izvor.getID());
				dos.writeDouble(x);
				dos.writeDouble(y);
				dos.writeDouble(jačina);
			} catch (final IOException ignorable) {}
		}

		return baos;
	}

}
