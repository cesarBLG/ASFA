package ecp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.COM;
import com.ECPStateSerializer;
import com.OR_Client;

import dmi.Sonidos;
import dmi.Botones.Botón;
import dmi.Botones.Botón.*;
import ecp.ASFA.Modo;
import ecp.EstadoBotón.LectorBoton;

class EstadoBotón {

    public boolean pulsado;
    public boolean iluminado;
    public double startTime = 0;
    public double tiempoPulsar = 0.5;
    public double siguientePulsacion = -1;
    double rawPress = 0;
    
    enum LectorBoton
    {
    	Ninguno,
    	Reconocimiento,
    	Alarma,
    	Rearme,
    	Reanudo,
    	FinParada,
    	ReconocimientoPeriodico,
    	Rebase,
    	CambioModo,
    	EliminarControles,
    	Aumento,
    	VelocidadLVI,
    	Velo
    }
    
    LectorBoton lector = LectorBoton.Ninguno;
    
    public EstadoBotón(boolean p, boolean i) {
        iluminado = i;
        pulsar(p);
    }
    public void pulsar(boolean p)
    {
    	if(p == pulsado) return;
    	pulsado = p;
    	rawPress = Clock.getSeconds();
    	if(pulsado && lector != LectorBoton.Ninguno && siguientePulsacion < Clock.getSeconds()) startTime = Clock.getSeconds();
    	else startTime = 0;
    }
    public void esperarPulsado(LectorBoton detector)
    {
    	if(detector == lector) return;
    	if (pulsado && Config.Fabricante.equalsIgnoreCase("DIMETRONIC")) return;
    	startTime = 0;
    	lector = detector;
    }
    public boolean flancoPulsado(LectorBoton detector)
    {
    	return flancoPulsado(detector, tiempoPulsar);
    }
    public boolean flancoPulsado(LectorBoton detector, double tiempo)
    {
    	if(detector != lector) return false;
    	boolean val = startTime!=0 && Clock.getSeconds() - startTime >= tiempo;
    	if(val)
    	{
    		startTime = 0;
    		lector = LectorBoton.Ninguno;
    	}
    	return val;
    }
    public boolean averiado(double threshold)
    {
    	return (pulsado && rawPress + threshold < Clock.getSeconds());
    }
}

public class DisplayInterface {
	public ASFA ASFA;
    public OR_Client orclient;
    public List<ECPStateSerializer> serialClients = new ArrayList<ECPStateSerializer>();
    public int cabinaActual = 1;
    public int cabinaActiva;
    public boolean pantallaconectada = false;
    public boolean pantallaactiva = false;
    
    Hashtable<TipoBotón, EstadoBotón> botonesCab1 = new Hashtable<>();
    Hashtable<TipoBotón, EstadoBotón> botonesCab2 = new Hashtable<>();
    Hashtable<TipoBotón, EstadoBotón> botoneraActiva = botonesCab1;
    
    byte controlByte(int n1, int n2) {
        int number = (n1 << 8) | n2;
        int control = 0;
        String s = Integer.toString(number);
        for (int i = 0; i < 16; i++) {
            control += i * ((number >> i) & 1);
        }
        return (byte) control;
    }

    public DisplayInterface(ASFA asfa) {
    	ASFA = asfa;
    	orclient = new OR_Client(ASFA);
    	serialClients.add(new ECPStateSerializer(this));
    	//serialClients.add(new ECPStateSerializer(this, "/dev/ttyUSB1"));
    }

    public void setCabinaActual(int num)
    {
    	if (cabinaActual != num)
    	{
    		cabinaActual = num;
    		Hashtable<TipoBotón, EstadoBotón> bots = cabinaActual == 1 ? botonesCab1 : botonesCab2;
    		for (Entry<TipoBotón, EstadoBotón> b : bots.entrySet())
    		{
                String name = b.getKey().name().toLowerCase();
        		if(name.equals("aumvel")) name = "aumento";
        		else if(name.equals("ocultación")) name = "ocultacion";
        		else if(name.equals("asfa_básico")) name = "basico";
                write("asfa::pulsador::ilum::"+name, b.getValue().iluminado ? 1 : 0);
    		}
    		for (int i=0; i<3; i++)
    		{
    			write("asfa::leds::"+i, cabinaActiva == cabinaActual ? leds[i] : 0);
    		}
    		orclient.sendData("asfa::pantalla::habilitada="+(cabinaActiva == cabinaActual && pantallahabilitada ? "1" : "0"));
    	}
    }
    
    void reset()
    {
    	TipoBotón[] t = TipoBotón.values();
        for (int i = 0; i < t.length; i++) {
            EstadoBotón e = botonesCab1.get(t[i]);
            if (e == null) continue;
            e.iluminado = false;
            e.lector = LectorBoton.Ninguno;
        }
        for (int i = 0; i < t.length; i++) {
            EstadoBotón e = botonesCab2.get(t[i]);
            if (e == null) continue;
            e.iluminado = false;
            e.lector = LectorBoton.Ninguno;
        }
        controles.clear();
        serialClients.forEach((c) -> c.cambio());
    }
    
    public void iluminar(TipoBotón botón, boolean state) {
    	if (botón == TipoBotón.Conex)
    	{
    		boolean changed1 = true;
    		boolean changed2 = true;
            if (!botonesCab1.containsKey(botón)) {
            	botonesCab1.put(botón, new EstadoBotón(false, state));
            } else if (botonesCab1.get(botón).iluminado == state) {
                changed1 = false;
            } else {
            	botonesCab1.get(botón).iluminado = state;
            }
            if (!botonesCab2.containsKey(botón)) {
            	botonesCab2.put(botón, new EstadoBotón(false, state));
            } else if (botonesCab2.get(botón).iluminado == state) {
                changed2 = false;
            } else {
            	botonesCab2.get(botón).iluminado = state;
            }
            if (changed1 || changed2)
            {
                write("asfa::pulsador::ilum::conex", state ? 1 : 0);
                serialClients.forEach((c) -> c.cambioRepetidor());
            }
    		return;
    	}
        if (!botoneraActiva.containsKey(botón)) {
        	botoneraActiva.put(botón, new EstadoBotón(false, state));
        } else if (botoneraActiva.get(botón).iluminado == state) {
            return;
        } else {
        	botoneraActiva.get(botón).iluminado = state;
        }
        if (cabinaActiva == cabinaActual)
        {
            String name = botón.name().toLowerCase();
    		if(name.equals("aumvel")) name = "aumento";
    		else if(name.equals("ocultación")) name = "ocultacion";
    		else if(name.equals("asfa_básico")) name = "basico";
            write("asfa::pulsador::ilum::"+name, state ? 1 : 0);
        }
        serialClients.forEach((c) -> c.cambioRepetidor());
    }

    public void iluminarTodos(boolean state) {
        TipoBotón[] t = TipoBotón.values();
        for (int i = 0; i < t.length; i++) {
            if (t[i]!=TipoBotón.Conex && t[i]!=TipoBotón.ASFA_básico) iluminar(t[i], state);
        }
    }

    public void pulsar(TipoBotón botón, boolean state) {
    	synchronized(ASFA) {
    		Hashtable<TipoBotón, EstadoBotón> bots = cabinaActual == 1 ? botonesCab1 : botonesCab2;
	        if (!bots.containsKey(botón)) {
	            bots.put(botón, new EstadoBotón(state, false));
	            ASFA.Registro.pulsador(botón, cabinaActual == -1 ? 2 : 1, state);
	        } else {
	        	EstadoBotón b = bots.get(botón);
	        	if(b.pulsado != state) ASFA.Registro.pulsador(botón, cabinaActual == -1 ? 2 : 1, state);
	            b.pulsar(state);
	        }
    	}
    }

    public boolean pressed(TipoBotón botón) {
    	if (!botoneraActiva.containsKey(botón)) {
    		botoneraActiva.put(botón, new EstadoBotón(false, false));
        }
        return botoneraActiva.get(botón).pulsado;
    }
    public void esperarPulsado(TipoBotón botón, LectorBoton detector)
    {
    	if (!botoneraActiva.containsKey(botón)) {
    		botoneraActiva.put(botón, new EstadoBotón(false, false));
        }
    	botoneraActiva.get(botón).esperarPulsado(detector);
    }
    public boolean pulsado(TipoBotón botón, LectorBoton detector) {
        return botoneraActiva.get(botón).flancoPulsado(detector);
    }
    public boolean algunoPulsando(LectorBoton detector)
    {
    	for(EstadoBotón b : botoneraActiva.values())
    	{
    		if(b.startTime != 0 && detector == b.lector) return true;
    	}
    	return false;
    }
    public boolean iluminado(TipoBotón boton)
    {
    	EstadoBotón e = botoneraActiva.get(boton);
    	return e != null && e.iluminado;
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
            case "Velocidad Objetivo Degradada":
                write("asfa::indicador::vcontrol_degradada", state);
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
            case "ModoEXT":
                write("asfa::indicador::tipo_tren", Modo.values()[state].name());
                break;
            case "Tipo":
                write("asfa::indicador::tipo_tren", state);
                break;
            case "Velo":
                write("asfa::indicador::velo", state);
            	break;
        }
        serialClients.forEach((c) -> c.cambioDisplay());
    }
	public int getDisplayValue(String name)
	{
		Integer i = controles.get(name);
		if (i == null) return 0;
		return i;
	}
    
    public int leds[] = new int[3];
    public void led_basico(int led, int state)
    {
    	if (leds[led]!=state)
    	{
    		leds[led] = state;
            if (cabinaActiva == cabinaActual) write("asfa::leds::"+led, state);
    	}
        serialClients.forEach((c) -> c.cambioRepetidor());
    }
    
    public boolean pantallahabilitada = false;
    public void start()
    {
    	pantallahabilitada = true;
    	if (cabinaActiva == cabinaActual) orclient.sendData("asfa::pantalla::habilitada=1");
    }
    public void stop()
    {
    	pantallahabilitada = false;
    	if (cabinaActiva == cabinaActual) orclient.sendData("asfa::pantalla::habilitada=0");
    }
    public String estadoecp = "";
    public void set(int num, List<Integer> errors)
    {
    	if (num < 0)
    	{
    		estadoecp = "";
    		orclient.sendData("asfa::ecp::estado=");
    		return;
    	}
    	String msg = "";
    	if (errors != null)
    	{
    		msg += "<html>";
        	for (int i=0; i<errors.size(); i++)
        	{
        		if (i>0) msg += "<br/>";
            	switch (errors.get(i))
            	{
        	    	case 1:
        	    		msg += "Fallo de comunicacion con DIV<br/><center>(Informacion redundante)</center>";
        	    		break;
        	    	case 2:
        	    		msg += "Fallo de comunicacion con DIV";
        	    		break;
        	    	case 3:
        	    		msg += "Fallo de pulsador";
        	    		break;
        	    	case 4:
        	    		msg += "Fallo subsistema de captacion";
        	    			break;
            	}
        	}
        	msg += "</html>";
    	}
    	estadoecp = num+","+msg;
    	//String msg2 = msg;
    	orclient.sendData("asfa::ecp::estado="+num+","+msg);
    	//serialClients.forEach((c) -> c.estadoECP(num, msg2));
    }
    public void startSound(String num)
    {
    	startSound(num, ASFA.basico);
    }
    public void startSound(String num, boolean basic)
    {
        orclient.sendData("noretain(asfa::sonido::iniciar=" + num + "," + (basic ? 1 : 0) + ")");
        ASFA.Registro.sonido(num, basic);
        serialClients.forEach((c) -> c.queueSonido(num, basic, true));
    }
    public void stopSound(String num)
    {
        orclient.sendData("noretain(asfa::sonido::detener="+num+")");
        ASFA.Registro.sonido("", false);
        serialClients.forEach((c) -> c.queueSonido(num, false, false));
    }
    public void stopSoundNoLog(String num)
    {
        orclient.sendData("noretain(asfa::sonido::detener="+num+")");
        serialClients.forEach((c) -> c.queueSonido(num, false, false));
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
