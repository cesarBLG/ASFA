package dmi.Pantalla;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

import dmi.Pantalla.Pantalla.ModoDisplay;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.ASFA;
import ecp.Config;
import ecp.Main;

public class Icono {
	
	static HashMap<String, Icono> AlmacenIconos =  new HashMap<>();
	
    ImageIcon[] iconos;
    boolean variable;
    
    public ImageIcon createScaledIcon(String location)
    {
    	ImageIcon ic = new ImageIcon(getClass().getResource(location));
    	Image img = ic.getImage();
    	Image newimg = img.getScaledInstance(Main.dmi.pantalla.getScale(img.getWidth(ic.getImageObserver())/4.3906f), Main.dmi.pantalla.getScale(img.getHeight(ic.getImageObserver())/4.3906f), java.awt.Image.SCALE_SMOOTH);
    	return new ImageIcon(newimg);
    }
    
    public Icono(boolean var, String halfroute) {
        variable = var;
        Icono ic = AlmacenIconos.get(halfroute);
        if (ic != null)
        {
        	iconos = ic.iconos;
        	return;
        }
        String route = "/content/Display/";
        if (variable) {
            iconos = new ImageIcon[ModoDisplay.values().length];
            for (int i = 0; i < iconos.length; i++) {
                try {
                    iconos[i] = createScaledIcon(route.concat(ModoDisplay.values()[i].name()).concat("/").concat(halfroute));
                } catch (Exception e) {
                    iconos[i] = null;
                }
            }
        } else {
            iconos = new ImageIcon[1];
            iconos[0] = createScaledIcon(route.concat(halfroute));
        }
        AlmacenIconos.put(halfroute, this);
    }

    public ImageIcon getIcon() {
        if (variable) {
            return iconos[Main.dmi.pantalla.modo.ordinal()];
        } else {
            return iconos[0];
        }
    }
    
    static void cargar(boolean var, String path)
    {
    	new Icono(var, path);
    	//new Thread(() -> {new Icono(var, path);}).start();;
    }
    
    public static void cargarIconos()
    {
        for (int i = 0; i < 8; i++) {
        	cargar(true, "Eficacia/Fase1/".concat(Integer.toString(i).concat(".png")));
        	cargar(true, "Eficacia/Fase2/".concat(Integer.toString(i).concat(".png")));
        }
    	for (int i = 0; i < Info.values().length; i++) {
    		cargar(true, Info.values()[i].name().concat(".png"));
        }
    	cargar(false, "LTV.png");
    	cargar(false, "LVI.png");
    	cargar(false, "PNdesp.png");
    	cargar(false, "SecAA.png");
    	cargar(true, "Desvío.png");
    	cargar(true, "PNprot.png");
    	cargar(false, "Frenado1.png");
    	cargar(false, "Frenado2.png");
    	cargar(false, "Urgencia.png");
    }
}
