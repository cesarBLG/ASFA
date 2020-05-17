package dmi.Pantalla;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import dmi.DMI;
import ecp.ASFA;
import ecp.Main;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ÚltimaInfo extends JPanel {

    public enum Info {
        Apagado,
        Desconocido,
        Parada,
        Rebase,
        Anuncio_parada,
        Anuncio_precaución,
        Preanuncio,
        Preanuncio_AV,
        Vía_libre_condicional,
        Vía_libre
    }
    Info info;
    JLabel j;
    Timer t;
    boolean blink;
    Icono[] iconos;

    @Override
    public void setBounds(int arg0, int arg1, int arg2, int arg3) {
        super.setBounds(arg0, arg1, arg2, arg3);
        update();
    }

    public ÚltimaInfo() {
        j = new JLabel();
        t = new Timer(500, new ActionListener() {
            boolean state = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
            	if(info == Info.Vía_libre_condicional)
            	{
                    if (state) {
                        j.setIcon(iconos[Info.Vía_libre_condicional.ordinal()].getIcon());
                    } else {
                        j.setIcon(iconos[Info.Apagado.ordinal()].getIcon());
                    }
            	}
            	else if(blink)
            	{
            		if (state) {
                        j.setIcon(iconos[info.ordinal()].getIcon());
                    } else {
                        j.setIcon(iconos[Info.Vía_libre.ordinal()].getIcon());
                    }
            	}
                state = !state;
            }
        });
        setLayout(null);
        setBackground(new Color(150, 150, 150));
        add(j);
        int num = Info.values().length;
        iconos = new Icono[num];
        for (int i = 0; i < num; i++) {
            iconos[i] = new Icono(true, Info.values()[i].name().concat(".png"));
        }
        setInfo(Info.Vía_libre, false);
    }

    public void update() {
        if (Main.dmi.pantalla.modo == ModoDisplay.Noche && info != Info.Vía_libre) {
            setOpaque(true);
            this.repaint();
        } else {
            setOpaque(false);
            this.repaint();
        }
        j.setIcon(iconos[info.ordinal()].getIcon());
        if (info != Info.Vía_libre_condicional && !blink) {
            if (t.isRunning()) {
                t.stop();
            }
            t.setRepeats(false);
        } else {
            t.start();
            t.setRepeats(true);
        }
        if (j.getIcon() != null) {
            j.setBounds(getWidth() / 2 - j.getIcon().getIconWidth() / 2, 42, j.getIcon().getIconWidth(), j.getIcon().getIconHeight());
        }
    }

    public void setInfo(Info i, boolean blink) {
        info = i;
        this.blink = blink;
        update();
    }
}
