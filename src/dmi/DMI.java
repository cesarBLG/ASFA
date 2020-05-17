package dmi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.COM;

import dmi.Botones.Botón;
import dmi.Botones.Pupitre;
import dmi.Botones.Repetidor;
import dmi.Pantalla.*;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.ASFA;
import ecp.FrecASFA;
import ecp.Main;
import ecp.Odometer;

public class DMI extends JFrame {

    public Pantalla pantalla;
    public Repetidor repetidor;
    public Pupitre pupitre;
    public boolean singleScreen = false;
    public boolean activo=false;
    public String fabricante = "DIMETRONIC";
    public ECPinterface ecp;

    public DMI() {
        Main.dmi = this;
    	if(singleScreen)
    	{
    		setUndecorated(true);
        	//setExtendedState(JFrame.MAXIMIZED_BOTH);
    	}
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.CENTER;
        g.gridy = 0;
        g.gridx = 1;
        g.gridwidth = 2;
        if(singleScreen) g.insets = new Insets(0, 0, 0, 0);
        else g.insets = new Insets(50, 5, 20, 70);
        pantalla = new Pantalla();
        add(pantalla, g);
        g.gridwidth = 1;
        g.insets = new Insets(0, 0, 10, 0);
        JButton modonoche = new JButton("D/N");
        modonoche.addActionListener((ActionEvent) -> {
        	pantalla.set();
        }); 
        if(!singleScreen)
        {
        	g.insets = new Insets(0, 5, 0, 0);
        	g.gridx = 0;
        	add(modonoche, g);
        	g.gridwidth = 3;
        }
        g.gridy++;
        g.insets = new Insets(5, 5, 5, 5);
        repetidor = new Repetidor();
        if(!singleScreen) add(repetidor, g);
        g.gridy++;
        pupitre = new Pupitre();
        if(!singleScreen) add(pupitre, g);
        JTextField jtb = new JTextField();
        jtb.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    String s = jtb.getText();
                    String[] sub = s.split(" ");
                    for(int i=0; i+1<sub.length; i+=2)
                    {
                    	COM.parse(Integer.parseUnsignedInt(sub[i]), Integer.parseUnsignedInt(sub[i+1]));
                    	try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                    jtb.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }
        });
        jtb.setPreferredSize(new Dimension(220, 25));
        jtb.setMinimumSize(new Dimension(220, 25));
        JButton cg = new JButton("CG");
        cg.addActionListener((ActionEvent) -> {
        	if (Main.ASFA == null) return;
        	CombinadorGeneral c = new CombinadorGeneral(Main.ASFA);
        	c.setVisible(true);
        });
        g.gridwidth = 2;
        g.gridx = 1;
        g.gridy++;
        if(!singleScreen) add(jtb, g);
        g.gridx = 0;
        g.gridwidth = 1;
        if(!singleScreen) add(cg, g);
        getContentPane().setBackground(new Color(0, 83, 135));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        ecp = new ECPinterface(this);
        setVisible(true);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
    }
}
