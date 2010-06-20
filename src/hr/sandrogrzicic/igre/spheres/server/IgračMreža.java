package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.server.AbstractIgrač;
import hr.sandrogrzicic.igre.server.AbstractIgračMreža;
import hr.sandrogrzicic.igre.server.AbstractServer;
import hr.sandrogrzicic.igre.utility.UDP;

public class IgračMreža extends AbstractIgračMreža {

	private final IgračPrimanje primanje;
	private final IgračSlanjeRijetko slanjeRijetko;
	private final IgračSlanjeČesto slanjeČesto;

	public IgračMreža(final AbstractIgrač igračAbstract, final UDP veza, final AbstractServer server) {
		super(igračAbstract, veza, server);

		final Igrač igrač = (Igrač) igračAbstract;

		primanje = new IgračPrimanje(igrač, veza, igrač.getSfera());
		slanjeRijetko = new IgračSlanjeRijetko(igrač, veza, server.getIgrači());
		slanjeČesto = new IgračSlanjeČesto(igrač, veza, ((Server) server).getLoptica());

		primanje.setDaemon(true);
		slanjeRijetko.setDaemon(true);
		slanjeČesto.setDaemon(true);
		// započni sa izvođenjem mrežnih dretvi.
		primanje.start();
		slanjeRijetko.start();
		slanjeČesto.start();

	}

	@Override
	public void igračOdspojen() {
		primanje.igračOdspojen();
		slanjeRijetko.igračOdspojen();
		slanjeČesto.igračOdspojen();
	}

}
