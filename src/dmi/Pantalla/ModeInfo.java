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
import ecp.ASFA;
import ecp.Config;
import ecp.ASFA.Modo;
import ecp.Main;

public class ModeInfo extends TipoModo {
    public ModeInfo() {
        construct();
        setValue("");
    }

    public void update(Modo m) {
        String t = "";
        String mod = m.name();
        for (int i=0; i<mod.length(); i++)
        {
        	t += mod.charAt(i);
        	if (mod.length()>i+1) t += " ";
        }
        setValue(mod);
    }
}
