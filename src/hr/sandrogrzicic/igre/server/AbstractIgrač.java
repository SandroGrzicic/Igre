package hr.sandrogrzicic.igre.server;

import hr.sandrogrzicic.igre.exceptions.NemaSlobodnihPortovaException;
import hr.sandrogrzicic.igre.poruke.Poruka;
import hr.sandrogrzicic.igre.utility.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;


public abstract class AbstractIgrač extends Thread {

	protected static int PAKET_MAX_VELIČINA = 1024;
	protected static int SOCKET_TIMEOUT = 5000;
	protected static int PAKET_VELIKI = 4096;
	protected final int igračID;
	protected final AbstractServer server;
	protected final SocketAddress adresa;
	protected DatagramSocket socket;
	protected AbstractIgračMreža mreža;

	// dostupno mrežnim dretvama
	protected UDP veza;

	/** Kreira novu dretvu koja će posluživati upravo spojenog klijeta. */
	public AbstractIgrač(final DatagramSocket socket, final DatagramPacket paket, final AbstractServer server, final int igračID) {
		this.socket = socket;
		this.server = server;
		this.igračID = igračID;
		this.adresa = paket.getSocketAddress();
	}

	/** Šalje poruku klijentu. */
	abstract protected void poruka(final Poruka poruka);

	/** Novi igrač je spojen. */
	abstract protected void igračSpojen(final AbstractIgrač spojen) throws IOException;

	/** Jedan od igrača je odspojen. */
	abstract protected void igračOdspojen(final AbstractIgrač odspojen) throws IOException;

	/** Javlja serveru i mrežnim dretvama da je ovaj igrač odspojen. */
	public void odspojen() {
		server.igračOdspojen(this);
		mreža.igračOdspojen();
	}

	@Override
	public void run() {
		Thread.currentThread().setName(getClass().getCanonicalName() + this);

		try {
			spojiIgrača();
		} catch (final NemaSlobodnihPortovaException e) {
			System.err.println("Nedovoljno slobodnih portova za spajanje klijenta #[" + igračID + "]!");
			assert (socket.getLocalPort() == AbstractServer.SERVER_PORT);
			return;
		} catch (final SocketTimeoutException ste) {
			System.err.println("Istekao timeout čekajući daljnje podatke od [" + adresa + "]");
			socket.close();
			return;
		} catch (final IOException io) {
			io.printStackTrace();
			return;
		}

		server.igračSpojen(this);

		mreža = inicijalizirajMrežniPodsustav(this);
	}

	protected abstract AbstractIgračMreža inicijalizirajMrežniPodsustav(AbstractIgrač abstractIgrač);

	/** Inicijalizacija "veze" sa klijentom. */
	protected void spojiIgrača() throws IOException {
		final DatagramSocket socketPrivatni = UDP.pronađiSlobodniPort(AbstractServer.SERVER_PORT, AbstractServer.SERVER_PORT_MAX);

		final byte[] portBytes = String.valueOf(socketPrivatni.getLocalPort()).getBytes();
		socket.send(new DatagramPacket(portBytes, portBytes.length, adresa));

		socket = socketPrivatni;
		socket.setSoTimeout(SOCKET_TIMEOUT);
		veza = new UDP(socket, PAKET_MAX_VELIČINA);

		razmijeniInicijalnePodatke();

		System.out.println("+ " + this + '[' + socket.getRemoteSocketAddress() + "] uspješno spojen na port [" +
				socket.getLocalPort() + "]!");
	}

	/** Razmjenjuje inicijalne podatke sa klijentom. */
	protected abstract void razmijeniInicijalnePodatke() throws IOException;

	public int getID() {
		return igračID;
	}


	@Override
	public String toString() {
		return "Igrač [ID: " + igračID + ']';
	}

	/** Vraća DatagramSocket preko kojega je ova dretva spojena sa igračem. */
	public DatagramSocket getSocket() {
		return veza.getSocket();
	}


}