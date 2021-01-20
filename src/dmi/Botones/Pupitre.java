package dmi.Botones;

import java.awt.*;

import javax.swing.*;

public class Pupitre extends JPanel {

    public Botón AnPar, PrePar, AnPre;

    public Pupitre() {
    	String carpeta = "/content/Botones/";
        setLayout(new GridBagLayout());
        ImageIcon[] anparic = new ImageIcon[2];
        anparic[0] = new ImageIcon(getClass().getResource(carpeta+"AnuncioParada.png"));
        anparic[1] = new ImageIcon(getClass().getResource(carpeta+"AnuncioParadaIluminado.png"));
        ImageIcon[] preparvlic = new ImageIcon[4];
        preparvlic[0] = new ImageIcon(getClass().getResource(carpeta+"Preanuncio-VLCond.png"));
        preparvlic[1] = new ImageIcon(getClass().getResource(carpeta+"PreanuncioIluminado.png"));
        preparvlic[2] = new ImageIcon(getClass().getResource(carpeta+"VLCondIluminado.png"));
        preparvlic[3] = new ImageIcon(getClass().getResource(carpeta+"PreanuncioVLCondIluminado.png"));
        ImageIcon[] anpreic = new ImageIcon[2];
        anpreic[0] = new ImageIcon(getClass().getResource(carpeta+"AnuncioPrecaucion.png"));
        anpreic[1] = new ImageIcon(getClass().getResource(carpeta+"AnuncioPrecaucionIluminado.png"));
        AnPar = new Botón(anparic, 2, Botón.TipoBotón.AnPar);
        AnPre = new Botón(anpreic, 2, Botón.TipoBotón.AnPre);
        PrePar = new Botón(preparvlic, 3, Botón.TipoBotón.PrePar);
        //setBackground(Color.blue);
        setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10,10,10,10);
        g.gridy = 0;
        g.gridx = 0;
        add(AnPar, g);
        g.gridx++;
        add(AnPre, g);
        g.gridx++;
        add(PrePar, g);
    }
}
