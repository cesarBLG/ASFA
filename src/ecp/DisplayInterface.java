package ecp;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import com.COM;
import com.Serial;

import dmi.Sonidos;
import dmi.Botones.Botón;
import dmi.Botones.Botón.*;

class EstadoBotón {

    public boolean pulsado;
    public boolean iluminado;
    public double startTime = 0;
    public double tiempoPulsar = 0.5;
    Object lector;
    
    public EstadoBotón(boolean p, boolean i) {
        iluminado = i;
        pulsar(p);
    }
    public void pulsar(boolean p)
    {
    	if(p == pulsado) return;
    	pulsado = p;
    	if(pulsado && lector != null) startTime = Clock.getSeconds();
    	else startTime = 0;
    }
    public void esperarPulsado(Object detector)
    {
    	if(lector!=null && detector.equals(lector)) return;
    	startTime = 0;
    	lector = detector;
    }
    public boolean flancoPulsado(Object detector)
    {
    	if(!detector.equals(lector)) return false;
    	boolean val = startTime!=0 && Clock.getSeconds() - startTime >= tiempoPulsar;
    	if(val)
    	{
    		startTime = 0;
    		lector = null;
    	}
    	return val;
    }
}

public class DisplayInterface {

    Serial OR = new Serial();
    Hashtable<TipoBotón, EstadoBotón> botones = new Hashtable<TipoBotón, EstadoBotón>();

    void write(int num, int data) {
        COM.parse(num, data);
        OR.write(new byte[]{(byte) num, (byte) data, (byte) controlByte(num, data)});
    }

    byte controlByte(int n1, int n2) {
        int number = (n1 << 8) | n2;
        int control = 0;
        String s = Integer.toString(number);
        for (int i = 0; i < 16; i++) {
            control += i * ((number >> i) & 1);
        }
        return (byte) control;
    }

    public DisplayInterface() {
        OR.start();
    }

    public void iluminar(TipoBotón botón, boolean state) {
        if (!botones.containsKey(botón)) {
            botones.put(botón, new EstadoBotón(false, state));
        } else if (botones.get(botón).iluminado == state) {
            return;
        } else {
            botones.get(botón).iluminado = state;
        }
        write(0, (botón.ordinal() << 2) | (state ? 1 : 0));
    }

    public void iluminarTodos(boolean state) {
        TipoBotón[] t = TipoBotón.values();
        for (int i = 0; i < t.length; i++) {
            iluminar(t[i], state);
        }
    }

    public void pulsar(TipoBotón botón, boolean state) {
        if (!botones.containsKey(botón)) {
            botones.put(botón, new EstadoBotón(state, false));
        } else {
            botones.get(botón).pulsar(state);
        }
        write(0, (botón.ordinal() << 2) | 2 | (state ? 1 : 0));
    }

    public boolean pressed(TipoBotón botón) {
        return botones.get(botón).pulsado;
    }
    public void esperarPulsado(TipoBotón botón, Object detector)
    {
    	botones.get(botón).esperarPulsado(detector);
    }
    public boolean pulsado(TipoBotón botón, Object detector) {
        return botones.get(botón).flancoPulsado(detector);
    }
    public boolean algunoPulsando(Object detector)
    {
    	for(EstadoBotón b : botones.values())
    	{
    		if(b.startTime != 0 && detector.equals(b.lector)) return true;
    	}
    	return false;
    }
    Hashtable<String, Integer> controles = new Hashtable<String, Integer>();

    public void display(String funct, int state) {
        if (!controles.containsKey(funct)) {
            controles.put(funct, state);
        } else if (controles.get(funct) == state) {
            return;
        } else {
            controles.replace(funct, state);
        }
        switch (funct) {
            case "Info":
                write(1, state);
                break;
            case "Velocidad":
                write(2, state);
                break;
            case "Velocidad Objetivo":
                write(3, (state << 1) / 5);
                break;
            case "EstadoVobj":
                write(3, (state << 1) | 1);
                break;
            case "Eficacia":
                write(4, state | 2);
                break;
            case "Sobrevelocidad":
                write(4, state | 4);
                break;
            case "Urgencia":
                write(4, state);
                break;
            case "Paso Desvío":
                write(5, state);
                break;
            case "Secuencia AA":
                write(5, state | 1<<2);
                break;
            case "LVI":
                write(5, state | 2<<2);
                break;
            case "PN sin protección":
                write(5, state | 3<<2);
                break;
            case "PN protegido":
                write(5, state | 4<<2);
                break;
            case "Modo":
            	write(11, state);
                break;
            case "Tipo":
            	write(12, state);
                break;
            case "Arranque":
            	write(14, state);
        }
    }
    
    public void start()
    {
    	write(14, 3);
    }
    public void set(int num, int errno)
    {
    	write(14, num);
    }
    public void startSound(String num)
    {
    	startSound(num, false);
    }
    String activo = null;
    public void startSound(String num, boolean basic)
    {
    	String num2 = num + (basic ? "b" : "");
    	if(activo != null && activo.equals(num2)) return;
    	activo = num2;
    	write(15, Sonidos.valueOf(num.replace('-', '_')).ordinal()<<2 | 2 | (basic ? 1 : 0));
    }
    public void stopSound(String num)
    {
    	if(activo==null || !activo.contains(num)) return;
    	write(15, Sonidos.valueOf(num.replace('-', '_')).ordinal()<<2);
    	activo = null;
    }
}
