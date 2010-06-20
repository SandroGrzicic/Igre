package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.server.AbstractListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Listener extends AbstractListener {
	protected byte[] CONNECTION_STRING = "Bok!".getBytes();

	/** Inicijalizira ovaj Listener. */
	public Listener(final Server server, final int port) {
		super(server, port);
	}

	@Override
	protected void kreirajIgrača(final DatagramSocket socket, final DatagramPacket paket, final int igračID) {
		final Igrač igrač = new Igrač(socket, paket, (Server) server, igračID);

		igrač.setDaemon(true);
		igrač.start();

	}

}
