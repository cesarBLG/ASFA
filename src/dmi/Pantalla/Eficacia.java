package dmi.Pantalla;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import dmi.DMI;
import ecp.ASFA;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Eficacia extends JLabel {

    int state = 0;
    String Fase;
    boolean eficacia;
    Icono[] iconos;

    public Eficacia(boolean Fase2) {
        if (Fase2) {
            Fase = "Eficacia/Fase2/";
        } else {
            Fase = "Eficacia/Fase1/";
        }
        Timer t = new Timer(500, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (eficacia) {
                    switchstate();
                }
            }
        });
        iconos = new Icono[8];
        for (int i = 0; i < 8; i++) {
            iconos[i] = new Icono(true, Fase.concat(Integer.toString(i).concat(".png")));
        }
        set(true);
        t.setRepeats(true);
        t.start();
    }

    public void set(boolean e) {
        if (eficacia == e) {
            return;
        }
        eficacia = e;
        if (eficacia) {
            switchstate();
        } else {
            setIcon(null);
        }
    }

    void switchstate() {
        state++;
        state = state % 8;
        setIcon(iconos[state].getIcon());
    }
}
