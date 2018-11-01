package dmi.Botones;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.COM;

import dmi.DMI;
import ecp.ASFA;
import ecp.Clock;
import ecp.Main;

public class Botón extends JButton {

    public enum TipoBotón {
        AumVel,
        Modo,
        PN,
        Rebase,
        Rearme,
        Alarma,
        Ocultación,
        LVI,
        Conex,
        ASFA_básico,
        AnPar,
        AnPre,
        PrePar,
        VLCond
    }
    public TipoBotón tipo;
    public static Botón[] ListaBotones = new Botón[14];
    int Iluminado;
    ImageIcon[] Icons;
    int NumIcons;

    Botón(ImageIcon[] icons, int count, TipoBotón tipo) {
        super();
        this.tipo = tipo;
        ListaBotones[tipo.ordinal()] = this;
        if (tipo == tipo.PrePar) {
            ListaBotones[TipoBotón.VLCond.ordinal()] = this;
        }
        setBorderPainted(false);
        this.setBackground(Color.BLUE);
        Botón b = this;
        Icons = icons;
        NumIcons = count;
        iluminar(0);
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getButton() == MouseEvent.BUTTON3) {
                    iluminar(1);
                }
                if (arg0.getButton() == MouseEvent.BUTTON1) {
                	COM.parse(10, (tipo.ordinal()<<1) | 1);
                    if (tipo == TipoBotón.PrePar) {
                    	COM.parse(10, (TipoBotón.VLCond.ordinal()<<1) | 1);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getButton() == MouseEvent.BUTTON3) {
                    iluminar(0);
                }
                if (arg0.getButton() == MouseEvent.BUTTON1) {
                	COM.parse(10, (tipo.ordinal()<<1) | 0);
                    if (tipo == TipoBotón.PrePar) {
                    	COM.parse(10, (TipoBotón.VLCond.ordinal()<<1) | 0);
                    }
                }
            }
        });
    }

    public void iluminar(int num) {
        if (num >= Icons.length) {
            return;
        }
        Iluminado = num;
        setIcon(Icons[num]);
    }

    public static void apagar() {
        for (Botón b : ListaBotones) {
            if (b == null) {
                continue;
            }
            b.iluminar(0);
        }
    }
}
