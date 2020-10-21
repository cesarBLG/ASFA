package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import dmi.Pantalla.Pantalla.ModoDisplay;
import ecp.ASFA;
import ecp.ASFA.Modo;
import ecp.Main;

public class ModeInfo extends JLabel {

    public ModeInfo() {
        setHorizontalAlignment(JLabel.LEFT);
        try {
            setFont(Font.createFont(Font.TRUETYPE_FONT, new File("HelveticaCdBd.ttf")).deriveFont((float)Main.dmi.pantalla.getScale(15)));
        } catch(FontFormatException | IOException e) {
        	setFont(new Font("Arial Narrow", 1, Main.dmi.pantalla.getScale(15)));
        }
        setForeground(Color.white);
        update();
    }

    public void update(Modo m) {
        String t = "";
        String mod = m.name();
        for (int i=0; i<mod.length(); i++)
        {
        	t += mod.charAt(i);
        	if (mod.length()>i+1) t += " ";
        }
        setText(t);
        setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Color.black : Pantalla.blanco);
    }
    public void update() {
        setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Color.black : Pantalla.blanco);
    }
}
