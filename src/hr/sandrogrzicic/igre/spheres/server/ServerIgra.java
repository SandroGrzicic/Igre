package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;

import java.util.List;


public class ServerIgra implements Runnable {
	// todo: prebaciti u neki config
	private static final double BODOVI_SUDAR = 131072;
	private static final double BODOVI_ZID = 4194304;
	private static final double BODOVI_PAD = -10000;
	private static final double BODOVI_FAKTOR_X = 70;
	private static final double BODOVI_FAKTOR_Y = 46;
	private static final double GRAVITACIJA_KONSTANTNA = 0.000000000025 * Server.KAŠNJENJE_IGRA;
	private static final double GRAVITACIJA_GORE = 0.0000001 * Server.KAŠNJENJE_IGRA;
	private static final double FAKTOR_ODBIJANJE_ZID = 0.8;
	private static final double SUDAR_FAKTOR_ODBIJANJA = 1.8;
	private static final double SUDAR_JAČINA_MINIMALNA = 0.000125;
	private static final double SFERA_R_BRZINA_POVEĆAVANJA = 0.99994;
	private static final double SFERA_R_BRZINA_SMANJIVANJA = 1.000025;
	private static final double LOPTICA_POČETNI_POLOŽAJ_X = 0.5;

	private boolean igraPokrenuta;
	private final Server server;
	private final Loptica loptica = new Loptica();
	/** Minimalni radius sfere. */
	private double radiusMin;
	/** Maksimalni radius sfere. */
	private double radiusMax;

	private double bodovi;
	private final List<ServerIgrač> igrači;

	public ServerIgra(final Server server, final List<ServerIgrač> igrači) {
		this.server = server;
		this.igrači = igrači;
	}

	@Override
	public void run() {
		if (igraPokrenuta) {
			pomakniLopticu();
			osvježiRadiuseSfera();
		} else {
			igraInit();
		}
	}

	/** Inicijalizacija parametara igre pri prvom pokretanju ili nastavljanju pauzirane igre. */
	void igraInit() {
		igrači.clear();
		radiusMin = 0.03125;
		radiusMax = 0.125;

		if (bodovi < 0) {
			server.bodovi(Integer.MIN_VALUE);
		}
		if (loptica.getY() > 0) {
			loptica.setX(LOPTICA_POČETNI_POLOŽAJ_X);
			loptica.setY(loptica.getR());
			loptica.setXv(0);
			loptica.setYv(0);
		}
	}

	/** Računa nove radiuse sfera s obzirom na trenutnu aktivnost sfere. */
	private void osvježiRadiuseSfera() {
		synchronized (igrači) {
			for (final ServerIgrač igrač : igrači) {
				final Sfera sfera = igrač.getSfera();
				if (sfera.getA() && (sfera.getR() > radiusMin)) {
					sfera.setR(sfera.getR() * SFERA_R_BRZINA_POVEĆAVANJA);
				} else if (sfera.getR() < radiusMax) {
					sfera.setR(sfera.getR() * SFERA_R_BRZINA_SMANJIVANJA);
				}
			}
		}
	}

	/**
	 * Pomiče lopticu te provjerava sudare. Fizika.
	 */
	final void pomakniLopticu() {
		double x = loptica.getX();
		double y = loptica.getY();
		double xv = loptica.getXv();
		double yv = loptica.getYv();
		final double r = loptica.getR();

		x += xv;
		y += yv;

		// sudari loptica-sfere
		if (y > 0.1) {
			synchronized (this) {
				for (final ServerIgrač igrač : igrači) {
					final Sfera sfera = igrač.getSfera().clone();

					/** udaljenost ruba prve sfere do ruba druge sfere */
					final double d = ((sfera.getX() - x) * (sfera.getX() - x) + (sfera.getY() - y) * (sfera.getY() - y)) -
					(sfera.getR() + r) * (sfera.getR() + r);
					// final float vLimit = 512 * (float) Math.sqrt(xv * xv + yv * yv);

					// if (d - (vLimit > 512 ? vLimit : 512) < 0) {

					if (d < 0.001) {

						// normala
						final float nX = (float) (sfera.getX() - x);
						final float nY = (float) (sfera.getY() - y);
						// dot product normale i brzine loptice podijeljeno sa dot productom normale sa sobom
						final double vNnN = ((nX * (float) loptica.getXv()) + (nY * (float) loptica.getYv())) / ((nX * nX) + (nY * nY));
						if (vNnN > 0) {
							final double vM = SUDAR_FAKTOR_ODBIJANJA * Math.cbrt(radiusMax / sfera.getR());
							// System.out.println(radius + " " + sfera.getR());
							xv -= (vM * vNnN * nX);
							yv -= (vM * vNnN * nY);

							if (vNnN > SUDAR_JAČINA_MINIMALNA) {
								bodovi(BODOVI_SUDAR * vNnN);
								server.sudar(igrač.getID(), x, y, vNnN);
							}
						}
					}
				}
			}
		}

		// gravitacija
		if (yv < 0) {
			yv = yv * (1 - GRAVITACIJA_GORE);
		}
		yv += GRAVITACIJA_KONSTANTNA;

		// odbijanje od zidova
		if (x <= r) {
			// lijevo; jako rijetko
			if (xv < -0.00001) {
				bodovi(BODOVI_ZID * Math.abs(xv));
			}
			xv = -xv * FAKTOR_ODBIJANJE_ZID;
			x = r;
		} else if (x >= 1 - r) {
			// desno; jako rijetko
			if (xv > 0.00001) {
				bodovi(BODOVI_ZID * Math.abs(xv));
			}
			xv = -xv * FAKTOR_ODBIJANJE_ZID;
			x = 1 - r;
		}
		if (y <= r) {
			// gore; *jako često*; treba biti brzo
			bodovi(BODOVI_FAKTOR_X * xv + Math.abs(BODOVI_FAKTOR_Y * yv));
			// y = r;
		} else if (y >= 1 - r) {
			// dolje (propadanje); jako rijetko; može biti proizvoljno sporo
			assert (yv > 0) : "Vertikalna komponenta brzine je negativna pri propadanju!";
			bodovi(BODOVI_PAD * Math.sqrt(igrači.size()) / (1 + Math.pow(Math.abs(xv), 1 / BODOVI_FAKTOR_X) + Math.pow(yv, 1 / BODOVI_FAKTOR_Y)));
			yv = 0;
			xv = 0;
			y = r;
		}

		// provjera
		if (Double.isInfinite(xv) || Double.isNaN(xv)) {
			xv = 1 - 2 * Math.random();
		}
		if (Double.isInfinite(yv) || Double.isNaN(yv)) {
			yv = 1 - 2 * Math.random();
		}

		loptica.setX(x);
		loptica.setY(y);
		loptica.setXv(xv);
		loptica.setYv(yv);
	}

	/** Dodaje zadane bodove trenutnim bodovima te javlja Serveru nove bodove. */
	private void bodovi(final double bodoviDelta) {
		bodovi += bodoviDelta;
		server.bodovi(bodovi);
	}


	public void setIgraPokrenuta(final boolean igraPokrenuta) {
		this.igraPokrenuta = igraPokrenuta;
	}

	public boolean isIgraPokrenuta() {
		return igraPokrenuta;
	}

	public Loptica getLoptica() {
		return loptica;
	}

	public double getRadius() {
		return radiusMax;
	}

}
