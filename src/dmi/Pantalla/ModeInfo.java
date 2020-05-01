package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import ecp.ASFA;
import ecp.ASFA.Modo;
import ecp.Main;

public class ModeInfo extends JLabel {

    public ModeInfo() {
        setHorizontalAlignment(JLabel.CENTER);
        try {
            setFont(Font.createFont(Font.TRUETYPE_FONT, new File("HelveticaCdBd.ttf")).deriveFont((float)Main.dmi.pantalla.getScale(15)));
        } catch(FontFormatException | IOException e) {
        }
        setForeground(Color.white);
        update(Modo.CONV);
    }

    public void update(Modo m) {
    	setText(m.name());
        setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Color.black : Color.white);
    }
    public void update() {
        setForeground(Main.dmi.pantalla.modo == ModoDisplay.Día ? Color.black : Color.white);
    }
}
