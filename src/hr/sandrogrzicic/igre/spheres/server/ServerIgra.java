package hr.sandrogrzicic.igre.spheres.server;

import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;

import java.util.List;


public class ServerIgra implements Runnable {
	// todo: prebaciti u neki config
	private static final double BODOVI_SUDAR = 131072;
	private static final double BODOVI_ZID = 4194304;
	private static final double BODOVI_PAD = -65536;
	private static final double BODOVI_FAKTOR_GORE_X = 23;
	private static final double BODOVI_FAKTOR_GORE_Y = 7;
	private static final double GRAVITACIJA_KONSTANTNA = 0.000000000025 * Server.KAŠNJENJE_IGRA;
	private static final double GRAVITACIJA_GORE = 0.00000005 * Server.KAŠNJENJE_IGRA;
	private static final double FAKTOR_ODBIJANJE_ZID = 0.8;
	private static final double SUDAR_FAKTOR_ODBIJANJA = 1.8;
	private boolean igraPokrenuta;
	private final Server server;
	private final Loptica loptica = new Loptica();
	private double radius;

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
		} else {
			igraInit();
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
							final double vM = SUDAR_FAKTOR_ODBIJANJA * Math.cbrt(radius / sfera.getR());
							// System.out.println(radius + " " + sfera.getR());
							xv -= (vM * vNnN * nX);
							yv -= (vM * vNnN * nY);

							if (vNnN > 0.000125) {
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
			// lijevo
			if (xv < -0.00001) {
				bodovi(BODOVI_ZID * Math.abs(xv));
			}
			xv = -xv * FAKTOR_ODBIJANJE_ZID;
			x = r;
		} else if (x >= 1 - r) {
			// desno
			if (xv > 0.00001) {
				bodovi(BODOVI_ZID * Math.abs(xv));
			}
			xv = -xv * FAKTOR_ODBIJANJE_ZID;
			x = 1 - r;
		}
		if (y <= r) {
			// gore
			bodovi(BODOVI_FAKTOR_GORE_X * xv + Math.abs(BODOVI_FAKTOR_GORE_Y * yv));
			// y = r;
		} else if (y >= 1 - r) {
			// dolje (propadanje)
			assert (yv > 0) : "Vertikalna komponenta brzine je negativna pri propadanju!";
			bodovi(BODOVI_PAD * Math.sqrt(igrači.size()) / (1 + Math.abs(xv) + yv));
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

	/** Inicijalizacija parametara igre. */
	void igraInit() {
		igrači.clear();
		radius = 0.1;

		if (bodovi < 0) {
			server.bodovi(Integer.MIN_VALUE);
		}
		if (loptica.getY() > 0) {
			loptica.setX(0.5);
			loptica.setY(loptica.getR());
			loptica.setXv(0);
			loptica.setYv(0);
		}
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
		return radius;
	}

}
