package dmi.Pantalla;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import dmi.DMI;
import ecp.ASFA;
import ecp.Config;
import ecp.Main;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Eficacia extends JLabel {

    int state = 0;
    boolean eficacia = false;
    Icono[] iconos;

    public Eficacia() {
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
        create(false);
        t.setRepeats(true);
        t.start();
    }
    public void create(boolean Fase2)
    {
    	String Fase = "Eficacia/";
        if (Fase2) {
            Fase += "Fase2/";
        } else {
            Fase += "Fase1/";
        }
        for (int i = 0; i < 8; i++) {
            iconos[i] = new Icono(true, Fase.concat(Integer.toString(i).concat(".png")));
        }
        if (eficacia || !Config.Fabricante.equals("")) setIcon(iconos[state].getIcon());
        else setIcon(null);
    }

    public void set(boolean e) {
        if (eficacia == e) {
            return;
        }
        eficacia = e;
        if (eficacia) {
            switchstate();
        } else {
        	if (Config.Fabricante.equals(""))
			{
                setIcon(null);
			}
        	else
        	{
                setIcon(iconos[state].getIcon());
        	}
        }
    }

    void switchstate() {
    	if(!eficacia) return;
        state++;
        state = state % 8;
        setIcon(iconos[state].getIcon());
    }
}
