package hr.sandrogrzicic.igre.spheres.klijent.mreža;

import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.klijent.Klijent;
import hr.sandrogrzicic.igre.spheres.klijent.KlijentSpheres;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class MrežaSlanjeSpheres extends MrežaSlanje {
	protected KlijentSpheres klijentSpheres;
	private final Sfera sfera;

	public MrežaSlanjeSpheres(final Klijent klijent, final UDP udp) {
		super(klijent, udp);
		this.klijentSpheres = (KlijentSpheres) klijent;
		this.sfera = klijentSpheres.getSfera();
	}

	/** Šalje podatke o koordinatama igračeve sfere serveru. Ne blocka. */
	@Override
	protected void mrežaPošalji() throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(14);
		final DataOutputStream out = new DataOutputStream(outBAOS);
		out.writeByte(Akcije.POMAK_SFERE.id());
		out.writeFloat((float) sfera.getX());
		out.writeFloat((float) sfera.getY());
		out.writeBoolean(sfera.getA());
		udp.pošalji(outBAOS);
	}

}
