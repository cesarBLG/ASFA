package dmi.Pantalla;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ecp.ASFA;
import ecp.Main;

public class VelocidadObjetivo extends JPanel {

    Velocidad value;
    JLabel Triángulo;
    JLabel Rectángulo;
    Icono IconoTriángulo;
    public int figureVisible;
    public int val;

    public VelocidadObjetivo() {
        setLayout(null);
        setOpaque(false);
        setBorder(null);
        value = new Velocidad(Color.white, Color.black);
        value.setBounds(45, 3, 75, 31);
        add(value);
        Rectángulo = new JLabel();
        Rectángulo.setBounds(42, 0, 81, 37);
        add(Rectángulo);
        Triángulo = new JLabel();
        Triángulo.setBounds(0, 0, 165, 72);
        add(Triángulo);
        IconoTriángulo = new Icono(true, "Triángulo.png");
        set(0, 0);
    }

    public void set(int visible, int val) {
        figureVisible = visible;
        this.val = val;
        update();
    }

    public void update() {
        if (figureVisible != 0) {
            value.setValue(val);
            value.setVisible(true);
            Rectángulo.setOpaque(true);
            Rectángulo.setBackground(Main.ASFA.dmi.pantalla.modo == ModoDisplay.Noche ? new Color(221, 221, 221) : Color.black);
        } else {
            value.setVisible(false);
            Rectángulo.setOpaque(false);
        }
        if (figureVisible == 2) {
            Triángulo.setIcon(IconoTriángulo.getIcon());
        } else {
            Triángulo.setIcon(null);
        }
        repaint();
    }
}
