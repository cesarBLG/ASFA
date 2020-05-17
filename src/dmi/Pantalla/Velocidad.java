package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import dmi.DMI;
import ecp.ASFA;
import ecp.Main;

public class Velocidad extends JPanel {

    int value=-1;
    Color Night;
    Color Day;

    boolean LeadingZeros=false;
    boolean Center;
    static Font font = null;
    JLabel[] digitos = new JLabel[3];
    public Velocidad(Color day, Color night) {
    	this.Center = Center;
    	if (font == null)
    	{
    		try {
				font = Font.createFont(Font.TRUETYPE_FONT, new File(Main.dmi.fabricante+".ttf")).deriveFont((float)Main.dmi.pantalla.getScale(37));
			} catch (FontFormatException | IOException e) {
				font = new Font("Lucida Sans", 0, Main.dmi.pantalla.getScale(37));
				e.printStackTrace();
			}
    	}
    	setOpaque(false);
        Night = night;
        Day = day;
        setLayout(null);
    }
    
    public void construct() {
        if (Center)
        {
        	digitos[0] = new JLabel();
        	digitos[0].setOpaque(false);
        	digitos[0].setFont(font);
        	digitos[0].setBounds(0, 0, getWidth(), getHeight());
        	add(digitos[0]);
        	digitos[0].setHorizontalAlignment(JLabel.CENTER);
        }
        else
        {
            for (int i=0; i<3; i++)
            {
            	digitos[i] = new JLabel();
            	digitos[i].setOpaque(false);
            	digitos[i].setFont(font);
            	digitos[i].setBounds((int)(getWidth()/3.0*i), 0, (int)(getWidth()/3.0), getHeight());
            	add(digitos[i]);
            	digitos[i].setHorizontalAlignment(JLabel.RIGHT);
            }
        	digitos[0].setHorizontalAlignment(JLabel.RIGHT);
        	digitos[1].setHorizontalAlignment(JLabel.CENTER);
        	digitos[2].setHorizontalAlignment(JLabel.LEFT);
        }
        setValue(0);
    }

    public void setValue(int val) {
    	if (value == val) return;
        value = val;
        int v1 = value / 100;
        int v2 = (value / 10) % 10;
        int v3 = value % 10;
        if (Center) {
            digitos[0].setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Day : Night);
        	String text = "";
        	if (v1 != 0) text += Integer.toString(v1);
        	if (v2 != 0 || v1!= 0 ) text += Integer.toString(v2);
        	text += Integer.toString(v3);
        	digitos[0].setText(text);
        	digitos[0].repaint(50);
        }
        else {
            digitos[0].setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Day : Night);
            digitos[1].setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Day : Night);
            digitos[2].setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Day : Night);
            if (v1 == 0 && !LeadingZeros) digitos[0].setText("");
            else digitos[0].setText(Integer.toString(v1));
            if (v2 == 0 && v1 == 0 && !LeadingZeros) digitos[1].setText("");
            else digitos[1].setText(Integer.toString(v2));
            digitos[2].setText(Integer.toString(v3));
        	digitos[0].repaint(50);
        	digitos[1].repaint(50);
        	digitos[2].repaint(50);
        }
    }
}
