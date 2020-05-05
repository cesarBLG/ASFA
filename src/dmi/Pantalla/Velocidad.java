package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import dmi.DMI;
import ecp.ASFA;
import ecp.Main;

public class Velocidad extends JLabel {

    int value=-1;
    Color Night;
    Color Day;

    boolean LeadingZeros=false;
    static Font font = null;
    public Velocidad(Color day, Color night) {
    	if (font == null)
    	{
    		try {
				font = Font.createFont(Font.TRUETYPE_FONT, new File(Main.dmi.fabricante+".ttf")).deriveFont((float)Main.dmi.pantalla.getScale(35));
			} catch (FontFormatException | IOException e) {
				font = new Font("Lucida Sans", 0, Main.dmi.pantalla.getScale(35));
				e.printStackTrace();
			}
    	}
        setBorder(null);
        Night = night;
        Day = day;
        setFont(font);
        setHorizontalAlignment(RIGHT);
        setValue(0);
    }

    public void setValue(int val) {
        setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Day : Night);
    	if (value == val) return;
        value = val;
        int v1 = value / 100;
        int v2 = (value / 10) % 10;
        int v3 = value % 10;
        String text = new String("");
        if (v1 != 0 || LeadingZeros) {
            text = text.concat(Integer.toString(v1))/*.concat(" ")*/;
        }
        if (v1 != 0 || v2 != 0 || LeadingZeros) {
            text = text.concat(Integer.toString(v2))/*.concat(" ")*/;
        }
        text = text.concat(Integer.toString(v3));
        setText(text);
        repaint(50);
    }
}
