package hr.sandrogrzicic.igre.poruke;

import hr.sandrogrzicic.igre.server.AbstractIgrač;
import hr.sandrogrzicic.igre.spheres.Akcije;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

public class Chat extends Poruka {
	private final String poruka;
	private final long timestamp;
	private ByteArrayOutputStream baos;

	public Chat(final AbstractIgrač igrač, final long timestamp, final String poruka) {
		super(igrač);
		this.timestamp = timestamp;
		this.poruka = poruka;
	}

	public String getPoruka() {
		return poruka;
	}

	public final long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Chat poruka [" + izvor + ": '" + poruka + "' @ " + new Date(timestamp) + "]";
	}

	@Override
	public ByteArrayOutputStream getBAOS() {
		if (baos == null) {
			baos = new ByteArrayOutputStream(15 + 3 * poruka.length());
			final DataOutputStream dos = new DataOutputStream(baos);

			try {
				dos.writeByte(Akcije.CHAT_PORUKA.id());
				dos.writeInt(izvor.getID());
				dos.writeLong(timestamp);
				dos.writeUTF(poruka);
			} catch (final IOException ignorable) {}
		}

		return baos;
	}

}
