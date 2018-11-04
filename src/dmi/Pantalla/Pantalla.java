package dmi.Pantalla;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import dmi.DMI;
import ecp.ASFA;
import ecp.Main;

enum ModoDisplay {
    Día,
    Noche
}

public class Pantalla extends JPanel {

    public ModoDisplay modo = ModoDisplay.Noche;
    public Velocidad vreal;
    public ÚltimaInfo info;
    public VelocidadObjetivo vtarget;
    public Eficacia eficacia;
    public InfoControles controles;
    public Intervención intervención;
    public ModeInfo ModoASFA;
    JLabel linea;
    public TipoTren tipoTren;

    public Pantalla() {
        setSize(350, 263);
        setMinimumSize(new Dimension(350, 263));
        setMaximumSize(new Dimension(350, 263));
        setPreferredSize(new Dimension(350, 263));
        setBackground(Color.black);
    }

    public void setup(int state, String msg) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel j;
        switch (state) {
            case 0:
                j = new JLabel("ASFA OK");
                j.setForeground(Color.green);
                break;
            case 1:
                j = new JLabel("ASFA-operativo");
                j.setForeground(Color.yellow);
                break;
            default:
                j = new JLabel("ASFA no operativo");
                j.setForeground(Color.red);
                break;
        }
        setBackground(Color.black);
        add(j);
        JLabel m = new JLabel(msg);
        m.setForeground(Color.white);
        add(m);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        j = new JLabel(df.format(date));

        j.setForeground(Color.white);
        add(j);
        validate();
    }

    public void start() {
        removeAll();
        setLayout(null);
        info = new ÚltimaInfo();
        info.setBounds(271, 0, 79, 263);
        add(info);
        vreal = new Velocidad(Color.black, Color.white);
        vreal.setBounds(16, 42, 75, 31);
        add(vreal);
        vtarget = new VelocidadObjetivo();
        vtarget.setBounds(106, 39, 165, 72);
        add(vtarget);
        linea = new JLabel();
        linea.setOpaque(true);
        linea.setBackground(Color.white);
        linea.setBounds(16, 73, 213, 3);
        add(linea);
        eficacia = new Eficacia(false);
        eficacia.setBounds(16, 202, 19, 19);
        add(eficacia);
        tipoTren = new TipoTren();
        tipoTren.setBounds(32, 179, 51, 16);
        add(tipoTren);
        ModoASFA = new ModeInfo();
        ModoASFA.setBounds(32, 202, 51, 16);
        add(ModoASFA);
        controles = new InfoControles();
        controles.setBounds(106, 140, 165, 98);
        add(controles);
        intervención = new Intervención();
        intervención.setBounds(0, 76, 106, 59);
        add(intervención);
        set(ModoDisplay.Noche);
        repaint();
    }

    public void set(ModoDisplay m) {
        modo = m;
        setBackground(modo == ModoDisplay.Noche ? Color.black : Color.white);
        vreal.setValue(vreal.value);
        info.update();
        vtarget.update();
        eficacia.switchstate();
        ModoASFA.update();
        controles.update();
        linea.setBackground(modo == ModoDisplay.Día ? Color.black : Color.white);
        tipoTren.update();
    }

    public void set() {
        set(modo == ModoDisplay.Día ? ModoDisplay.Noche : ModoDisplay.Día);
    }
}
