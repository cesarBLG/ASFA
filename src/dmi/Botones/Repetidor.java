package dmi.Botones;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class Repetidor extends JPanel {

    public Botón Modo, Rearme, Rebase, AumVel, Alarma, Ocultación, LVI, PN;

    public Repetidor() {
        ImageIcon[] iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Modo.png"));
        iconos[1] = iconos[0];
        Modo = new Botón(iconos, 2, Botón.TipoBotón.Modo);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Rearme.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/RearmeIluminado.png"));
        Rearme = new Botón(iconos, 2, Botón.TipoBotón.Rearme);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Rebase.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/RebaseIluminado.png"));
        Rebase = new Botón(iconos, 2, Botón.TipoBotón.Rebase);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/AumVel.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/AumVelIluminado.png"));
        AumVel = new Botón(iconos, 2, Botón.TipoBotón.AumVel);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Alarma.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/AlarmaIluminado.png"));
        Alarma = new Botón(iconos, 2, Botón.TipoBotón.Alarma);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Ocultación.png"));
        iconos[1] = iconos[0];
        Ocultación = new Botón(iconos, 2, Botón.TipoBotón.Ocultación);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/LVI.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/LVIIluminado.png"));
        LVI = new Botón(iconos, 2, Botón.TipoBotón.LVI);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/PN.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/PNIluminado.png"));
        PN = new Botón(iconos, 2, Botón.TipoBotón.PN);
        setLayout(new GridLayout(2, 5));
        setBackground(Color.blue);
        JButton Conex = new JButton("Conex");
        add(Conex);
        add(Modo);
        add(Rearme);
        add(Rebase);
        add(AumVel);
        add(new JButton("ASFA "));
        add(Alarma);
        add(Ocultación);
        add(LVI);
        add(PN);
    }
}
