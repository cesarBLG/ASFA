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
    boolean mostrar = false;
    boolean eficacia = false;
    boolean fase2 = false;
    Icono[] iconos;

    public Eficacia() {
    	Main.dmi.pantalla.Blinker.Blinker2Hz.add((e) -> {
    		if (eficacia) {
    			switchstate();
    		}
    	});
        this.setVerticalAlignment(JLabel.BOTTOM);
        iconos = new Icono[8];
        fase(false);
    }
    public void fase(boolean Fase2)
    {
    	fase2 = Fase2;
    	String Fase = "Eficacia/";
        if (Fase2) {
            Fase += "Fase2/";
        } else {
            Fase += "Fase1/";
        }
        for (int i = 0; i < 8; i++) {
            iconos[i] = new Icono(true, Fase.concat(Integer.toString(i).concat(".png")));
        }
        if (eficacia || mostrar) setIcon(iconos[state].getIcon());
        else setIcon(null);
    }

    public void set(boolean e, boolean mos) {
        if (eficacia == e && mos == mostrar) {
            return;
        }
        eficacia = e;
        mostrar = mos;
        if (eficacia) {
            switchstate();
        } else {
        	if (!mostrar)
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
