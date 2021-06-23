package dmi.Pantalla;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import ecp.Main;

public class Velo extends JPanel {
    private Color[] colores_velo;
    private Pantalla p;
    boolean activo=false;
	public Velo(Pantalla p)
	{
		this.p = p;
		colores_velo = new Color[21];
    	colores_velo[0] = new Color(250,250,250);
    	colores_velo[1] = new Color(149,149,149);
    	colores_velo[2] = new Color(96,96,96);
    	colores_velo[3] = new Color(117,117,117);
    	colores_velo[4] = new Color(249,249,249);
    	colores_velo[5] = Color.white;
    	colores_velo[6] = new Color(221,221,221);
    	colores_velo[7] = new Color(122,122,122);
    	colores_velo[8] = new Color(86,86,86);
    	colores_velo[9] = new Color(161,161,161);
    	colores_velo[10] = Color.white;
    	colores_velo[11] = Color.white;
    	colores_velo[12] = new Color(184,184,184);
    	colores_velo[13] = new Color(103,103,103);
    	colores_velo[14] = new Color(90,90,90);
    	colores_velo[15] = new Color(210,210,210);
    	colores_velo[16] = Color.white;
    	colores_velo[17] = new Color(250,250,250);
    	colores_velo[18] = new Color(149,149,149);
    	colores_velo[19] = new Color(210,210,210);
    	colores_velo[20] = Color.white;
    	this.setOpaque(false);
	}
    public void setActivo(boolean activo)
    {
    	if (this.activo == activo) return;
    	this.activo = activo;
    	repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	if (!activo) return;
    	g.setPaintMode();
    	float sum = Main.dmi.singleScreen ? p.scale : 1;
    	float curr=-sum;
    	float num2 = 1f;
    	int lino = 0;
    	/*int[] values = {79,105,123,140,202,245,263,271,350,368,473};
    	for (int i=0; i<values.length; i++)
    	{
    		values[i] = getScale(values[i]);
    	}*/
    	for (int i=0; i<p.getScale(79); i++)
    	{
    		curr+=sum;
    		if (curr>=num2*sum)
    		{
    			curr -= num2*sum;
    			lino++;
    			if (lino == 21) lino = 0;
    		}
    		if (colores_velo[lino].getRed() == 255) continue;
    		g.setColor(colores_velo[lino]);
    		g.drawLine(p.getScale(350), i, p.getScale(350)-i, 0);
    	}
    	for (int i=0; i<p.getScale(123); i++)
    	{
    		curr+=sum;
    		if (curr>=num2*sum)
    		{
    			curr -= num2*sum;
    			lino++;
    			if (lino == 21) lino = 0;
    		}
    		if (colores_velo[lino].getRed() == 255) continue;
    		g.setColor(colores_velo[lino]);
    		g.drawLine(p.getScale(271), i, p.getScale(350), p.getScale(79)+i);
    	}
    	for (int i=p.getScale(123+79); i<p.getScale(263); i++)
    	{
    		curr+=sum;
    		if (curr>=num2*sum)
    		{
    			curr -= num2*sum;
    			lino++;
    			if (lino == 21) lino = 0;
    		}
    		if (colores_velo[lino].getRed() == 255) continue;
    		g.setColor(colores_velo[lino]);
    		g.drawLine(p.getScale(350), i, p.getScale(350+123)-i, p.getScale(123));
    	}
    	for (int i=p.getScale(350); i>p.getScale(245); i--)
    	{
    		curr+=sum;
    		if (curr>=num2*sum)
    		{
    			curr -= num2*sum;
    			lino++;
    			if (lino == 21) lino = 0;
    		}
    		if (colores_velo[lino] == Color.white) continue;
    		g.setColor(colores_velo[lino]);
    		g.drawLine(i, p.getScale(263), i-p.getScale(140), p.getScale(123));
    	}
    	for (int i=p.getScale(245); i>p.getScale(105); i--)
    	{
    		curr+=sum;
    		if (curr>=num2*sum)
    		{
    			curr -= num2*sum;
    			lino++;
    			if (lino == 21) lino = 0;
    		}
    		if (colores_velo[lino] == Color.white) continue;
    		g.setColor(colores_velo[lino]);
    		g.drawLine(i, p.getScale(263), p.getScale(105), p.getScale(263+105)-i);
    	}
    }
}
