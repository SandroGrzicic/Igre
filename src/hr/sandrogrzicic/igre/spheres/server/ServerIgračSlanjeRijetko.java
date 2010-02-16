package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;


class ServerIgračSlanjeRijetko extends Thread {
	private final ServerIgrač igrač;
	private boolean igračSpojen = true;
	private final UDP udp;
	private final List<ServerIgrač> igrači;

	public ServerIgračSlanjeRijetko(final ServerIgrač igrač, final UDP udp, final List<ServerIgrač> igrači) {

		this.igrač = igrač;
		this.udp = udp;
		this.igrači = igrači;
	}

	@Override
	/** Glavna petlja koja diskretizirano rijetko šalje manje važne podatke igraču. */
	public void run() {
		this.setName(getClass().getCanonicalName() + igrač);

		long vrijeme = System.currentTimeMillis();
		long vrijemeSpavanja;

		while (igračSpojen) {
			try {
				pošaljiNiskoPrioritetnePodatke();

				if (Math.random() > 0.999) {
					System.out.println(("% ID: #[" + igrač.getID() + "] recv: " + udp.getUkupnoPrimljeno() / 1024) + " - sent: " +
							(udp.getUkupnoPoslano() / 1024));
				}
			} catch (final IOException e) {
				igrač.odspojen();
				break;
			}

			vrijemeSpavanja = Server.KAŠNJENJE_MREŽA_RIJETKO - System.currentTimeMillis() + vrijeme;

			if (vrijemeSpavanja < 0) {
				vrijemeSpavanja = 2;
			}
			try {
				Thread.sleep(vrijemeSpavanja);
			} catch (final InterruptedException ignorable) {}
			vrijeme = System.currentTimeMillis();
		}
	}

	private void pošaljiNiskoPrioritetnePodatke() throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(4 + 21 * igrači.size());
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.PODACI_NISKI_PRIORITET.id());

		// pošalji bodove
		out.writeLong(igrač.getBodovi());

		// pošalji lokacije sferi
		synchronized (igrači) {
			for (final ServerIgrač ig : igrači) {
				if (ig != igrač) {
					out.writeInt(ig.getID());
					out.writeFloat((float) ig.getSfera().getX());
					out.writeFloat((float) ig.getSfera().getY());
					out.writeFloat((float) ig.getSfera().getR());
					out.writeBoolean(ig.getSfera().getA());
				}
				udp.pošalji(outBAOS);
			}
		}

	}

	/** Prekida izvođenje ove dretve. */
	void igračOdspojen() {
		igračSpojen = false;
	}

}
