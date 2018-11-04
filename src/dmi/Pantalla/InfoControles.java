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

public class InfoControles extends JPanel {

    JLabel area1;
    JLabel area2;
    JLabel area3;
    Icono IconoLVI;
    Icono IconoPNdesp;
    Icono IconoPNprot;
    Icono IconoSecAA;
    Icono IconoDesvío;
    Timer TimerLVI;
    Timer TimerPN;

    public InfoControles() {
        setLayout(new GridLayout(1, 3));
        setOpaque(false);
        area1 = new JLabel();
        area2 = new JLabel();
        area3 = new JLabel();
        add(area1);
        add(area2);
        add(area3);
        IconoLVI = new Icono(false, "LVI.png");
        IconoPNdesp = new Icono(false, "PNdesp.png");
        IconoSecAA = new Icono(false, "SecAA.png");
        IconoDesvío = new Icono(true, "Desvío.png");
        IconoPNprot = new Icono(true, "PNprot.png");
        TimerLVI = new Timer(500, new ActionListener() {
            boolean Displayed = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                area3.setVisible(!Displayed);
                Displayed = !Displayed;
            }

        });
        TimerPN = new Timer(500, new ActionListener() {
            boolean Displayed = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                area1.setVisible(!Displayed);
                Displayed = !Displayed;
            }

        });
        update(0, 0, 0, 0, 0);
    }
    public int PNdesp;
    public int SecAA;
    public int Desv;
    public int LVI;
	public int PNprot;

    public void update() {
        if (PNdesp == 2) {
            TimerPN.start();
            TimerPN.setRepeats(true);
            area1.setIcon(IconoPNdesp.getIcon());
        }
        else
        {
            TimerPN.stop();
            TimerPN.setRepeats(false);
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
            TimerLVI.start();
            TimerLVI.setRepeats(true);
        } else {
            TimerLVI.stop();
            TimerLVI.setRepeats(false);
            area3.setVisible(true);
            if (LVI == 1) {
                area3.setIcon(IconoLVI.getIcon());
            } else {
                area3.setIcon(null);
            }
        }
    }

    public void update(int PNdesp, int PNprot, int Desv, int SecAA, int LVI) {
        this.PNdesp = PNdesp;
        this.PNprot = PNprot;
        this.Desv = Desv;
        this.SecAA = SecAA;
        this.LVI = LVI;
        update();
    }
}
