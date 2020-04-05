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
    public ModoDisplay modo = ModoDisplay.Día;
    public Velocidad vreal;
    public ÚltimaInfo info;
    public VelocidadObjetivo vtarget;
    public Eficacia eficacia;
    public InfoControles controles;
    public Intervención intervención;
    public ModeInfo ModoASFA;
    JLabel linea;
    public TipoTren tipoTren;
    public float scale = 1.95f /*1.2f*/;

    public int getScale(int val) {
    	
    	return Main.dmi.singleScreen ? (int)(val*scale) : val;
    }
    public Pantalla() {
        setSize(getScale(350), getScale(263));
        setMinimumSize(new Dimension(getScale(350), getScale(263)));
        setMaximumSize(new Dimension(getScale(350), getScale(263)));
        setPreferredSize(new Dimension(getScale(350), getScale(263)));
        setBackground(Color.black);
    }

    public void poweroff()
    {
    	Main.dmi.activo = false;
        removeAll();
        validate();
        repaint();
    }
    
    public void setup(int state, String msg) {
        removeAll();
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
    	Main.dmi.activo = true;
        removeAll();
        setLayout(null);
        info = new ÚltimaInfo();
        info.setBounds(getScale(271), getScale(0), getScale(79), getScale(263));
        add(info);
        vreal = new Velocidad(Color.black, Color.white);
        vreal.LeadingZeros = true;
        vreal.setBounds(getScale(16), getScale(42), getScale(75), getScale(31));
        add(vreal);
        vtarget = new VelocidadObjetivo();
        vtarget.setBounds(getScale(106), getScale(39), getScale(165), getScale(72));
        add(vtarget);
        linea = new JLabel();
        linea.setOpaque(true);
        linea.setBackground(Color.white);
        linea.setBounds(getScale(16), getScale(73), getScale(213), getScale(3));
        add(linea);
        eficacia = new Eficacia(false);
        eficacia.setBounds(getScale(16), getScale(202), getScale(19), getScale(19));
        add(eficacia);
        tipoTren = new TipoTren();
        tipoTren.setBounds(getScale(32), getScale(179), getScale(51), getScale(16));
        add(tipoTren);
        ModoASFA = new ModeInfo();
        ModoASFA.setBounds(getScale(32), getScale(202), getScale(51), getScale(16));
        add(ModoASFA);
        controles = new InfoControles();
        controles.setBounds(getScale(106), getScale(140), getScale(165), getScale(98));
        add(controles);
        intervención = new Intervención();
        intervención.setBounds(getScale(0), getScale(76), getScale(106), getScale(59));
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
