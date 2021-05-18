package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import dmi.Pantalla.Pantalla.ModoDisplay;
import ecp.Config;
import ecp.Main;

public abstract class TipoModo extends JLabel {
	class JCharacter extends JLabel
	{
		@Override
		public void paint(Graphics g)
		{
			//super.paint(g);
			if (getIcon() == null) return;
			g.drawImage(((ImageIcon)getIcon()).getImage(), 0, 0, this);
		}
	}
	JCharacter[] chars = new JCharacter[4];
	private float height;
	public Font buildFont(char c)
    {
    	Font fontnum;
    	Font font;
    	int sz = Main.dmi.pantalla.getScale(16);
    	boolean noche = Main.dmi.pantalla.modo == ModoDisplay.Noche;
		try {
			fontnum = Font.createFont(Font.TRUETYPE_FONT, new File("ASFAB.ttf")).deriveFont((float)sz);
		} catch (FontFormatException | IOException e) {
			fontnum = new Font("Lucida Sans", noche ? 0 : 1, sz);
			e.printStackTrace();
		}
        try {
        	if (!noche && Config.Fabricante.equals("SEPSA")) font = Font.createFont(Font.TRUETYPE_FONT, new File("HelveticaCdBk.otf")).deriveFont((float)sz);
        	else font = Font.createFont(Font.TRUETYPE_FONT, new File("HelveticaCd.otf")).deriveFont(noche || Config.Fabricante.equals("DIMETRONIC") ? 0 : 1, sz);
        } catch(FontFormatException | IOException e) {
        	font = new Font("Arial Narrow", noche ? 0 : 1, sz);
        }
        double h = 0;
        double w = 0;
        for (char c2 : (Config.Fabricante.equals("SEPSA") ? "CONVT200" : "M").toCharArray())
        {
	        Rectangle2D r1 = font.createGlyphVector(getFontMetrics(font).getFontRenderContext(), "EXTMBRACONVBTSAVRAMT1234567890").getVisualBounds();
	    	Rectangle2D r2 = font.createGlyphVector(getFontMetrics(font).getFontRenderContext(), Character.toString(c2)).getLogicalBounds();
	        h = Math.max(h, r1.getHeight());
	        w = Math.max(w, r2.getWidth());
        }
        for (char c2 : (Config.Fabricante.equals("DIMETRONIC") ? "0123456789" : "").toCharArray())
        {
	        Rectangle2D r1 = fontnum.createGlyphVector(getFontMetrics(fontnum).getFontRenderContext(), "1234567890").getVisualBounds();
	    	Rectangle2D r2 = fontnum.createGlyphVector(getFontMetrics(fontnum).getFontRenderContext(), Character.toString(c2)).getLogicalBounds();
	        h = Math.max(h, r1.getHeight());
	        w = Math.max(w, r2.getWidth());
        }
        double ty = sz*sz/h;
		double tx = sz*Main.dmi.pantalla.getScale(10)/w;
		if (tx<ty) height = (float)(tx*h/sz);
		else height = sz;
		return (Character.isDigit(c) && Config.Fabricante.equals("DIMETRONIC") ? fontnum : font).deriveFont((float) Math.min(tx, ty));
    }
	ImageIcon RenderDigit(char num, Color c)
    {
		BufferedImage bi = new BufferedImage(
				Main.dmi.pantalla.getScale(10), Main.dmi.pantalla.getScale(16), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(c);
        Font font = buildFont(num);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        String text = Character.toString(num);
        float scy = (float) (height/font.createGlyphVector(getFontMetrics(font).getFontRenderContext(), text).getVisualBounds().getHeight());
        float scx = Math.min(1,(float)Main.dmi.pantalla.getScale(10)/fm.stringWidth(text));
        g2d.transform(AffineTransform.getScaleInstance(scx, scy));
        g2d.drawString(text, scx < 1 ? 0 : (Main.dmi.pantalla.getScale(10)-fm.stringWidth(text))/2, Main.dmi.pantalla.getScale(16)/scy);
        g2d.dispose();
        return new ImageIcon(bi);
    }
	String text;
	public void setValue(String text) {
		if (text.equals(this.text)) return;
		this.text = text;
		update();
	}
	void update()
    {
		//setFont(buildFont());
    	for (int i=0; i<4; i++) {
    		chars[i].setIcon(text.length()>i && text.charAt(i) != ' ' ? RenderDigit(text.charAt(i), Main.dmi.pantalla.modo == ModoDisplay.Día ? Color.black : Pantalla.blanco) : null);
    	}
    }
	public void construct() {
    	for (int i=0; i<4; i++)
        {
        	chars[i] = new JCharacter();
        	/*chars[i].setOpaque(true);
        	chars[i].setBackground(Color.red);*/
        	chars[i].setBounds(Main.dmi.pantalla.getScale(14*i), 0, Main.dmi.pantalla.getScale(10), Main.dmi.pantalla.getScale(16));
        	add(chars[i]);
        }
    }
}
