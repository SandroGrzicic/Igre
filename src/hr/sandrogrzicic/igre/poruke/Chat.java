package hr.sandrogrzicic.igre.poruke;

import hr.sandrogrzicic.igre.server.AbstractIgrač;

import java.util.Date;

public class Chat extends Poruka {
	private final String poruka;
	private final long timestamp;

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


}
