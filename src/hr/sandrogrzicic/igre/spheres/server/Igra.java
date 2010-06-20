package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.poruke.Sudar;
import hr.sandrogrzicic.igre.server.AbstractIgra;
import hr.sandrogrzicic.igre.server.AbstractIgrač;
import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;

import java.util.List;


public class Igra extends AbstractIgra {
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
	private static final double SFERA_R_BRZINA_SMANJIVANJA = 0.99994;
	private static final double SFERA_R_BRZINA_POVEĆAVANJA = 1.000025;
	private static final double LOPTICA_POČETNI_POLOŽAJ_X = 0.5;

	private final Server server;
	private final Loptica loptica = new Loptica();

	/** Minimalni radius sfere. */
	private final double radiusMin = 0.03125;
	/** Maksimalni radius sfere. */
	private final double radiusMax = 0.125;

	private double bodovi;

	public Igra(final Server server, final List<AbstractIgrač> igrači) {
		this.server = server;
		this.igrači = igrači;
	}

	@Override
	public void run() {
		pomakniLopticu();
		osvježiRadiuseSfera();
	}

	@Override
	public void pokreni() {
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

	@Override
	public void zaustavi() {
	}

	/** Računa nove radiuse sfera s obzirom na trenutnu aktivnost sfere. */
	private void osvježiRadiuseSfera() {
		synchronized (igrači) {
			for (final AbstractIgrač igrač : igrači) {
				final Sfera sfera = ((Igrač) igrač).getSfera();
				if (sfera.getA() && (sfera.getR() > radiusMin)) {
					sfera.setR(sfera.getR() * SFERA_R_BRZINA_SMANJIVANJA);
				} else if (!sfera.getA() && (sfera.getR() < radiusMax)) {
					sfera.setR(sfera.getR() * SFERA_R_BRZINA_POVEĆAVANJA);
				}
			}
		}
	}

	/**
	 * Pomiče lopticu te provjerava sudare. Fizika.
	 */
	private void pomakniLopticu() {
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
				for (final AbstractIgrač igračAbstract : igrači) {
					final Igrač igrač = (Igrač) igračAbstract;
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
						final double jačina = ((nX * (float) loptica.getXv()) + (nY * (float) loptica.getYv())) / ((nX * nX) + (nY * nY));
						if (jačina > 0) {
							final double vM = SUDAR_FAKTOR_ODBIJANJA * Math.cbrt(radiusMax / sfera.getR());
							// System.out.println(radius + " " + sfera.getR());
							xv -= (vM * jačina * nX);
							yv -= (vM * jačina * nY);

							if (jačina > SUDAR_JAČINA_MINIMALNA) {
								bodovi(BODOVI_SUDAR * jačina);
								server.poruka(new Sudar(igrač, x, y, jačina));
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

	public Loptica getLoptica() {
		return loptica;
	}

	public double getRadius() {
		return radiusMax;
	}



}
