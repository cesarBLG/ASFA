package dmi.Pantalla;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import ecp.ASFA;
import ecp.ASFA.Modo;
import ecp.Main;

public class ModeInfo extends JLabel {

    public ModeInfo() {
        setHorizontalAlignment(JLabel.CENTER);
        setFont(new Font("Helvetica-Condensed", 1, 15));
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
