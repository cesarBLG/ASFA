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
import com.Serial;

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

    public DMI() {
        Main.ASFA.dmi = this;
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.CENTER;
        g.gridx = g.gridy = 0;
        g.insets = new Insets(50, 50, 20, 50);
        pantalla = new Pantalla();
        add(pantalla, g);
        g.gridy++;
        g.insets = new Insets(0, 0, 10, 0);
        JButton modonoche = new JButton("D/N");
        modonoche.addActionListener((ActionEvent) -> {
        	COM.parse(new byte[] {6,0});
        }); 
        add(modonoche, g);
        g.gridy++;
        g.insets = new Insets(5, 5, 5, 5);
        repetidor = new Repetidor();
        add(repetidor, g);
        g.gridy++;
        pupitre = new Pupitre();
        add(pupitre, g);
        JTextField jtb = new JTextField();
        jtb.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent arg0) {
                // TODO Auto-generated method stub
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    String s = jtb.getText();
                    byte[] data = new byte[2];
                    String s1 = s.substring(0, s.indexOf(' '));
                    String s2 = s.substring(s.indexOf(' ') + 1);
                    data[0] = (byte) Integer.parseUnsignedInt(s1);
                    data[1] = (byte) Integer.parseUnsignedInt(s2);
                    jtb.setText("");
                    COM.parse(data);
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
        g.gridy++;
        jtb.setPreferredSize(new Dimension(300, 50));
        add(jtb, g);
        getContentPane().setBackground(Color.blue);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}