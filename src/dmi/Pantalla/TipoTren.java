package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import ecp.ASFA;
import ecp.Main;

public class TipoTren extends JLabel {

    String tipo = "";

    public TipoTren() {
        setHorizontalAlignment(JLabel.LEFT);
        try {
            setFont(Font.createFont(Font.TRUETYPE_FONT, new File("HelveticaCdBd.ttf")).deriveFont((float)Main.dmi.pantalla.getScale(15)));
        } catch(FontFormatException | IOException e) {
        	setFont(new Font("Arial Narrow", 1, Main.dmi.pantalla.getScale(15)));
        }
        setForeground(Color.white);
        update();
    }

    public void set(int T) {
        tipo = "T".concat(Integer.toString(T));
        update();
    }

    public void update() {
        setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Color.black : Color.white);
        String t = "";
        for (int i=0; i<tipo.length(); i++)
        {
        	t += tipo.charAt(i);
        	if (tipo.length()>i+1) t += " ";
        }
        setText(t);
    }
}
