package hr.sandrogrzicic.igre.utility;

import hr.sandrogrzicic.igre.exceptions.NemaSlobodnihPortovaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;


/**
 * Utility klasa koja olakšava rad s UDPom.
 */
public class UDP {

	private DatagramSocket socket;
	private final int				paketMaxVeličina;
	private long					ukupnoPrimljeno;
	private long					ukupnoPoslano;

	/**
	 * Kreira novu UDP utility klasu koja je vezana za novi UDP socket vezan za slučajno odabrani port.
	 */
	public UDP(final int paketMaxVeličina) {
		this.socket = null;
		this.paketMaxVeličina = paketMaxVeličina;
	}

	/**
	 * Kreira novu UDP utility klasu koja je vezana uz zadani socket.
	 */
	public UDP(final DatagramSocket socket, final int paketMaxVeličina) {
		this.socket = socket;
		this.paketMaxVeličina = paketMaxVeličina;
		ukupnoPoslano = 0;
		ukupnoPrimljeno = 0;
	}

	/** Otvara novi UDP socket na slučajno odabranom portu. */
	public void otvoriSocket() throws SocketException {
		this.socket = new DatagramSocket();
	}


	/** Prima jedan paket. Metoda blocka dok se paket ne primi. Vraća DataInputStream iz kojeg je moguće čitati sadržaj paketa. */
	public DataInputStream primi() throws IOException {
		final DatagramPacket paket = new DatagramPacket(new byte[paketMaxVeličina], paketMaxVeličina);
		socket.receive(paket);
		ukupnoPrimljeno += paket.getLength();
		return new DataInputStream(new ByteArrayInputStream(paket.getData()));
	}

	/**
	 * Prima jedan paket, tako da popuni zadani paket s podacima. Metoda blocka dok se paket ne primi. Vraća DataInputStream iz kojeg je moguće čitati
	 * sadržaj paketa.
	 */
	public DataInputStream primi(final DatagramPacket paket) throws IOException {
		if (paket == null) {
			return primi();
		}
		socket.receive(paket);
		ukupnoPrimljeno += paket.getLength();
		return new DataInputStream(new ByteArrayInputStream(paket.getData()));
	}

	/** Šalje jedan paket. Metoda ne blocka, osim ako se šalje paket iz drugog threada. */
	public void pošalji(final ByteArrayOutputStream outBAOS) throws IOException {
		outBAOS.flush();
		socket.send(new DatagramPacket(outBAOS.toByteArray(), outBAOS.size()));
		ukupnoPoslano += outBAOS.size();
	}

	/** Vraća broj ukupno primljenih bajtova od kreacije ovog objekta. */
	public final long getUkupnoPrimljeno() {
		return ukupnoPrimljeno;
	}

	/** Vraća broj ukupno poslanih bajtova od kreacije ovog objekta. */
	public final long getUkupnoPoslano() {
		return ukupnoPoslano;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void connect(final SocketAddress adresa) throws SocketException {
		socket.connect(adresa);
	}

	public void receive(final DatagramPacket paket) throws IOException {
		socket.receive(paket);
		ukupnoPrimljeno += paket.getLength();
	}

	public void send(final DatagramPacket paket) throws IOException {
		socket.send(paket);
		ukupnoPoslano += paket.getLength();
	}

	public void setTimeout(final int timeout) throws SocketException {
		socket.setSoTimeout(timeout);
	}

	/**
	 * Pronalazi prvi slobodni port te binda UDP socket na njega.
	 * 
	 * @param port
	 *            nulti port koji smijemo koristiti (prvi port = serverPort + 1)
	 * @param portMax
	 *            zadnji port koji smijemo koristiti
	 * @return DatagramSocket bindan na prvi slobodni port
	 * @throws NemaSlobodnihPortovaException
	 *             ukoliko nema slobodnih portova unutar zadanog raspona
	 */
	public static DatagramSocket pronađiSlobodniPort(int port, final int portMax) throws NemaSlobodnihPortovaException {
		DatagramSocket socketNovi = null;
		while ((socketNovi == null) && (port < portMax)) {
			try {
				port++;
				socketNovi = new DatagramSocket(port);
			} catch (final SocketException ignorable) {}
		}
		if (socketNovi == null) {
			throw new NemaSlobodnihPortovaException();
		}
		return socketNovi;
	}

}
