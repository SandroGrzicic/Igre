package hr.sandrogrzicic.igre.spheres.klijent.prikaz;

import java.awt.Color;

/** Često korištene boje. */
enum Boja {

	PLAVA("Plava", Color.BLUE.darker()),
	TAMNOZELENA("Zelena", Color.GREEN.darker()),
	CRVENA("Crvena", Color.RED.darker()),
	SVIJETLOSIVA("Svijetlosiva", Color.LIGHT_GRAY),
	SIVA("Siva", Color.GRAY),
	TAMNOSIVA("Tamnosiva", Color.DARK_GRAY),
	ŽUTA("Žuta", Color.YELLOW.darker()),
	NARANČASTA("Narančasta", Color.ORANGE.darker()),
	ROZA("Roza", Color.PINK.darker()),
	RUŽIČASTA("Ružičasta", Color.MAGENTA);

	private final String ime;
	private final Color boja;

	Boja(final String ime, final Color boja) {
		this.ime = ime;
		this.boja = boja;
	}

	Color getBoja() {
		return boja;
	}

	static Boja vratiRandomBoju() {
		return Boja.values()[(int) (Math.random() * Boja.values().length)];
	}

	@Override
	public String toString() {
		return this.ime;
	}


}