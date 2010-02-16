package hr.sandrogrzicic.igre.spheres.klijent.prikaz;

import hr.sandrogrzicic.igre.spheres.klijent.Klijent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;


class DijalogOpcije extends JDialog {
	private static final long serialVersionUID = -6655441278930411136L;
	private final JTextField imeText;
	private final JColorChooser bojaChooser;
	private String igračIme;
	private Color boja;
	private Dimension rezolucija;
	// private final JFormattedTextField rezCustomŠirina;
	// private final JFormattedTextField rezCustomVisina;
	private final JComboBox rezolucijaCombo;
	private final JFormattedTextField rezolucijaCustom;

	public DijalogOpcije(final PrikazSpheresJFrame klijentPrikaz) {
		super(klijentPrikaz, PrikazSpheresJFrame.NASLOV_PROZORA, true);
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final Box komponente = new Box(BoxLayout.Y_AXIS);
		final Box imeBox = new Box(BoxLayout.X_AXIS);
		final JLabel imeLabel = new JLabel("Ime: ");
		imeText = new JTextField(32);
		imeLabel.setPreferredSize(new Dimension(64, imeLabel.getHeight()));
		imeBox.add(Box.createHorizontalStrut(8));
		imeBox.add(imeLabel);
		imeBox.add(imeText);
		imeBox.add(Box.createGlue());
		komponente.add(imeBox);

		final Box bojaSfereBox = new Box(BoxLayout.X_AXIS);
		final JLabel bojaSfereLabel = new JLabel("Boja sfere: ");
		bojaChooser = new JColorChooser(Boja.vratiRandomBoju().getBoja());
		bojaChooser.setPreviewPanel(null);
		bojaSfereLabel.setPreferredSize(new Dimension(64, bojaSfereLabel.getHeight()));
		bojaSfereBox.add(Box.createHorizontalStrut(8));
		bojaSfereBox.add(bojaSfereLabel);
		bojaSfereBox.add(Box.createGlue());
		bojaSfereBox.add(bojaChooser);
		komponente.add(bojaSfereBox);

		final Box rezolucijaBox = new Box(BoxLayout.X_AXIS);
		final JLabel rezolucijaLabel = new JLabel("Rezolucija: ");
		rezolucijaCombo = new JComboBox(Rezolucija.values());
		rezolucijaCombo.setSelectedIndex(2);
		rezolucijaCombo.setMaximumSize(rezolucijaCombo.getPreferredSize());
		rezolucijaCustom = new JFormattedTextField(NumberFormat.getIntegerInstance());
		rezolucijaCustom.setColumns(5);
		rezolucijaCustom.setMaximumSize(rezolucijaCustom.getPreferredSize());
		// rezCustomŠirina = new JFormattedTextField(NumberFormat.getIntegerInstance());
		// rezCustomVisina = new JFormattedTextField(NumberFormat.getIntegerInstance());
		// rezCustomŠirina.setColumns(5);
		// rezCustomVisina.setColumns(5);
		// rezCustomŠirina.setMaximumSize(rezCustomŠirina.getPreferredSize());
		// rezCustomVisina.setMaximumSize(rezCustomVisina.getPreferredSize());
		rezolucijaLabel.setPreferredSize(new Dimension(64, rezolucijaLabel.getHeight()));
		rezolucijaBox.add(Box.createHorizontalStrut(8));
		rezolucijaBox.add(rezolucijaLabel);
		rezolucijaBox.add(rezolucijaCombo);
		rezolucijaBox.add(Box.createHorizontalStrut(16));
		rezolucijaBox.add(rezolucijaCustom);
		// rezolucijaBox.add(rezCustomŠirina);
		// rezolucijaBox.add(Box.createHorizontalStrut(8));
		// rezolucijaBox.add(rezCustomVisina);
		rezolucijaBox.add(Box.createHorizontalGlue());
		komponente.add(rezolucijaBox);
		komponente.add(Box.createVerticalStrut(16));

		final Box gumbiBox = new Box(BoxLayout.X_AXIS);

		final JButton OK = new JButton("OK");
		OK.setMaximumSize(new Dimension(100, 30));
		OK.setAlignmentX(CENTER_ALIGNMENT);
		OK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				procesirajUlaz();
			}
		});

		final JButton izlaz = new JButton("Izlaz");
		izlaz.setMaximumSize(new Dimension(100, 30));
		izlaz.setAlignmentX(CENTER_ALIGNMENT);
		izlaz.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				System.exit(0);
			}
		});

		gumbiBox.add(OK);
		gumbiBox.add(Box.createHorizontalStrut(32));
		gumbiBox.add(izlaz);
		komponente.add(gumbiBox);

		add(komponente);

		rootPane.setDefaultButton(OK);
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Procesira parametre koje je korisnik unio. Zove dispose() ukoliko je procesiranje uspješno.
	 */
	protected void procesirajUlaz() {
		igračIme = imeText.getText();
		if ((igračIme == null) || (igračIme.trim().length() == 0)) {
			igračIme = Integer.toString(1000 + (int) (9000 * Math.random()));
		}
		if (igračIme.length() > Klijent.IME_MAX_LENGTH) {
			igračIme = igračIme.substring(0, Klijent.IME_MAX_LENGTH);
		}

		boja = bojaChooser.getColor();
		// test za pretamnu ili presvijetlu boju
		final float[] komponente = Color.RGBtoHSB(boja.getRed(), boja.getGreen(), boja.getBlue(), null);
		if (komponente[2] < 0.3f) {
			komponente[2] = 0.3f;
		}
		if (komponente[2] > 0.8f) {
			komponente[2] = 0.8f;
		}
		boja = new Color(Color.HSBtoRGB(komponente[0], komponente[1], komponente[2]));

		try {
			if (rezolucijaCombo.getSelectedIndex() == rezolucijaCombo.getItemCount() - 1) {
				final int w = Integer.parseInt(rezolucijaCustom.getText());
				final int h = w;
				if ((w < 240)) {
					rezolucijaCustom.setBackground(Color.RED);
					return;
				}
				rezolucija = new Dimension(w, h);
			} else {
				rezolucija = ((Rezolucija) rezolucijaCombo.getSelectedItem()).rezolucija();
			}
		} catch (final NumberFormatException ignorable) {
			return;
		}

		dispose();
	}

	@Override
	protected void processWindowEvent(final WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			procesirajUlaz();
		}
	}

	public String getIme() {
		return igračIme;
	}

	public Color getBoja() {
		return boja;
	}

	public Dimension getRezolucija() {
		return rezolucija;
	}

}
