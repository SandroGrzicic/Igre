package hr.sandrogrzicic.igre.spheres.klijent.mreža;

import hr.sandrogrzicic.igre.spheres.klijent.Klijent;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;


public class MrežaPrimanje extends Thread {
	private final Klijent klijent;
	private final UDP udp;
	private boolean igraAktivna;

	public MrežaPrimanje(final Klijent klijent, final UDP udp) {
		this.setName(getClass().getCanonicalName());
		this.setDaemon(true);
		this.klijent = klijent;
		this.udp = udp;
		this.igraAktivna = true;
	}

	@Override
	public void run() {
		while (true) {

			while (!igraAktivna) {
				try {
					Thread.sleep(100);
					if (klijent.isIgraAktivna()) {
						igraAktivna = true;
					}
				} catch (final InterruptedException ignorable) {}
			}

			try {
				// blocka dok se ne primi paket ili ne istekne timeout
				klijent.primljenPaket(udp.primi());
			} catch (final PortUnreachableException pue) {
				klijent.izgubljenaVeza();
			} catch (final SocketTimeoutException ste) {
				klijent.izgubljenaVeza();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setIgraAktivna(final boolean igraAktivna) {
		this.igraAktivna = igraAktivna;
	}
}
