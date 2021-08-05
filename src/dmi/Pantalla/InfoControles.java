package dmi.Pantalla;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import dmi.DMI;
import ecp.ASFA;
import ecp.Config;
import ecp.Main;

public class InfoControles extends JPanel {

    JLabel area1;
    JLabel area2;
    JLabel area3;
    Icono IconoLVI;
    Icono IconoPNdesp;
    Icono IconoPNprot;
    Icono IconoSecAA;
    Icono IconoDesvío;

    public InfoControles() {
        setLayout(new GridLayout(1, 3));
        setOpaque(false);
        area1 = new JLabel();
        area2 = new JLabel();
        area3 = new JLabel();
        add(area1);
        add(area2);
        add(area3);
        IconoLVI = new Icono(false, Config.Version < 3 ? "LTV.png" : "LVI.png");
        IconoPNdesp = new Icono(false, "PNdesp.png");
        IconoSecAA = new Icono(false, "SecAA.png");
        IconoDesvío = new Icono(true, "Desvío.png");
        IconoPNprot = new Icono(true, "PNprot.png");
        Main.dmi.pantalla.Blinker.Blinker4Hz.add((e) -> {
            if (LVI == 2)
            {
               area3.setVisible("on".equals(e.getActionCommand()));
            }
        });
        Main.dmi.pantalla.Blinker.Blinker4Hz.add((e) -> {
            if (PNdesp == 2)
            {
            	area1.setVisible("on".equals(e.getActionCommand()));
            }
        });
        update();
    }
    public int PNdesp;
    public int SecAA;
    public int Desv;
    public int LVI;
	public int PNprot;

    public void update() {
        if (PNdesp == 2) {
            area1.setIcon(IconoPNdesp.getIcon());
        }
        else
        {
            area1.setVisible(true);
        	if(PNdesp == 1) area1.setIcon(IconoPNdesp.getIcon());
        	else if(PNprot == 1) area1.setIcon(IconoPNprot.getIcon());
        	else area1.setIcon(null);
        }
        if (Desv==1) {
            area2.setIcon(IconoDesvío.getIcon());
        } else if (SecAA==1) {
            area2.setIcon(IconoSecAA.getIcon());
        } else {
            area2.setIcon(null);
        }
        if (LVI == 2) {
            area3.setIcon(IconoLVI.getIcon());
        } else {
            area3.setVisible(true);
            if (LVI == 1) {
                area3.setIcon(IconoLVI.getIcon());
            } else {
                area3.setIcon(null);
            }
        }
    }

    public void update(int PNdesp, int PNprot, int Desv, int SecAA, int LVI) {
    	if (this.PNdesp == PNdesp && this.PNprot == PNprot && this.Desv == Desv && 
    			this.SecAA == SecAA && this.LVI == LVI) return;
        this.PNdesp = PNdesp;
        this.PNprot = PNprot;
        this.Desv = Desv;
        this.SecAA = SecAA;
        this.LVI = LVI;
        update();
    }
}
