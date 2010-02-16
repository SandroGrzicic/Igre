package hr.sandrogrzicic.igre.spheres.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

public class ServerListener implements Runnable {
	private static final byte[] CONNECTION_STRING = "Bok!".getBytes();

	private final HashMap<InetAddress, Short> adrese = new HashMap<InetAddress, Short>();

	private DatagramSocket socket;
	private final Server server;
	private final int port;

	/** Inicijalizira ovaj ServerListener. */
	public ServerListener(final Server server, final int port) {
		this.server = server;
		this.port = port;
	}

	@Override
	/** Sluša zahtjeve za spajanje na server. */
	public void run() {
		int brojIgračaZadnjeg = 0;

		while (server.getBrojIgrača() < Server.SERVER_MAX_IGRAČA) {
			try {
				final DatagramPacket paket = new DatagramPacket(new byte[4], 4);
				socket.receive(paket);

				final InetAddress adresa = paket.getAddress();
				if (adrese.containsKey(adresa) && (adrese.get(adresa).compareTo(Server.MAKSIMALAN_BROJ_KLONOVA) >= 0)) {
					// dosegnut maksimalan broj konekcija s jednog IPa
					continue;
				}
				adrese.put(adresa, adrese.containsKey(adresa) ? Short.valueOf((short) (adrese.get(adresa) + 1)) : 1);

				if (!Arrays.equals(paket.getData(), CONNECTION_STRING)) {
					// neispravni handshake
					System.err.println("Dobiven paket koji nema odgovarajući connection_string [ID: #" + brojIgračaZadnjeg + ", adresa: [" +
							paket.getSocketAddress() + "].");
					continue;
				}

				final ServerIgrač igrač = new ServerIgrač(socket, paket, server, brojIgračaZadnjeg);

				brojIgračaZadnjeg++;

				igrač.setDaemon(true);
				igrač.start();

			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void klijentOdspojen(final InetAddress adresa) {
		adrese.put(adresa, Short.valueOf((short) (adrese.get(adresa) - 1)));
	}

	/**
	 * Pokušava otvoriti listener socket na portu s kojim je ovaj objekt konstruiran.
	 * 
	 * @throws SocketException ukoliko port nije moguće koristiti.
	 */
	public void bind() throws SocketException {
		this.socket = new DatagramSocket(port);
	}

}
