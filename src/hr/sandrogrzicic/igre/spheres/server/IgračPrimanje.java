package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.exceptions.NeočekivanaAkcijaException;
import hr.sandrogrzicic.igre.server.AbstractIgrač;
import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Calendar;


class IgračPrimanje extends Thread {
	private final Igrač igrač;
	private boolean igračSpojen = true;
	private final UDP veza;
	private final Sfera sfera;

	public IgračPrimanje(final AbstractIgrač igrač, final UDP veza, final Sfera sfera) {
		this.setDaemon(true);
		this.setName(getClass().getCanonicalName() + igrač);

		this.igrač = (Igrač) igrač;
		this.veza = veza;
		this.sfera = sfera;
	}

	@Override
	/** Glavna petlja koja kontinuirano prima podatke igrača. */
	public void run() {

		while (igračSpojen) {
			try {
				primiPodatke();
			} catch (final IOException e) {
				igrač.odspojen();
				break;
			}
		}
	}

	/** Prima podatke od klijenta. Blokira izvođenje dok se podaci ne prime. */
	private void primiPodatke() throws IOException {
		final DataInputStream in = veza.primi();

		final Akcije akcija = Akcije.get(in.readByte());
		switch (akcija) {
		case POMAK_SFERE:
			sfera.set(in.readFloat(), in.readFloat(), in.readBoolean());
			break;
		case CHAT_PORUKA:
			final String poruka = in.readUTF();
			igrač.chatPrimljen(Calendar.getInstance().getTimeInMillis(), poruka);
			break;
		case IGRAČ_IZAŠAO:
			igrač.odspojen();
			break;
		default:
			throw new NeočekivanaAkcijaException(akcija, igrač.getID());
		}
	}

	/** Prekida izvođenje ove dretve. */
	void igračOdspojen() {
		igračSpojen = false;
	}

}
