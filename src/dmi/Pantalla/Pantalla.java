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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import dmi.DMI;
import dmi.Pantalla.Pantalla.ModoDisplay;
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
	public Modo modo_asfa = null;
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
    public boolean habilitada = false; // Display habilitado por ECP
    public boolean activa = false; // Pantalla activa mostrando indicaciones ASFA
    public boolean conectada = false; // Pantalla en funcionamiento
    public boolean ocupada = false; // Display incapaz de mostrar indicaciones
    
    public class BlinkerASFA implements ActionListener
    {
    	public List<ActionListener> Blinker2Hz = new ArrayList<>();
    	public List<ActionListener> Blinker4Hz = new ArrayList<>();
    	int count;
    	Timer t;
    	BlinkerASFA()
    	{
    		t = new Timer(250, this);
    		t.setRepeats(true);
    		t.start();
    	}
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean tic = count % 2 == 0; 
			ActionEvent e1 = new ActionEvent(this, e.getID(), tic ? "on" : "off");
			if (tic)
			{
				ActionEvent e2 = new ActionEvent(this, e.getID(), count==0 ? "on" : "off");
				Blinker2Hz.forEach((l) -> l.actionPerformed(e2));
			}
			Blinker4Hz.forEach((l) -> l.actionPerformed(e1));
			count = (count+1)%4;
		}
		public void stop()
		{
			t.removeActionListener(this);
			t.stop();
			Blinker2Hz.clear();
			Blinker4Hz.clear();
		}
    }
    public BlinkerASFA Blinker;
    
    public PantallaSerializer serialClient;
    
    static Color blanco = new Color(248, 248, 248);
    
    public int getScale(float val) {
    	
    	return Math.round(Main.dmi.singleScreen ? val*scale : val);
    }
    public Pantalla() {
    	int sizex = Config.Fabricante.equals("SIEMENS") ? 370 : 350;
        setSize(getScale(sizex), getScale(263));
        setMinimumSize(new Dimension(getScale(sizex), getScale(263)));
        setMaximumSize(new Dimension(getScale(sizex), getScale(263)));
        setPreferredSize(new Dimension(getScale(sizex), getScale(263)));
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
			public void mouseReleased(MouseEvent arg0) {}
		});
        
    	if (Config.ApagarOrdenador)
    	{
			Main.dmi.activo = true;
			Main.dmi.pantalla.encender();
    	}
    	new PantallaGPIO(this);
    }

    public void stop()
    {
		activa = false;
        Main.dmi.ecp.sendData("asfa::pantalla::activa=0");
    	if (Main.dmi.fullScreen)
    	{
            /*try {
    			Runtime.getRuntime().exec("vcgencmd display_power 0");
    		} catch (IOException e) {
    			e.printStackTrace();
    		}*/
    	}
    	modo_asfa = null;
    	if (Blinker != null) Blinker.stop();
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
    	/*if(Main.dmi.fullScreen)
    	{
            try {
				Runtime.getRuntime().exec("vcgencmd display_power 1");
			} catch (IOException e) {
			}
        	GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(Main.dmi);
    	}*/
    	if (Config.Fabricante.contentEquals("SEPSA")) splash_sepsa();
		Timer t = new Timer(100, (arg0) -> {
			conectada = true;
	        Main.dmi.ecp.sendData("asfa::pantalla::conectada=1");
	    	Main.dmi.ecp.subscribe("asfa::ecp::estado");
		});
		ocupada = !(Config.Fabricante.equalsIgnoreCase("DIMETRONIC") || Config.Fabricante.equalsIgnoreCase("SIEMENS"));
		t.setRepeats(false);
		t.start();
    }
    
    public void apagar()
    {
    	stop();
    	conectada = false;
    	Main.dmi.ecp.unsubscribe("asfa::ecp::estado");
        Main.dmi.ecp.sendData("asfa::pantalla::conectada=0");
    	/*if(Main.dmi.fullScreen)
    	{
    		Main.dmi.setVisible(false);
            try {
				Runtime.getRuntime().exec("vcgencmd display_power 0");
			} catch (IOException e) {
			}
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
    	}*/
    }
    
    public void setup(int state, String msg) {
    	ocupada = true;
    	if ((Config.Fabricante.equalsIgnoreCase("DIMETRONIC") || Config.Fabricante.equalsIgnoreCase("SIEMENS")) && state < 2)
    	{
    		ocupada = false;
    		if (habilitada) start();
    		else stop();
    		return;
    	}
        removeAll();
        if (Config.Fabricante.equals("INDRA")) setup_indra(state, msg);
        else if (Config.Fabricante.equalsIgnoreCase("DIMETRONIC")) setup_dimetronic(state, msg);
        else setup_sepsa(state, msg);
        validate();
        repaint();
        if (state == 0 || state == 1)
        {
    		Timer t = new Timer(1800, (arg0) -> {
        		ocupada = false;
        		if (habilitada) start();
        		else stop();
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
        if (state == 2)
        {
            j = new JLabel("Averia grave - Avisar a mantenimiento");
            j.setAlignmentX(Component.CENTER_ALIGNMENT);
            j.setForeground(Color.red);
            j.setFont(new Font(j.getFont().getName(), Font.PLAIN, getScale(j.getFont().getSize())));
            add(j);
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

    public void setup_dimetronic(int state, String msg)
    {
        setLayout(new GridLayout(1,2));
        JPanel panelDatos = new JPanel();
        JPanel panelEstado = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));
        panelEstado.setLayout(new BoxLayout(panelEstado, BoxLayout.Y_AXIS));
        panelDatos.setOpaque(false);
        //panelEstado.setOpaque(false);
        panelDatos.setAlignmentY(CENTER_ALIGNMENT);
        panelEstado.setAlignmentY(CENTER_ALIGNMENT);
        add(panelDatos);
        add(panelEstado);
        Date date = new Date();
        DateFormat df1 = new SimpleDateFormat("HH:mm:ss");
        DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
        JLabel clock = new JLabel(df1.format(date));
        panelDatos.add(clock);
        //new Timer(250, (arg0) ->  {clock.setText(df1.format(new Date()));}).start();
        JLabel j = new JLabel(df2.format(date));
        panelDatos.add(j);
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
        setBackground(Color.black);
        panelEstado.add(j);
        panelEstado.add(Box.createRigidArea(new Dimension(0, 5)));
        if (msg != null && !msg.isEmpty())
        {
            JLabel m = new JLabel(msg);
            m.setAlignmentX(Component.CENTER_ALIGNMENT);
            m.setHorizontalAlignment(JLabel.CENTER);
            m.setForeground(Color.white);
            m.setFont(new Font(m.getFont().getName(), Font.PLAIN, getScale(m.getFont().getSize())));
            panelEstado.add(m);
            panelEstado.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }
    public void habilitar(boolean activa)
    {
		if (activa != habilitada)
		{
			habilitada = activa;
			if (conectada && !ocupada)
			{
				if (activa) start();
				else stop();
			}
		}
    }
    
    public void start() {
    	if(Main.dmi.fullScreen)
    	{
            /*try {
				Runtime.getRuntime().exec("vcgencmd display_power 1");
			} catch (IOException e) {
			}*/
    	}
    	Blinker = new BlinkerASFA();
        JLayeredPane pane = new JLayeredPane();
        pane.setBounds(0,0,getWidth(), getHeight());
        info = new ÚltimaInfo();
        info.setBounds(getScale(271), getScale(0), getScale(Config.Fabricante.equals("SIEMENS") ? 99 : 79), getScale(263));
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
        if (modo_asfa != null) ModoASFA.update(modo_asfa);
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
    	removeAll();
        setLayout(null);
        add(pane);
        activa = true;
        Main.dmi.ecp.sendData("asfa::pantalla::activa=1");
        set(ModoDisplay.Día);
        validate();
        repaint();
    }

    public void set(ModoDisplay m) {
    	if (!activa) return;
        modo = m;
        setBackground(modo == ModoDisplay.Noche ? Color.black : blanco);
        vreal.update();
        info.update();
        vtarget.update();
        eficacia.update();
        ModoASFA.update();
        controles.update();
        linea.setBackground(modo == ModoDisplay.Día ? Color.black : blanco);
        tipoTren.update();
    }
    
    public void setModo(Modo modo)
    {
    	if (modo_asfa == modo) return;

		if (modo == Modo.EXT)
		{
			linea.setVisible(false);
			vreal.setVisible(false);
			vtarget.setVisible(false);
			controles.setVisible(false);
			info.setVisible(false);
			velo.setVisible(false);
			intervención.setVisible(false);
			set(ModoDisplay.Noche);
		}
		else if (modo_asfa == Modo.EXT)
		{
			linea.setVisible(true);
			vreal.setVisible(true);
			vtarget.setVisible(true);
			controles.setVisible(true);
			info.setVisible(true);
			velo.setVisible(true);
			intervención.setVisible(true);
			set(ModoDisplay.Día);
		}
    	modo_asfa = modo;
		if (modo == null) ModoASFA.setValue("");
		else ModoASFA.update(modo);
    }

    public void set() {
        if (modo_asfa != Modo.EXT) set(modo == ModoDisplay.Día ? ModoDisplay.Noche : ModoDisplay.Día);
    }
}
