package dmi.Pantalla;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;

import dmi.Pantalla.Pantalla.ModoDisplay;
import ecp.ASFA;
import ecp.Main;

public class Icono {
	
    ImageIcon[] iconos;
    boolean variable;
    
    public ImageIcon createScaledIcon(URL location)
    {
    	ImageIcon ic = new ImageIcon(location);
    	Image img = ic.getImage();
    	Image newimg = img.getScaledInstance(Main.dmi.pantalla.getScale(img.getWidth(ic.getImageObserver())/4.3906f), Main.dmi.pantalla.getScale(img.getHeight(ic.getImageObserver())/4.3906f), java.awt.Image.SCALE_SMOOTH);
    	return new ImageIcon(newimg);
    }
    
    public Icono(boolean var, String halfroute) {
        variable = var;
        String route = "/content/Display/";
        if (variable) {
            iconos = new ImageIcon[ModoDisplay.values().length];
            for (int i = 0; i < iconos.length; i++) {
                try {
                    URL arg0 = getClass().getResource(route.concat(ModoDisplay.values()[i].name()).concat("/").concat(halfroute));
                    if (arg0 != null) {
                        iconos[i] = createScaledIcon(arg0);
                    }
                } catch (Exception e) {
                    iconos[i] = null;
                }
            }
        } else {
            iconos = new ImageIcon[1];
            iconos[0] = createScaledIcon(getClass().getResource(route.concat(halfroute)));
        }
    }

    public ImageIcon getIcon() {
        if (variable) {
            return iconos[Main.dmi.pantalla.modo.ordinal()];
        } else {
            return iconos[0];
        }
    }
}
