package dmi.Botones;

import java.awt.*;

import javax.swing.*;

public class Pupitre extends JPanel {

    public Botón AnPar, PrePar, AnPre;

    public Pupitre() {
        setLayout(new GridBagLayout());
        ImageIcon[] anparic = new ImageIcon[2];
        anparic[0] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioParada.png"));
        anparic[1] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioParadaIluminado.png"));
        ImageIcon[] preparvlic = new ImageIcon[4];
        preparvlic[0] = new ImageIcon(getClass().getResource("/content/Botones/Preanuncio-VLCond.png"));
        preparvlic[1] = new ImageIcon(getClass().getResource("/content/Botones/PreanuncioIluminado.png"));
        preparvlic[2] = new ImageIcon(getClass().getResource("/content/Botones/VLCondIluminado.png"));
        preparvlic[3] = new ImageIcon(getClass().getResource("/content/Botones/PreanuncioVLCondIluminado.png"));
        ImageIcon[] anpreic = new ImageIcon[2];
        anpreic[0] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioPrecaucion.png"));
        anpreic[1] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioPrecaucionIluminado.png"));
        AnPar = new Botón(anparic, 2, Botón.TipoBotón.AnPar);
        AnPre = new Botón(anpreic, 2, Botón.TipoBotón.AnPre);
        PrePar = new Botón(preparvlic, 3, Botón.TipoBotón.PrePar);
        //setBackground(Color.blue);
        setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10,20,10,20);
        g.gridy = 0;
        g.gridx = 0;
        add(AnPar, g);
        g.gridx++;
        add(AnPre, g);
        g.gridx++;
        add(PrePar, g);
    }
}
