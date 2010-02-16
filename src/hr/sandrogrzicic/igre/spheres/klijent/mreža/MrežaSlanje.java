package hr.sandrogrzicic.igre.spheres.klijent.mreža;

import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.klijent.Klijent;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;


public class MrežaSlanje extends Thread {
	protected static final short DEFAULT_KAŠNJENJE = 10;
	protected final Klijent klijent;
	protected final UDP udp;
	protected int kašnjenje;
	private boolean igraAktivna;

	public MrežaSlanje(final Klijent klijent, final UDP udp) {
		this.setName(getClass().getCanonicalName());
		this.setDaemon(true);
		this.klijent = klijent;
		this.udp = udp;
		this.kašnjenje = DEFAULT_KAŠNJENJE;
		this.igraAktivna = true;
	}

	@Override
	public void run() {
		long vrijeme = System.currentTimeMillis();
		long vrijemeSpavanja;

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
				mrežaPošalji();
			} catch (final SocketTimeoutException ste) {
				klijent.izgubljenaVeza();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			vrijemeSpavanja = kašnjenje - System.currentTimeMillis() + vrijeme;

			if (vrijemeSpavanja < 0) {
				vrijemeSpavanja = 2;
			}
			try {
				Thread.sleep(vrijemeSpavanja);
			} catch (final InterruptedException ignorable) {}
			vrijeme = System.currentTimeMillis();

		}
	}

	/**
	 * Metoda ne radi ništa.
	 * 
	 * @throws IOException
	 */
	protected void mrežaPošalji() throws IOException {
	}

	/** Zove se kada igrač izađe iz igre ili zatvori prozor. */
	public void klijentIzašao() throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(512);
		final DataOutputStream out = new DataOutputStream(outBAOS);
		out.writeByte(Akcije.IGRAČ_IZAŠAO.id());
		udp.pošalji(outBAOS);
	}

	/** Utility metoda za slanje određene akcije, bez ikakvih parametara. */
	void pošaljiAkciju(final Akcije akcija) throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(4);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(akcija.id());
		udp.pošalji(outBAOS);
	}

	public void pošaljiPoruku(final String poruka) throws IOException {
		final ByteArrayOutputStream outBAOS = new ByteArrayOutputStream(4);
		final DataOutputStream out = new DataOutputStream(outBAOS);

		out.writeByte(Akcije.CHAT_PORUKA.id());
		out.writeUTF(poruka);

		udp.pošalji(outBAOS);
	}

	public long getKašnjenje() {
		return kašnjenje;
	}

	public void setKašnjenje(final int kašnjenje) {
		this.kašnjenje = kašnjenje;
	}

	public void setIgraAktivna(final boolean igraAktivna) {
		this.igraAktivna = igraAktivna;
	}

}
