package hr.sandrogrzicic.igre.spheres.klijent.prikaz;

import hr.sandrogrzicic.igre.klijent.AbstractKlijent;
import hr.sandrogrzicic.igre.spheres.Akcije;
import hr.sandrogrzicic.igre.spheres.klijent.Igrač;
import hr.sandrogrzicic.igre.spheres.klijent.Klijent;
import hr.sandrogrzicic.igre.spheres.klijent.efekti.Efekt;
import hr.sandrogrzicic.igre.spheres.klijent.efekti.Sudar;
import hr.sandrogrzicic.igre.spheres.objekti.Loptica;
import hr.sandrogrzicic.igre.spheres.objekti.Sfera;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;


/**
 * JFrame implementacija KlijentPrikaza. Koristi Swing (JFrame container) te Canvas/Java2D za iscrtavanje.
 * 
 * @author Sandro Gržičić
 */
public class PrikazSpheresJFrame extends JFrame implements PrikazSpheres, MouseListener, MouseMotionListener, KeyListener {
	private static final long serialVersionUID = 4455192690762108750L;
	static final String NASLOV_PROZORA = "Serious Spheres";
	private static final String UPPER_TEXT = "Serious Spheres by SeriousWorm build " + AbstractKlijent.VERZIJA;
	private static final Color BOJA_VISINA_1 = new Color(255, 192, 192);
	private static final Color BOJA_VISINA_2 = new Color(255, 128, 128);
	private static final Color BOJA_VISINA_3 = new Color(255, 64, 64);
	private static final Color BOJA_VISINA_4 = new Color(255, 0, 0);
	private static final Color BOJA_GORNJI_TEKST = new Color(64, 64, 64);
	private static final Color BOJA_POZADINA = Color.BLACK;
	private static final Color BOJA_POZADINA_VRH = new Color(32, 32, 32);
	private static final Color BOJA_BODOVI = Color.RED;
	private static final Color BOJA_BODOVI_MAX = BOJA_BODOVI.darker();
	private static final Color BOJA_BRZINA_LOPTICE = Color.YELLOW;
	private static final Color BOJA_BRZINA_LOPTICE_MAX = BOJA_BRZINA_LOPTICE.darker();
	private static final Color BOJA_VISINA_LOPTICE = Color.PINK;
	private static final Color BOJA_VISINA_LOPTICE_MAX = BOJA_VISINA_LOPTICE.darker();
	private static final Color BOJA_LOPTICA_RUB = Color.RED.darker();
	private static final Color BOJA_LOPTICA_FILL = Color.DARK_GRAY;
	private static final Font FONT_DEFAULT = new Font("Sans-serif", Font.PLAIN, 14);
	private static final Font FONT_CHAT = new Font("Monospaced", Font.PLAIN, 11);
	private static final Font FONT_BODOVI = FONT_DEFAULT.deriveFont(17f);
	private static final Font FONT_SFERE = new Font("Monospaced", Font.PLAIN, 10);
	private static final int KVALITETA_MAX = 1;
	private static final int KVALITETA_MIN = -1;
	private static final Color BOJA_CHAT_VLASTITI = Color.WHITE;
	private static final int CHAT_BUFFER_MAX = 255;
	private static final String STRING_NULA = "0";
	private static final double KONSTANTA_BRZINE = 4000;
	private static final short PORUKE_DECAY_RATE = 255;
	private static final String TEKST_HELP = "ALT+a\t\t Always on top\nALT+g\t\t Kvaliteta grafike: niska/visoka\nALT+e\t\t Efekti: prikaži/sakrij\nALT+c\t\t Chat: prikaži/sakrij\nALT+p\t\t PSYCHO!!!\n\n- Miš -\nLijeva tipka:\t smanjuj sferu (tijekom držanja)\nDesna tipka:\t smanjuj sferu (toggle)\nSrednja ili ALT+desna tipka:\t zaključaj trenutnu poziciju sfere (toggle)"
		+ "\n\nOgromno hvala svima koji su testirali ovu igru te mi tako iznimno pomogli!\nPosebne zahvale: Sh1fty, v-v, immortal_cro, d4red3vil, Dr_Car_T, ce, Vedran Lanc, Rovokop. Hvala!";
	// private static final Color BOJA_CHAT_SELF = new Color(192, 192, 255);
	private Canvas okvir;
	private int širina;
	private int visina;
	private BufferStrategy bufferStrategy;
	private final int kašnjenje;
	private final HashMap<Key, Object> renderingHints;
	private final GraphicsConfiguration gConfig;

	private int fps = 0;
	private long fpsVrijemeNano;
	private long fpsBrojač;
	private final double[] fpsAvg = new double[16];
	private int fpsAvgNext = 0;

	final Klijent klijent;
	private boolean igraAktivna;
	private final Sfera sfera;
	private final Map<Integer, Igrač> igrači;
	private final ConcurrentHashMap<Integer, Sfera> sfere = new ConcurrentHashMap<Integer, Sfera>(16, 0.5f, 1);
	private final Loptica loptica;
	// private final List<Efekt> efekti;
	private final List<Efekt> efekti;
	private double radiusMax;
	private boolean inicijalizacija;

	private final List<Poruka> poruke = new ArrayList<Poruka>();
	private final List<String> porukeCache = new ArrayList<String>();
	private final List<Long> porukeCacheVrijeme = new ArrayList<Long>();
	private char[] chatBuffer;
	private int chatBufferPos;
	private String chatBufferStr = "";
	private boolean chatUključen;

	private String bodoviStr = "0";
	private long bodoviMax;
	private String bodoviMaxStr = "0";
	private long brzinaMax;
	private String brzinaMaxStr = "0";
	private int visinaMax;
	private String visinaMaxStr = "0";
	int gfxKvaliteta = 1;

	private boolean crtajPozadinu = true;
	private boolean crtajEfekte = true;
	private Dimension rezolucija;
	/** Faktor kojim se množi podatak dobiven od servera kako bi se ispravno prikazao u prozoru sa određenom rezolucijom. */
	private double faktor;
	private final Igrač igrač;

	private final DateFormat formatter;
	private boolean pritisnutGumbSrednji;
	private boolean pritisnutGumbDesni;
	private final Map<Color, Color> bojeCacheSvjetlije = new HashMap<Color, Color>();

	/**
	 * Kreira novi KlijentPrikaz u obliku Swing JFramea.
	 */
	public PrikazSpheresJFrame(final Klijent klijent, final Igrač igrač) {
		super(NASLOV_PROZORA);
		this.klijent = klijent;
		this.renderingHints = new HashMap<Key, Object>();
		this.gConfig = this.getGraphicsConfiguration();
		this.igrač = igrač;
		this.sfera = new Sfera(igrač.getSfera().getBoja(), igrač.getSfera().getR());
		this.loptica = new Loptica(klijent.getLoptica().getR());
		this.igrači = klijent.getIgrači();
		this.efekti = new CopyOnWriteArrayList<Efekt>();
		// this.efekti = new ArrayList<Efekt>();
		this.chatBuffer = new char[255];
		inicijalizacija = false;

		// final Map<String, Object> argumenti = parsirajArgumente(args);
		// this.igrač.setIme((String) argumenti.get("ime"));
		// this.rezolucija = (Dimension) argumenti.get("res");

		// default postavke
		this.kašnjenje = 6;
		this.chatUključen = true;
		this.formatter = new SimpleDateFormat("kk:mm:ss");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignorable) {}

		initOpcije();
		initWindow();

		// osvježi lokalne kopije postavki
		promjenaPostavki();

		okvir.addMouseMotionListener(this);
		okvir.addMouseListener(this);
		okvir.addKeyListener(this);
		addKeyListener(this);

		klijent.dodajPrikaz(this);
	}

	private Map<String, Object> parsirajArgumente(final String[] argumenti) {
		final Map<String, Object> args = new HashMap<String, Object>();
		if ((argumenti != null) && (argumenti.length > 0)) {
			// TODO: parsiraj argumente
		}
		return args;

	}

	@Override
	public void run() {
		assert (inicijalizacija) : "Potrebno je pozvati setInit() metodu prije pokretanja klase!";

		osvježiCache();
		igra();
	}

	/**
	 * Popunjava mapu sfera koristeći popunjenu mapu igrača. Ključevi nove mape imaju istu referencu
	 * kao ključevi stare, no sfere se kloniraju te su neovisne o mapi igrača.
	 */
	private void osvježiCache() {
		sfere.clear();
		bojeCacheSvjetlije.clear();
		for (final Entry<Integer, Igrač> unos : igrači.entrySet()) {
			if (unos.getValue() != igrač) {
				sfere.put(unos.getKey(), unos.getValue().getSfera().clone());
				bojeCacheSvjetlije.put(unos.getValue().getSfera().getBoja(), unos.getValue().getSfera().getBoja().brighter());
			}
		}
		bojeCacheSvjetlije.put(sfera.getBoja(), sfera.getBoja().brighter());
	}

	/** Glavna prikazna petlja. */
	public void igra() {
		long vrijeme = System.currentTimeMillis();
		long vrijemeSpavanja;
		fpsBrojač = 0;
		fpsVrijemeNano = System.nanoTime();

		while (true) {
			if (!igraAktivna) {
				while (!igraAktivna) {
					try {
						Thread.sleep(4 * kašnjenje);
					} catch (final InterruptedException ignorable) {}
				}
				osvježiCache();
			}

			grafika();

			vrijemeSpavanja = kašnjenje - System.currentTimeMillis() + vrijeme;

			izračunajFPS();

			if (vrijemeSpavanja < 0) {
				vrijemeSpavanja = 1;
			}
			try {
				Thread.sleep(vrijemeSpavanja);
			} catch (final InterruptedException ignorable) {}
			vrijeme = System.currentTimeMillis();
		}
	}

	private void grafika() {
		final Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();

		// final BufferedImage img = gConfig.createCompatibleImage(širina, visina, Transparency.BITMASK);

		// pozadina
		if (crtajPozadinu) {
			g.setBackground(BOJA_POZADINA_VRH);
			g.clearRect(0, 0, širina, visina / 10);
			g.setBackground(BOJA_POZADINA);
			g.clearRect(0, visina / 10, širina, visina);
		}
		g.setRenderingHints(renderingHints);

		// sfere
		g.setFont(FONT_SFERE);
		for (final Entry<Integer, Sfera> unos : sfere.entrySet()) {
			try {
				final Igrač ig = igrači.get(unos.getKey());
				iscrtajSferu(g, unos.getValue(), ig.getIme());
			} catch (final NullPointerException npe) {
				// ignore
			}
		}
		// vlastita sfera
		iscrtajSferu(g, sfera.clone(), igrač.getIme());

		// gornji tekst
		g.setColor(BOJA_GORNJI_TEKST);
		g.drawString(UPPER_TEXT, širina / 2 - UPPER_TEXT.length() * 3, visina / 50);

		// loptica
		if (loptica.getY() > loptica.getR()) {
			g.setColor(BOJA_LOPTICA_RUB);
			g.fillOval(zaokruži(loptica.getX() - loptica.getR() - 2), zaokruži(loptica.getY() - loptica.getR() - 2),
					zaokruži(2 * loptica.getR() + 4), zaokruži(2 * loptica.getR() + 4));
			g.setColor(BOJA_LOPTICA_FILL);
			g.fillOval(zaokruži(loptica.getX() - loptica.getR()), zaokruži(loptica.getY() - loptica.getR()), zaokruži(2 * loptica.getR()),
					zaokruži(2 * loptica.getR()));
		} else if (loptica.getY() < -4 * visina) {
			g.setColor(BOJA_VISINA_4);
			g.fillOval(zaokruži(loptica.getX() - loptica.getR()), zaokruži(loptica.getR()),
					zaokruži(loptica.getR()), zaokruži(loptica.getR()));
		} else if (loptica.getY() < -2 * visina) {
			g.setColor(BOJA_VISINA_3);
			g.fillOval(zaokruži(loptica.getX() - loptica.getR()), zaokruži(loptica.getR()),
					zaokruži(loptica.getR()), zaokruži(loptica.getR()));
		} else if (loptica.getY() < -visina) {
			g.setColor(BOJA_VISINA_2);
			g.fillOval(zaokruži(loptica.getX() - loptica.getR()), zaokruži(loptica.getR()),
					zaokruži(loptica.getR()), zaokruži(loptica.getR()));
		} else {
			g.setColor(BOJA_VISINA_1);
			g.fillOval(zaokruži(loptica.getX() - loptica.getR()), zaokruži(loptica.getR()),
					zaokruži(loptica.getR()), zaokruži(loptica.getR()));
		}

		// efekti
		if (!efekti.isEmpty()) {
			for (final ListIterator<Efekt> it = efekti.listIterator(); it.hasNext();) {
				if (!it.next().iscrtaj(g) && (it.previousIndex() < efekti.size())) {
					efekti.remove(it.previousIndex());
				}
			}
		}

		// brzina loptice
		g.setColor(BOJA_BRZINA_LOPTICE);
		g.setFont(FONT_DEFAULT);
		g.drawString("v", 2 * širina / 33, visina / 20);
		g.drawString(trenutnaBrzinaLoptice(), 3 * širina / 33, visina / 20);
		// max brzina loptice
		g.setColor(BOJA_BRZINA_LOPTICE_MAX);
		g.setFont(FONT_DEFAULT);
		g.drawString(brzinaMaxStr, 3 * širina / 33, visina / 13);

		// visina loptice
		g.setColor(BOJA_VISINA_LOPTICE);
		g.setFont(FONT_DEFAULT);
		g.drawString("h", 7 * širina / 33, visina / 20);
		g.drawString(trenutnaVisinaLoptice(), 8 * širina / 33, visina / 20);
		// max visina loptice
		g.setColor(BOJA_VISINA_LOPTICE_MAX);
		g.setFont(FONT_DEFAULT);
		g.drawString(visinaMaxStr, 8 * širina / 33, visina / 13);

		// bodovi
		g.setColor(BOJA_BODOVI);
		g.setFont(FONT_BODOVI);
		g.drawString(bodoviStr, 3 * širina / 4, visina / 20);
		// max bodovi
		g.setColor(BOJA_BODOVI_MAX);
		g.setFont(FONT_BODOVI);
		g.drawString(bodoviMaxStr, 3 * širina / 4, visina / 13);

		// chat
		if (chatUključen) {
			iscrtajChat(g);
		}
		// FPS brojač
		g.setColor(Color.YELLOW);
		g.setFont(FONT_DEFAULT);
		g.drawString(Integer.toString(fps), širina - 32, 32);

		g.dispose();
		bufferStrategy.show();
	}

	private final void iscrtajSferu(final Graphics2D g, final Sfera s, final String ime) {
		g.setColor(Color.WHITE);
		g.fillOval(zaokruži(s.getX() - s.getR() - 1), zaokruži(s.getY() - s.getR() - 1), zaokruži(2 * s.getR() + 2),
				zaokruži(2 * s.getR() + 2));
		g.setColor(bojeCacheSvjetlije.get(s.getBoja()));
		g.fillOval(zaokruži(s.getX() - s.getR()), zaokruži(s.getY() - s.getR()), zaokruži(2 * s.getR()), zaokruži(2 * s
				.getR()));
		g.setColor(s.getBoja());
		g.fillOval(zaokruži(s.getX() - s.getR() + 8), zaokruži(s.getY() - s.getR() + 8), zaokruži(2 * s.getR() - 16),
				zaokruži(2 * s.getR() - 16));
		// ime igrača
		g.setColor(Color.BLACK);
		g.drawString(ime, zaokruži(s.getX() - 3.5 * ime.length()) + 4, zaokruži(s.getY() + 3));
	}

	private final void iscrtajChat(final Graphics2D g) {
		g.setFont(FONT_CHAT);

		g.setColor(BOJA_CHAT_VLASTITI);
		g.drawString(chatBufferStr, 80, 6 * visina / 7 + 20);

		if (poruke.size() == 0) {
			return;
		}

		final int brojPoruka = porukeCache.size();
		for (int i = 1; (i <= brojPoruka) && (i <= 10); i++) {
			final int deltaI = brojPoruka - i;
			// izračunaj alphu tako da od maksimuma oduzmemo starost poruke u ms podijeljenu sa konstantom
			final int alpha = 255 - (int) (System.currentTimeMillis() - porukeCacheVrijeme.get(deltaI)) / PORUKE_DECAY_RATE;
			if (alpha > 0) {
				final String poruka = porukeCache.get(deltaI);
				// TODO: cacheati boje
				g.setColor(new Color(poruke.get(deltaI).getIzvor().getSfera().getBoja().getRGB() + 32 + (32 << 8) + (32 << 16) + (alpha << 24),
						true));
				g.drawString(poruka, 8, 6 * visina / 7 - i * 10);
			}
		}
	}

	private final String trenutnaVisinaLoptice() {
		if (loptica.getY() > 0) {
			return STRING_NULA;
		}
		final int visinaLoptice = (int) -Math.round(loptica.getY());
		if (visinaLoptice > visinaMax) {
			visinaMax = visinaLoptice;
			visinaMaxStr = String.valueOf(visinaMax);
		}
		return Integer.toString(visinaLoptice);
	}

	private final String trenutnaBrzinaLoptice() {
		final long brzina = Math.abs(Math.round(KONSTANTA_BRZINE * loptica.getYv()));
		if (brzina > brzinaMax) {
			brzinaMax = brzina;
			brzinaMaxStr = String.valueOf(brzinaMax);
		}
		return Integer.toString((int) (-Math.signum(loptica.getYv()) * brzina));
	}

	/** Vraća zaokruženi int. */
	private final int zaokruži(final double broj) {
		return (int) Math.round(broj);
	}

	private final void izračunajFPS() {
		fpsBrojač++;
		if ((fpsBrojač & 15) == 15) {
			fps = 0;
			fpsAvg[fpsAvgNext++] = 1000000000 * ((double) fpsBrojač / (System.nanoTime() - fpsVrijemeNano));
			for (final double fpsA : fpsAvg) {
				fps += fpsA;
			}
			if (fpsAvgNext > (fpsAvg.length - 1)) {
				fpsAvgNext = 0;
			}
			fps = fps >> 4;
		}
	}

	public void setInit(final double radiusMax) {
		assert (inicijalizacija == false) : "radiusMax je već postavljen [" + radiusMax + "]!";
		this.radiusMax = faktor * radiusMax;
		sfera.setR(this.radiusMax);
		loptica.setR(faktor * klijent.getLoptica().getR());
		inicijalizacija = true;
	}

	/** Generira upitni dijalog koji služi za unos inicijalnih opcija. */
	private void initOpcije() {
		final DijalogOpcije opcije = new DijalogOpcije(this);

		igrač.setIme(opcije.getIme());
		igrač.getSfera().setBoja(opcije.getBoja());
		sfera.setBoja(opcije.getBoja());
		rezolucija = opcije.getRezolucija();
		// igrač.setIme("test");
		// igrač.getSfera().setBoja(Boja.vratiRandomBoju().getBoja());
		// sfera.setBoja(igrač.getSfera().getBoja());
		// rezolucija = new Dimension(640, 640);
		faktor = rezolucija.getWidth();
	}

	/** Inicijalizira glavni prozor. */
	private void initWindow() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(NASLOV_PROZORA);

		// setJMenuBar(new Izbornici());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				klijent.ugasiIgru();
			}
		});

		setResizable(false);
		okvir = new Canvas(gConfig);
		okvir.setSize(rezolucija);
		okvir.setIgnoreRepaint(true);
		okvir.setFocusTraversalKeysEnabled(false);
		širina = (int) rezolucija.getWidth();
		visina = (int) rezolucija.getHeight();
		add(okvir);
		pack();

		setLocationRelativeTo(null);
		setLocation(getLocation().x, 24);
		setIgnoreRepaint(true);
		setFocusTraversalKeysEnabled(false);

		setVisible(true);
		okvir.createBufferStrategy(2);
		bufferStrategy = okvir.getBufferStrategy();

		gfxKvaliteta = KVALITETA_MAX;
		promjenaGrafičkihPostavki();
	}


	@Override
	/** Prikazuje popup sa navedenim upitom te vraća korisnikov odabir. */
	public boolean prikažiUpit(final String poruka, final String naslov) {
		if (JOptionPane.showConfirmDialog(this, poruka, naslov, JOptionPane.YES_NO_OPTION) == 1) {
			return false;
		}
		return true;
	}

	@Override
	/** Prikazuje popup sa navedenom porukom o grešci. */
	public void prikažiGrešku(final String poruka, final Exception e) {
		String greška = poruka;
		if (e != null) {
			greška = poruka + "\n" + e.toString();
		}
		JOptionPane.showMessageDialog(this, greška, "Greška!", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	/** Prikazuje popup sa navedenom porukom i naslovom. */
	public void prikažiTekst(final String poruka, final String naslov) {
		JOptionPane.showMessageDialog(this, poruka, naslov, JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			igrač.getSfera().setA(true);
			pritisnutGumbDesni = false;
			break;
		case MouseEvent.BUTTON2:
			pritisnutGumbSrednji();
			break;
		case MouseEvent.BUTTON3:
			if (e.isAltDown()) {
				// simulacija srednje tipke
				pritisnutGumbSrednji();
			} else {
				igrač.getSfera().setA(true);
				pritisnutGumbDesni = !pritisnutGumbDesni;
			}
			break;
		}
	}

	private void pritisnutGumbSrednji() {
		pritisnutGumbSrednji = !pritisnutGumbSrednji;
		if (pritisnutGumbSrednji) {
			okvir.removeMouseMotionListener(this);
		} else {
			okvir.addMouseMotionListener(this);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (!pritisnutGumbDesni) {
			igrač.getSfera().setA(false);
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if ((e.getX() > 0) && (e.getX() < širina) && (e.getY() > 0) && (e.getY() < visina)) {
			sfera.setPos(e.getX(), e.getY());
			igrač.getSfera().setPos(e.getX() / faktor, e.getY() / faktor);
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F1) {
			// pomoć
			prikažiTekst(TEKST_HELP, "Pomoć");
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}

	@Override
	public void keyTyped(final KeyEvent e) {
		if (e.isAltDown()) {
			switch (e.getKeyChar()) {
			case 'a':
				if (isAlwaysOnTop()) {
					setAlwaysOnTop(false);
				} else {
					setAlwaysOnTop(true);
				}
				break;
			case 'g':
				promjenaGrafičkihPostavki();
				break;
			case 'e':
				crtajEfekte = !crtajEfekte;
				if (!crtajEfekte) {
					efekti.clear();
				}
				break;
			case 'p':
				// psihodelija
				crtajPozadinu = !crtajPozadinu;
				break;
			case 'c':
				chatUključen = !chatUključen;
				break;
			}
		} else {
			// chat
			switch (e.getKeyChar()) {
			case KeyEvent.VK_ENTER:
				if (chatBufferStr.trim().length() > 0) {
					chatDodaj(new Poruka(timestamp(), PorukaTip.CHAT_VLASTITI, igrač, chatBufferStr));
					klijent.chatPošalji(chatBufferStr);
					chatBuffer = new char[CHAT_BUFFER_MAX];
					chatBufferPos = 0;
					chatBufferStr = "";
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				if (chatBufferPos > 0) {
					chatBuffer[--chatBufferPos] = ' ';
				}
				chatBufferStr = new String(chatBuffer, 0, chatBufferPos);
				break;
			default:
				if (chatBufferPos > CHAT_BUFFER_MAX) {
					return;
				}
				chatBuffer[chatBufferPos++] = e.getKeyChar();
				chatBufferStr = new String(chatBuffer, 0, chatBufferPos);
			}
		}
	}

	/** Postavi grafičke postavke na minimum ili maksimum, ovisno o trenutnim postavkama. */
	private void promjenaGrafičkihPostavki() {
		if (gfxKvaliteta == KVALITETA_MAX) {
			gfxKvaliteta = KVALITETA_MIN;
			renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		} else if (gfxKvaliteta == KVALITETA_MIN) {
			gfxKvaliteta = KVALITETA_MAX;
			renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}
	}

	@Override
	public void setIgraAktivna(final boolean igraAktivna) {
		this.igraAktivna = igraAktivna;
	}

	@Override
	/** Osvježi lokalne kopije postavki. */
	public void promjenaPostavki() {
		// igrač.setIme((String) klijent.getPostavka(Postavke.IGRAČ_IME));
		sfera.set(igrač.getSfera().getX(), igrač.getSfera().getY(), igrač.getSfera().getR(), igrač.getSfera().getA());
	}


	@Override
	public void setBodovi(final long bodovi) {
		bodoviStr = String.valueOf(bodovi);
		if (bodovi > bodoviMax) {
			bodoviMax = bodovi;
			bodoviMaxStr = String.valueOf(bodoviMax);
		}
	}

	@Override
	/** Prikaži sudar sfere i loptice grafički. */
	public void sudar(final int id, final double x, final double y, final double jačina) {
		if (crtajEfekte) {
			efekti.add(new Sudar(x, y, jačina, igrači.get(id).getSfera().getBoja()));
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	// TODO: prebaciti većinu ovoga u općeniti Prikaz koji to dalje delegira..
	public void onPrimljenPaket(final DataInputStream paket) throws IOException {
		int id;
		switch (Akcije.get(paket.readByte())) {
		case PODACI_ČESTI:
			sfera.setR(faktor * paket.readFloat());
			loptica.set(faktor * paket.readFloat(), faktor * paket.readFloat(), faktor * paket.readFloat(), faktor * paket.readFloat());
			break;
		case PODACI_NISKI_PRIORITET:
			setBodovi(paket.readLong());
			while ((id = paket.readInt()) != -1) {
				if (sfere.containsKey(id)) {
					sfere.get(id).set(faktor * paket.readFloat(), faktor * paket.readFloat(), faktor * paket.readFloat(),
							paket.readBoolean());
				}
			}
			break;
		case SUDAR:
			sudar(paket.readInt(), faktor * paket.readDouble(), faktor * paket.readDouble(), faktor * paket.readDouble());
			break;
		case CHAT_PORUKA:
			id = paket.readInt();
			final Igrač i = igrači.get(id);
			if (i != null) {
				chatDodaj(new Poruka(paket.readLong(), PorukaTip.CHAT, i, paket.readUTF()));
			}
			break;
		case IGRAČ_SPOJEN:
			id = paket.readInt();
			final Sfera s = new Sfera(new Color(paket.readInt()),
					faktor * paket.readFloat(), faktor * paket.readFloat(), faktor * paket.readFloat(), false);
			sfere.put(id, s);
			bojeCacheSvjetlije.put(s.getBoja(), s.getBoja().brighter());
			break;
		case IGRAČ_ODSPOJEN:
			id = paket.readInt();
			sfere.remove(id);
			bojeCacheSvjetlije.remove(igrači.get(id).getSfera().getBoja());
			break;
		}
	}

	/** Dodaje zadanu poruku te se brine da je cache poruka ispravan. */
	private void chatDodaj(final Poruka poruka) {
		poruke.add(poruka);
		porukeCache.add(poruka.format(formatter));
		porukeCacheVrijeme.add(poruka.getTimestamp());
	}

	/** Osvježava cache poruka. */
	@SuppressWarnings("unused")
	private void chatOsvježi() {
		porukeCache.clear();
		for (final Poruka poruka : poruke) {
			porukeCache.add(poruka.format(formatter));
		}
	}

	/**
	 * Vraća timestamp generiran iz trenutnog vremena.
	 * 
	 * @return trenutni timestamp.
	 */
	private final long timestamp() {
		return Calendar.getInstance().getTimeInMillis();
	}


}
