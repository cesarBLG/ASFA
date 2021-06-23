package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import dmi.DMI;
import dmi.Pantalla.Pantalla.ModoDisplay;
import ecp.ASFA;
import ecp.Config;
import ecp.Main;

class JDigit extends JLabel
{
	int i;
	public JDigit(int i) {this.i=i;} 
	@Override
	public void paint(Graphics g)
	{
		if (getIcon() == null) return;
		g.drawImage(((ImageIcon)getIcon()).getImage(), 0, 0, this);
	}
}

public class Velocidad extends JPanel {

    public int value=-1;
    Color Night;
    Color Day;

    boolean LeadingZeros=false;
    boolean Center;
    static Font fontdia = null;
    static Font fontnoche = null;
    JDigit[] digitos;
    ImageIcon[] icons_dia = new ImageIcon[10];
    ImageIcon[] icons_noche = new ImageIcon[10];
    ImageIcon[] icons;
    static float digits_height_dia;
    static float digits_height_noche;
    ImageIcon RenderDigit(int num, Color c, boolean noche)
    {
		BufferedImage bi = new BufferedImage(
				Main.dmi.pantalla.getScale(19), Main.dmi.pantalla.getScale(30), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(c);
        g2d.setFont(noche ? fontnoche : fontdia);
        FontMetrics fm = g2d.getFontMetrics();
        String text = Integer.toString(num);

        float sc = Main.dmi.pantalla.getScale(30)/(noche ? digits_height_noche : digits_height_dia);
        g2d.transform(AffineTransform.getScaleInstance(1, sc));
        g2d.drawString(text, (Main.dmi.pantalla.getScale(19)-fm.stringWidth(text))/2, Main.dmi.pantalla.getScale(30)/sc);
        g2d.dispose();
        return new ImageIcon(bi);
    }
    public Velocidad(Color day, Color night) {
    	if (fontdia == null)
    	{
    		fontdia = getFont(false);
    		fontnoche = getFont(true);
    	}
    	setOpaque(false);
        Night = night;
        Day = day;
        setLayout(null);  	
    	for (int i=0; i<10; i++)
    	{
	        icons_dia[i] = RenderDigit(i, Day, false);
	        icons_noche[i] = RenderDigit(i, Night, true);
    	}
    	icons = icons_dia;
    }
    
    public void construct() {
        digitos = new JDigit[Center ? 5 : 3];
    	for (int i=0; i<3; i++)
        {
        	digitos[i] = new JDigit(i);
        	/*digitos[i].setOpaque(true);
        	digitos[i].setBackground(Color.red);*/
        	digitos[i].setBounds(Main.dmi.pantalla.getScale(28*i), 0, Main.dmi.pantalla.getScale(19), Main.dmi.pantalla.getScale(30));
        	add(digitos[i]);
        }
    	if (Center)
    	{
        	for (int i=3; i<5; i++)
            {
            	digitos[i] = new JDigit(i);
            	/*digitos[i].setOpaque(true);
            	digitos[i].setBackground(Color.red);*/
            	digitos[i].setBounds(i == 3 ? Main.dmi.pantalla.getScale(14) : Main.dmi.pantalla.getScale(42), 0, Main.dmi.pantalla.getScale(19), Main.dmi.pantalla.getScale(30));
            	add(digitos[i]);
            }
    	}
        setValue(0);
    }

    public void update()
    {
    	//System.out.println(System.currentTimeMillis()+"update" + value);
    	icons = Main.dmi.pantalla.modo == ModoDisplay.Día ? icons_dia : icons_noche;
        int v1 = value / 100;
        int v2 = (value / 10) % 10;
        int v3 = value % 10;
        if (Center && v1==0 && v2>0) {
        	for (int i=0; i<3; i++)
        	{
        		digitos[i].setIcon(null);
        	}
        	digitos[3].setIcon(icons[v2]);
        	digitos[4].setIcon(icons[v3]);
        } else {
        	for (int i=3; i<digitos.length; i++)
        	{
        		digitos[i].setIcon(null);
        	}
        	if (Center && v1==0 && v2 == 0)
        	{
        		digitos[0].setIcon(null);
                digitos[1].setIcon(icons[v3]);
        		digitos[2].setIcon(null);
        	}
        	else
        	{
                if (v1 == 0 && !LeadingZeros) digitos[0].setIcon(null);
                else digitos[0].setIcon(icons[v1]);
                if (v2 == 0 && v1 == 0 && !LeadingZeros) digitos[1].setIcon(null);
                else digitos[1].setIcon(icons[v2]);
                digitos[2].setIcon(icons[v3]);
        	}
        }
    }
    public void setValue(int val) {
    	if (value == val) return;
        value = Math.min(Math.abs(val), 999);
        update();
    }
    public Font getFont(boolean noche)
    {
    	Font font;
    	int sz = Main.dmi.pantalla.getScale(30);
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File(noche ? "ASFA.ttf" : "ASFAB.ttf")).deriveFont((float)sz);
		} catch (FontFormatException | IOException e) {
			font = new Font("Lucida Sans", noche ? 0 : 1, sz);
			e.printStackTrace();
		}
		Rectangle2D r = font.createGlyphVector(getFontMetrics(font).getFontRenderContext(), "0").getVisualBounds();
		double ty = sz*sz/r.getHeight();
		double tx = sz*Main.dmi.pantalla.getScale(18)/r.getWidth();
		float digits_height;
		if (tx<ty) digits_height = (float)(tx*r.getHeight()/sz);
		else digits_height = sz;
		if (noche) digits_height_noche = digits_height;
		else digits_height_dia = digits_height;
		return font.deriveFont((float) Math.min(tx, ty));
    }
}
