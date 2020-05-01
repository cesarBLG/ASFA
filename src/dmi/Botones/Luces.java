package dmi.Botones;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import dmi.Pantalla.ÚltimaInfo.Info;

public class Luces extends JPanel {
	ImageIcon rojo;
	ImageIcon verde;
	int leds[] = new int[3];
	JLabel eficacia;
	JLabel frenar;
	JLabel multi;
	public Luces()
	{
		setOpaque(false);
		eficacia = new JLabel();
        eficacia.setDisabledIcon(new ImageIcon(getClass().getResource("/content/LED/Apagado.png")));
		eficacia.setIcon(new ImageIcon(getClass().getResource("/content/LED/Eficacia.png")));
        eficacia.setEnabled(false);
		frenar = new JLabel();
		frenar.setDisabledIcon(new ImageIcon(getClass().getResource("/content/LED/Apagado.png")));
		frenar.setIcon(new ImageIcon(getClass().getResource("/content/LED/Frenar.png")));
		frenar.setEnabled(false);
		multi = new JLabel();
		multi.setDisabledIcon(new ImageIcon(getClass().getResource("/content/LED/Apagado.png")));
		rojo = new ImageIcon(getClass().getResource("/content/LED/Rojo.png"));
		verde = new ImageIcon(getClass().getResource("/content/LED/L2.png"));
		multi.setEnabled(false);
		JLabel labelef = new JLabel("Eficacia");
		JLabel labelfr = new JLabel("Frenar");
		JLabel labelasfa = new JLabel("<html>\u25c0ASFA<br/>Básico</html>");
		labelef.setForeground(Color.white);
		labelfr.setForeground(Color.white);
		labelasfa.setForeground(Color.white);
		setLayout(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0,0,6,5);
        g.anchor = GridBagConstraints.WEST;
		g.gridx = g.gridy = 0;
		add(eficacia, g);
		g.gridy++;
		add(frenar, g);
		g.gridy++;
		add(multi, g);
		g.gridx = g.gridy = 0;
		g.gridx++;
		add(labelef, g);
		g.gridy++;
		add(labelfr, g);
		g.gridx = 0;
		g.gridy = 3;
		g.gridwidth = 2;
		add(labelasfa, g);
		new Timer(500, new ActionListener() {
            boolean state = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (leds[0]==2) eficacia.setEnabled(state);
                if (leds[1]==2) frenar.setEnabled(state);
                if ((leds[2]&2) != 0) multi.setEnabled(state);
                state = !state;
            }
        }).start();
	}
	public void update(int led, int state)
	{
		leds[led] = state;
		if (led == 0) eficacia.setEnabled(state!=0);
		if (led == 1) frenar.setEnabled(state!=0);
		if (led == 2)
		{
			multi.setIcon(state > 2 ? rojo : verde);
			multi.setEnabled(state!=0);
		}
		repaint();
	}
}
