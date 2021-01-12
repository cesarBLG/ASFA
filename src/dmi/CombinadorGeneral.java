package dmi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import ecp.ASFA;
import ecp.Main;

public class CombinadorGeneral extends JDialog {
	ASFA asfa;
	boolean conect=true;
	boolean anul=false;
	int t=7;
	void setConectado(boolean c)
	{
		if (asfa==null) conect = c;
		else
		{
			asfa.ASFAconectado = c;
			synchronized(asfa)
			{
				asfa.notify();
			}
		}
	}
	void setAnulado(boolean a)
	{
		if (asfa==null) anul = a;
		else 
		{
			asfa.ASFAanulado = a;
			synchronized(asfa)
			{
				asfa.notify();
			}
		}
	}
	void setTipo(int t)
	{
		if (asfa==null) this.t = t;
		else asfa.selectorT = t;
	}
	boolean getConectado()
	{
		return asfa==null ? conect : asfa.ASFAconectado;
	}
	boolean getAnulado()
	{
		return asfa==null ? anul : asfa.ASFAanulado;
	}
	int getT()
	{
		return asfa==null ? t : asfa.selectorT;
	}
	
	public CombinadorGeneral(ASFA asfa)
	{
		this.asfa = asfa;
        if (asfa != null && (asfa.selectorT<1 || asfa.selectorT > 8)) asfa.selectorT = 1;
		setTitle("Combinador General");
		setLayout(new GridBagLayout());
		setBackground(Color.blue);
        getContentPane().setBackground(new Color(0, 83, 135));
		GridBagConstraints g = new GridBagConstraints();
		g.gridy=0;
		g.gridx=0;
		Conexion conex = new Conexion();
		conex.update(getConectado());
		conex.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getButton() == MouseEvent.BUTTON1 || arg0.getButton() == MouseEvent.BUTTON3)
				{
					setConectado(!getConectado());
				}
				conex.update(getConectado());
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
		add(conex, g);
		g.gridx++;
		Anulacion anul = new Anulacion();
		anul.update(asfa.ASFAanulado);
		anul.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getButton() == MouseEvent.BUTTON1 || arg0.getButton() == MouseEvent.BUTTON3)
				{
					setAnulado(!getAnulado());
				}
				anul.update(getAnulado());
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
		add(anul, g);
		g.gridx++;
		SelectorTipo tipo = new SelectorTipo();
        tipo.update(asfa.selectorT);
        tipo.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int sel = getT();
				if (arg0.getButton() == MouseEvent.BUTTON1) sel = sel%8+1;
				if (arg0.getButton() == MouseEvent.BUTTON3)
				{
					if (sel <= 1) sel = 8;
					else sel = sel-1;
				}
				setTipo(sel);
				tipo.update(sel);
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
		add(tipo, g);
		setSize(700, 320);
	}
	class SelectorTipo extends JLabel
	{
		int selectorT = 1;
		public SelectorTipo()
		{
			setIcon(new ImageIcon(getClass().getResource("/content/CG/SelectorT.png")));
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}
		void update(int selectorT)
		{
			this.selectorT = selectorT;
			repaint();
		}
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			BufferedImage bi;
			try {
				bi = ImageIO.read(getClass().getResource("/content/CG/Selector.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(getWidth()/2-bi.getWidth()/2, getHeight()/2-bi.getHeight()/2);
            g2.rotate(-Math.PI/4*selectorT, bi.getWidth()/2, bi.getHeight()/2);
            g2.drawImage(bi, 0, 0, null);
		}
	};
	class Conexion extends JLabel
	{
		boolean conectado = true;
		public Conexion()
		{
			setIcon(new ImageIcon(getClass().getResource("/content/CG/Conexion.png")));
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}
		void update(boolean conectado)
		{
			this.conectado = conectado;
			repaint();
		}
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			BufferedImage bi;
			try {
				bi = ImageIO.read(getClass().getResource("/content/CG/Selector.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(getWidth()/2-bi.getWidth()/2, getHeight()/2-bi.getHeight()/2);
            g2.rotate(conectado ? Math.PI/2 : 0, bi.getWidth()/2, bi.getHeight()/2);
            g2.drawImage(bi, 0, 0, null);
		}
	}
	class Anulacion extends JLabel
	{
		boolean anulado = true;
		public Anulacion()
		{
			setIcon(new ImageIcon(getClass().getResource("/content/CG/Anulacion.png")));
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}
		void update(boolean anulado)
		{
			this.anulado = anulado;
			repaint();
		}
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			BufferedImage bi;
			try {
				bi = ImageIO.read(getClass().getResource("/content/CG/Selector.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(getWidth()/2-bi.getWidth()/2, getHeight()/2-bi.getHeight()/2);
            g2.rotate(anulado ? 0 : Math.PI/2, bi.getWidth()/2, bi.getHeight()/2);
            g2.drawImage(bi, 0, 0, null);
		}
	}
}
