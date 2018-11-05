package dmi.Pantalla;

import java.net.URL;

import javax.swing.ImageIcon;

import ecp.ASFA;
import ecp.Main;

public class Icono {

    ImageIcon[] iconos;
    boolean variable;

    public Icono(boolean var, String halfroute) {
        variable = var;
        String route = "/content/Display/";
        if (variable) {
            iconos = new ImageIcon[ModoDisplay.values().length];
            for (int i = 0; i < iconos.length; i++) {
                try {
                    URL arg0 = getClass().getResource(route.concat(ModoDisplay.values()[i].name()).concat("/").concat(halfroute));
                    if (arg0 != null) {
                        iconos[i] = new ImageIcon(arg0);
                    }
                } catch (Exception e) {
                    iconos[i] = null;
                }
            }
        } else {
            iconos = new ImageIcon[1];
            iconos[0] = new ImageIcon(getClass().getResource(route.concat(halfroute)));
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
