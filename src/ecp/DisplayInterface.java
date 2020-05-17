package ecp;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import com.COM;
import com.OR_Client;
import com.TCP;

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
    TCP OR2 = new TCP();
    OR_Client orclient;
    Hashtable<TipoBotón, EstadoBotón> botones = new Hashtable<TipoBotón, EstadoBotón>();

    void write(int num, int data) {
        COM.parse(num, data);
        byte[] b = new byte[]{(byte) num, (byte) data, (byte) 0xFF};
        OR2.write(b);
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
    	OR2.initialize();
    	orclient = new OR_Client();
    }

    void reset()
    {
    	TipoBotón[] t = TipoBotón.values();
        for (int i = 0; i < t.length; i++) {
            EstadoBotón e = botones.get(t[i]);
            if (e == null) continue;
            e.iluminado = false;
            e.lector = null;
        }
        controles.clear();
    }
    
    public void iluminar(TipoBotón botón, boolean state) {
        if (!botones.containsKey(botón)) {
            botones.put(botón, new EstadoBotón(false, state));
        } else if (botones.get(botón).iluminado == state) {
            return;
        } else {
            botones.get(botón).iluminado = state;
        }
        String name = botón.name().toLowerCase();
		if(name.equals("aumvel")) name = "aumento";
		else if(name.equals("ocultación")) name = "ocultacion";
		else if(name.equals("asfa_básico")) name = "basico";
        write("asfa::pulsador::ilum::"+name, state ? 1 : 0);
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
            PaqueteRegistro.pulsador(botón, 1, state);
        } else {
        	EstadoBotón b = botones.get(botón);
        	if(b.pulsado != state) PaqueteRegistro.pulsador(botón, 1, state);
            b.pulsar(state);
        }
    }

    public boolean pressed(TipoBotón botón) {
    	if (!botones.containsKey(botón)) {
            botones.put(botón, new EstadoBotón(false, false));
        }
        return botones.get(botón).pulsado;
    }
    public void esperarPulsado(TipoBotón botón, Object detector)
    {
    	if (!botones.containsKey(botón)) {
            botones.put(botón, new EstadoBotón(false, false));
        }
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
                write("asfa::indicador::ultima_info", state);
                break;
            case "Velocidad":
                write("asfa::indicador::velocidad", state);
                break;
            case "Velocidad Objetivo":
                write("asfa::indicador::v_control", state);
                break;
            case "EstadoVobj":
                write("asfa::indicador::estado_vcontrol", state);
                break;
            case "Eficacia":
                write("asfa::indicador::eficacia", state);
                break;
            case "Sobrevelocidad":
                write("asfa::indicador::frenado", state);
                break;
            case "Urgencia":
                write("asfa::indicador::urgencia", state);
                break;
            case "Paso Desvío":
                write("asfa::indicador::control_desvio", state);
                break;
            case "Secuencia AA":
                write("asfa::indicador::secuencia_aa", state);
                break;
            case "LVI":
                write("asfa::indicador::lvi", state);
                break;
            case "PN sin protección":
                write("asfa::indicador::pndesp", state);
                break;
            case "PN protegido":
                write("asfa::indicador::pnprot", state);
                break;
            case "Modo":
                write("asfa::indicador::modo", state);
                break;
            case "Tipo":
                write("asfa::indicador::tipo_tren", state);
                break;
            case "Velo":
                write("asfa::indicador::velo", state);
            	break;
        }
    }
    
    int leds[] = new int[3];
    public void led_basico(int led, int state)
    {
    	if (leds[led]!=state)
    	{
    		leds[led] = state;
            write("asfa::leds::"+led, state);
    	}
    }
    
    public void start()
    {
    	if (Main.ASFA.basico) return;
    	orclient.sendData("noretain(asfa::pantalla::iniciar=1)");
    }
    public void apagar()
    {
    	orclient.sendData("noretain(asfa::pantalla::apagar=1)");
    }
    public void set(int num, int errno)
    {
    	if (Main.ASFA.basico) return;
    	write(14, num);
    }
    public void startSound(String num)
    {
    	startSound(num, Main.ASFA.basico);
    }
    public void startSound(String num, boolean basic)
    {
        orclient.sendData("noretain(asfa::sonido::iniciar=" + num + "," + (basic ? 1 : 0) + ")");
    }
    public void stopSound(String num)
    {
        orclient.sendData("noretain(asfa::sonido::detener="+num+")");
    }
    public void write(String fun, int val)
    {
    	write(fun, Integer.toString(val));
    }
    public void write(String fun, String val)
    {
    	orclient.sendData(fun + "=" + val);
    }
}
