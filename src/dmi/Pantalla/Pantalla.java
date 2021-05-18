package dmi.Pantalla;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import dmi.DMI;
import ecp.ASFA;
import ecp.ASFA.Modo;
import ecp.Config;
import ecp.Main;


public class Pantalla extends JPanel {
	public enum ModoDisplay {
    	Día,
    	Noche
	}
	public ModoDisplay modo = ModoDisplay.Día;
    public Velocidad vreal;
    public ÚltimaInfo info;
    public VelocidadObjetivo vtarget;
    public Eficacia eficacia;
    public InfoControles controles;
    public Intervención intervención;
    public ModeInfo ModoASFA;
    public JLabel linea;
    public TipoTren tipoTren;
    public Velo velo;
    public float scale = 1.95f /*1.2f*/;
    public boolean activa = false;
    public boolean conectada = false;
    
    static Color blanco = new Color(248, 248, 248);
    
    public int getScale(float val) {
    	
    	return Math.round(Main.dmi.singleScreen ? val*scale : val);
    }
    public Pantalla() {
        setSize(getScale(350), getScale(263));
        setMinimumSize(new Dimension(getScale(350), getScale(263)));
        setMaximumSize(new Dimension(getScale(350), getScale(263)));
        setPreferredSize(new Dimension(getScale(350), getScale(263)));
        setBackground(Color.black);
        this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				set();
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}});
    }

    public void stop()
    {
        setBackground(Color.black);
    	Main.dmi.ecp.unsubscribe("asfa::indicador::*");
    	Main.dmi.ecp.unsubscribe("asfa::fase");
        removeAll();
        validate();
        repaint();
    }
    
    public void splash_sepsa()
    {
        removeAll();
    	setLayout(new BorderLayout());
    	JLabel j = new JLabel("Esperando comunicaciones con ECP ......");
    	j.setForeground(Color.green);
    	j.setHorizontalAlignment(JLabel.CENTER);
    	add(j);
    	validate();
    	repaint();
    }
    
    public void encender()
    {
    	if (Config.Fabricante.contentEquals("SEPSA")) splash_sepsa();
		Timer t = new Timer(0, (arg0) -> {
	    	Main.dmi.ecp.subscribe("asfa::ecp::estado");
		});
		t.setRepeats(false);
		t.start();
    	if(Main.dmi.fullScreen)
    	{
    		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(Main.dmi);
    		Main.dmi.setVisible(true);
    	}
    }
    
    public void apagar()
    {
    	stop();
		conectada = false;
    	Main.dmi.ecp.unsubscribe("asfa::ecp::estado");
    	Main.dmi.ecp.unsubscribe("asfa::pantalla::activa");
    	if(Main.dmi.fullScreen)
    	{
    		Main.dmi.setVisible(false);
    	}
    }
    
    public void setup(int state, String msg) {
    	conectada = true;
        removeAll();
        setup_sepsa(state, msg);
        validate();
        repaint();
        if (state == 0 || state == 1)
        {
    		Timer t = new Timer(1800, (arg0) -> {
    			conectada = true;
    			stop();
    	    	Main.dmi.ecp.subscribe("asfa::pantalla::activa");
    			Main.dmi.ecp.sendData("noretain(asfa::pantalla::conectada=1)");
    		});
    		t.setRepeats(false);
    		t.start();
        }
    }
    
    public void setup_sepsa(int state, String msg)
    {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        JLabel j = new JLabel(df.format(date));
        j.setForeground(Color.blue);
        j.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(j);
        add(Box.createRigidArea(new Dimension(0, 5)));
        switch (state) {
            case 0:
                j = new JLabel("ASFA OK");
                j.setForeground(Color.green);
                break;
            case 1:
                j = new JLabel("ASFA-operativo");
                j.setForeground(Color.yellow);
                break;
            default:
                j = new JLabel("ASFA no operativo");
                j.setForeground(Color.red);
                break;
        }
        j.setFont(new Font(j.getFont().getName(), Font.PLAIN, getScale(20)));
        j.setAlignmentX(Component.CENTER_ALIGNMENT);
        setBackground(Color.black);
        add(j);
        add(Box.createRigidArea(new Dimension(0, 5)));
        if (msg != null && !msg.isEmpty())
        {
            JLabel m = new JLabel(msg);
            m.setAlignmentX(Component.CENTER_ALIGNMENT);
            m.setHorizontalAlignment(JLabel.CENTER);
            m.setForeground(Color.white);
            m.setFont(new Font(m.getFont().getName(), Font.PLAIN, getScale(m.getFont().getSize())));
            add(m);
            add(Box.createRigidArea(new Dimension(0, 5)));
        }
        j = new JLabel("Versión Software EV V"+Config.Version);
        j.setAlignmentX(Component.CENTER_ALIGNMENT);
        j.setForeground(Color.gray);
        j.setFont(new Font(j.getFont().getName(), Font.PLAIN, getScale(j.getFont().getSize())));
        add(j);
        add(Box.createRigidArea(new Dimension(0, 5)));
        if (state != 3)
        {
        	j = new JLabel("Estado del Display OK");
        	j.setForeground(Color.green);
        }
        else
        {
        	j = new JLabel("Estado del Display NOT OK");
        	j.setForeground(Color.red);
        }
        j.setAlignmentX(Component.CENTER_ALIGNMENT);
        j.setFont(new Font(j.getFont().getName(), Font.PLAIN, getScale(j.getFont().getSize())));
        add(j);
        add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    public void setup_indra(int state, String msg)
    {
    	setBackground(blanco);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createRigidArea(new Dimension(0, 5)));
        JLabel j;
        switch (state) {
            case 0:
                j = new JLabel("ASFA OK");
                j.setForeground(Color.green);
                break;
            case 1:
                j = new JLabel("ASFA-operativo");
                j.setForeground(Color.yellow);
                break;
            default:
                j = new JLabel("ASFA no operativo");
                j.setForeground(Color.red);
                break;
        }
        j.setFont(new Font(j.getFont().getName(), Font.PLAIN, getScale(20)));
        j.setAlignmentX(Component.LEFT_ALIGNMENT);
        j.setHorizontalAlignment(JLabel.LEFT);
        add(j);
        
        j = new JLabel("Version "+Config.Version);
        j.setForeground(Color.black);
        j.setAlignmentX(Component.RIGHT_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(j);
        
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        j = new JLabel(df.format(date)+"-GMT");
        j.setForeground(Color.black);
        j.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(j);
        
        add(Box.createRigidArea(new Dimension(0, 5)));
        if (msg != null && !msg.isEmpty())
        {
            JLabel m = new JLabel(msg);
            m.setAlignmentX(Component.CENTER_ALIGNMENT);
            m.setHorizontalAlignment(JLabel.CENTER);
            m.setForeground(Color.white);
            m.setFont(new Font(m.getFont().getName(), Font.PLAIN, getScale(m.getFont().getSize())));
            add(m);
            add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    public void start() {
        removeAll();
        setLayout(null);
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0,0,getScale(350),getScale(263));
        add(pane);
        info = new ÚltimaInfo();
        info.setBounds(getScale(271), getScale(0), getScale(79), getScale(263));
        pane.add(info);
        vreal = new Velocidad(Color.black, blanco);
        vreal.LeadingZeros = Config.Fabricante.equals("DIMETRONIC");
        vreal.setBounds(getScale(16), getScale(42), getScale(75), getScale(31));
        vreal.Center = !Config.Fabricante.equals("DIMETRONIC");
        vreal.construct();
        pane.add(vreal);
        vtarget = new VelocidadObjetivo();
        vtarget.setBounds(getScale(106), getScale(39), getScale(165), getScale(72));
        pane.add(vtarget);
        linea = new JLabel();
        linea.setOpaque(true);
        linea.setBackground(blanco);
        linea.setBounds(getScale(16), getScale(73), getScale(213), getScale(3));
        pane.add(linea);
        eficacia = new Eficacia();
        eficacia.setBounds(getScale(16), getScale(202), getScale(19), getScale(19));
        pane.add(eficacia);
        tipoTren = new TipoTren();
        tipoTren.setBounds(getScale(39), getScale(182), getScale(52), getScale(16));
        pane.add(tipoTren);
        ModoASFA = new ModeInfo();
        ModoASFA.setBounds(getScale(39), getScale(205), getScale(52), getScale(16));
        pane.add(ModoASFA);
        controles = new InfoControles();
        controles.setBounds(getScale(106), getScale(123), getScale(165), getScale(98));
        pane.add(controles);
        intervención = new Intervención();
        intervención.setBounds(getScale(0), getScale(76), getScale(106), getScale(59));
        pane.add(intervención);
        velo = new Velo(this);
        velo.setBounds(0,0,getScale(350),getScale(263));
        pane.add(velo, new Integer(12));
    	Main.dmi.ecp.subscribe("asfa::indicador::*");
    	Main.dmi.ecp.subscribe("asfa::fase");
        set(ModoDisplay.Día);
        repaint();
    }

    public void set(ModoDisplay m) {
    	if (!activa) return;
        modo = m;
        setBackground(modo == ModoDisplay.Noche ? Color.black : blanco);
        vreal.update();
        info.update();
        vtarget.update();
        eficacia.switchstate();
        ModoASFA.update();
        controles.update();
        linea.setBackground(modo == ModoDisplay.Día ? Color.black : blanco);
        tipoTren.update();
    }

    public void set() {
        if (Main.dmi.modo != Modo.EXT) set(modo == ModoDisplay.Día ? ModoDisplay.Noche : ModoDisplay.Día);
    }
}
