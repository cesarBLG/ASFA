package dmi.Botones;

import java.awt.*;

import javax.swing.*;

public class Pupitre extends JPanel {

    public Botón AnPar, PrePar, AnPre;

    public Pupitre() {
        setLayout(new GridLayout(1, 3));
        ImageIcon[] anparic = new ImageIcon[2];
        anparic[0] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioParada.png"));
        anparic[1] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioParadaIluminado.png"));
        ImageIcon[] preparvlic = new ImageIcon[3];
        preparvlic[0] = new ImageIcon(getClass().getResource("/content/Botones/Preanuncio-VLCond.png"));
        preparvlic[1] = new ImageIcon(getClass().getResource("/content/Botones/PreanuncioIluminado.png"));
        preparvlic[2] = new ImageIcon(getClass().getResource("/content/Botones/VLCondIluminado.png"));
        ImageIcon[] anpreic = new ImageIcon[2];
        anpreic[0] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioPrecaucion.png"));
        anpreic[1] = new ImageIcon(getClass().getResource("/content/Botones/AnuncioPrecaucionIluminado.png"));
        AnPar = new Botón(anparic, 2, Botón.TipoBotón.AnPar);
        AnPre = new Botón(anpreic, 2, Botón.TipoBotón.AnPre);
        PrePar = new Botón(preparvlic, 3, Botón.TipoBotón.PrePar);
        setBackground(Color.blue);
        add(AnPar);
        add(AnPre);
        add(PrePar);
    }
}
