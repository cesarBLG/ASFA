package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import dmi.Pantalla.Pantalla.ModoDisplay;
import ecp.ASFA;
import ecp.Main;

public class TipoTren extends TipoModo {

    public TipoTren() {
        construct();
        setValue("");
    }

    public void set(int T) {
    	setValue(T==0 ? "" : "T".concat(Integer.toString(T)));
    }
    public void set(String text)
    {
        setValue(text);
    }
}
