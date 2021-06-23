package dmi.Pantalla;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ecp.Main;

public class Intervención extends JPanel {

    JLabel Frenado1;
    JLabel Frenado2;
    JLabel Urgencia;
    Icono IconoF1;
    Icono IconoF2;
    Icono IconoU;

    public Intervención() {
        Frenado1 = new JLabel();
        Frenado2 = new JLabel();
        Urgencia = new JLabel();
        Urgencia.setBounds(Main.dmi.pantalla.getScale(16), Main.dmi.pantalla.getScale(12), Main.dmi.pantalla.getScale(74), Main.dmi.pantalla.getScale(31));
        Frenado2.setBounds(Main.dmi.pantalla.getScale(16), Main.dmi.pantalla.getScale(12), Main.dmi.pantalla.getScale(47), Main.dmi.pantalla.getScale(47));
        Frenado1.setBounds(Main.dmi.pantalla.getScale(16), Main.dmi.pantalla.getScale(12), Main.dmi.pantalla.getScale(28), Main.dmi.pantalla.getScale(28));
        setOpaque(false);
        setLayout(null);
        add(Frenado1);
        add(Frenado2);
        add(Urgencia);
        IconoF1 = new Icono(false, "Frenado1.png");
        IconoF2 = new Icono(false, "Frenado2.png");
        IconoU = new Icono(false, "Urgencia.png");
        update();
    }
    public int frenado;
    public boolean urgencia;

    public void update(int f, boolean urg) {
    	if (frenado == f && urgencia == urg) return;
        frenado = f;
        urgencia = urg;
        update();
    }

    public void update() {
        if (urgencia) {
            Frenado1.setIcon(null);
            Frenado2.setIcon(null);
            Urgencia.setIcon(IconoU.getIcon());
        } else {
            Urgencia.setIcon(null);
            switch (frenado) {
                case 0:
                    Frenado1.setIcon(null);
                    Frenado2.setIcon(null);
                    break;
                case 1:
                    Frenado1.setIcon(IconoF1.getIcon());
                    Frenado2.setIcon(null);
                    break;
                case 2:
                    Frenado1.setIcon(null);
                    Frenado2.setIcon(IconoF2.getIcon());
                    break;
            }
        }
    }
}
