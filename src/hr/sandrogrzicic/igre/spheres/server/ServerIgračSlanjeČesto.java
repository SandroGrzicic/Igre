package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


class ServerIgračSlanjeČesto extends Thread {
	private boolean igračSpojen = true;
	private final UDP udp;
	private final Loptica loptica;
	private final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(17);
	private final DataOutputStream out = new DataOutputStream(outBAOS);
	private final ServerIgrač igrač;
	private final Sfera sfera;

	public ServerIgračSlanjeČesto(final ServerIgrač igrač, final UDP udp, final Loptica loptica) {
		this.setDaemon(true);
		this.setName(getClass().getCanonicalName() + igrač);

		this.igrač = igrač;
		this.udp = udp;
		this.loptica = loptica;
		this.sfera = igrač.getSfera();
	}

	@Override
	/** Glavna petlja koja diskretizirano često šalje važne podatke igraču. */
	public void run() {
		long vrijeme = System.currentTimeMillis();
		long vrijemeSpavanja;

		while (igračSpojen) {
			try {
				podaciČesti();
			} catch (final IOException e) {
				igrač.odspojen();
				break;
			}

			vrijemeSpavanja = Server.KAŠNJENJE_MREŽA_ČESTO - System.currentTimeMillis() + vrijeme;

			if (vrijemeSpavanja < 0) {
				vrijemeSpavanja = 2;
			}
			try {
				Thread.sleep(vrijemeSpavanja);
			} catch (final InterruptedException ignorable) {}
			vrijeme = System.currentTimeMillis();
		}
	}

	private void podaciČesti() throws IOException {
		out.writeByte(Akcije.PODACI_ČESTI.id());

		out.writeFloat((float) sfera.getR());

		out.writeFloat((float) loptica.getX());
		out.writeFloat((float) loptica.getY());
		out.writeFloat((float) loptica.getXv());
		out.writeFloat((float) loptica.getYv());
		udp.pošalji(outBAOS);

		outBAOS.reset();
	}

	/** Prekida izvođenje ove dretve. */
	void igračOdspojen() {
		igračSpojen = false;
	}

}
