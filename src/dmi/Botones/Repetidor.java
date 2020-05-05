package dmi.Botones;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.COM;

import dmi.Botones.Botón.TipoBotón;
import ecp.Main;

public class Repetidor extends JPanel {

    public Botón Modo, Rearme, Rebase, AumVel, Alarma, Ocultación, LVI, PN;
    
    public JButton basico;
    
    public Luces luces_basico;
    
    public Repetidor() {
        ImageIcon[] iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Modo.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/ModoIluminado.png"));
        Modo = new Botón(iconos, 2, Botón.TipoBotón.Modo);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Rearme.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/RearmeIluminado.png"));
        Rearme = new Botón(iconos, 2, Botón.TipoBotón.Rearme);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Rebase.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/RebaseIluminado.png"));
        Rebase = new Botón(iconos, 2, Botón.TipoBotón.Rebase);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/AumVel.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/AumVelIluminado.png"));
        AumVel = new Botón(iconos, 2, Botón.TipoBotón.AumVel);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Alarma.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/AlarmaIluminado.png"));
        Alarma = new Botón(iconos, 2, Botón.TipoBotón.Alarma);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/Ocultación.png"));
        iconos[1] = iconos[0];
        Ocultación = new Botón(iconos, 2, Botón.TipoBotón.Ocultación);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/LVI.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/LVIIluminado.png"));
        LVI = new Botón(iconos, 2, Botón.TipoBotón.LVI);
        iconos = new ImageIcon[2];
        iconos[0] = new ImageIcon(getClass().getResource("/content/Botones/PN.png"));
        iconos[1] = new ImageIcon(getClass().getResource("/content/Botones/PNIluminado.png"));
        PN = new Botón(iconos, 2, Botón.TipoBotón.PN);
        setBackground(Color.blue);
        JButton Conex = new JButton();
        Conex.addActionListener((arg0) -> 
        {
        	Main.dmi.ecp.enviarPulsacion(TipoBotón.Conex, !Main.dmi.activo);
        	if(Main.dmi.activo)
        	{
        		Main.dmi.pantalla.poweroff();
        		Main.dmi.activo = false;
        	}
        });
        Conex.setIcon(new ImageIcon(getClass().getResource("/content/Botones/Conex.png")));
        Conex.setOpaque(false);
        Conex.setContentAreaFilled(false);
        Conex.setBorderPainted(false);
        basico = new JButton();
        basico.addActionListener((arg0) -> {
        	basico.setSelected(!basico.isSelected());
        	if (basico.isSelected())
        	{
        		Main.dmi.ecp.enviarPulsacion(TipoBotón.ASFA_básico, true);
        		Main.dmi.pantalla.poweroff();
        	}
        	else
        	{
        		Main.dmi.ecp.enviarPulsacion(TipoBotón.ASFA_básico, false);
        	}
        });
        basico.setIcon(new ImageIcon(getClass().getResource("/content/Botones/Basico.png")));
        basico.setSelectedIcon(new ImageIcon(getClass().getResource("/content/Botones/BasicoIluminado.png")));
        basico.setOpaque(false);
        basico.setContentAreaFilled(false);
        basico.setBorderPainted(false);
        
        luces_basico = new Luces();
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.insets = new Insets(0,0,0,0);
        add(Conex, gbc);
        gbc.gridx++;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(luces_basico, gbc);
        gbc.gridheight = 1;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5,5,5,5);
        add(Modo, gbc);
        gbc.gridx++;
        add(Rearme, gbc);
        gbc.gridx++;
        add(Rebase, gbc);
        gbc.gridx++;
        add(AumVel, gbc);
        gbc.gridx=0;
        gbc.gridy++;
        gbc.insets = new Insets(0,0,0,0);
        add(basico, gbc);
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx++;
        gbc.gridx++;
        add(Alarma, gbc);
        gbc.gridx++;
        add(Ocultación, gbc);
        gbc.gridx++;
        add(LVI, gbc);
        gbc.gridx++;
        add(PN, gbc);
        gbc.gridx++;
    }
}
