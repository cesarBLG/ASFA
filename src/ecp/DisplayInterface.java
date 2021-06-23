package ecp;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    public boolean averiado()
    {
    	return averiado(10);
    }
    public boolean averiado(double threshold)
    {
    	return (pulsado && rawPress + threshold < Clock.getSeconds());
    }
}

public class DisplayInterface {
	public ASFA ASFA;
    OR_Client orclient;
    ECPStateSerializer serialClient;
    public boolean pantallaconectada=false;
    
    Hashtable<TipoBotón, EstadoBotón> botones = new Hashtable<TipoBotón, EstadoBotón>();
    
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
    	serialClient = new ECPStateSerializer(this);
    }

    void reset()
    {
    	TipoBotón[] t = TipoBotón.values();
        for (int i = 0; i < t.length; i++) {
            EstadoBotón e = botones.get(t[i]);
            if (e == null) continue;
            e.iluminado = false;
            e.lector = LectorBoton.Ninguno;
        }
        controles.clear();
        serialClient.cambio();
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
        serialClient.cambioRepetidor();
    }

    public void iluminarTodos(boolean state) {
        TipoBotón[] t = TipoBotón.values();
        for (int i = 0; i < t.length; i++) {
            if (t[i]!=TipoBotón.Conex && t[i]!=TipoBotón.ASFA_básico) iluminar(t[i], state);
        }
    }

    public void pulsar(TipoBotón botón, boolean state) {
    	synchronized(ASFA) {
	        if (!botones.containsKey(botón)) {
	            botones.put(botón, new EstadoBotón(state, false));
	            ASFA.Registro.pulsador(botón, 1, state);
	        } else {
	        	EstadoBotón b = botones.get(botón);
	        	if(b.pulsado != state) ASFA.Registro.pulsador(botón, 1, state);
	            b.pulsar(state);
	        }
    	}
    }

    public boolean pressed(TipoBotón botón) {
    	if (!botones.containsKey(botón)) {
            botones.put(botón, new EstadoBotón(false, false));
        }
        return botones.get(botón).pulsado;
    }
    public void esperarPulsado(TipoBotón botón, LectorBoton detector)
    {
    	if (!botones.containsKey(botón)) {
            botones.put(botón, new EstadoBotón(false, false));
        }
    	botones.get(botón).esperarPulsado(detector);
    }
    public boolean pulsado(TipoBotón botón, LectorBoton detector) {
        return botones.get(botón).flancoPulsado(detector);
    }
    public boolean algunoPulsando(LectorBoton detector)
    {
    	for(EstadoBotón b : botones.values())
    	{
    		if(b.startTime != 0 && detector == b.lector) return true;
    	}
    	return false;
    }
    public boolean iluminado(TipoBotón boton)
    {
    	EstadoBotón e = botones.get(boton);
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
        serialClient.cambioDisplay();
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
            write("asfa::leds::"+led, state);
    	}
        serialClient.cambioRepetidor();
    }
    
    /*public void poweron()
    {
    	if (Main.ASFA.basico) return;
    	orclient.sendData("noretain(asfa::pantalla::encender=1)");
    }*/
    public boolean pantallaactiva=false;
    public void start()
    {
    	pantallaactiva=true;
    	orclient.sendData("asfa::pantalla::activa=1");
    }
    public void stop()
    {
    	pantallaactiva=false;
    	orclient.sendData("asfa::pantalla::activa=0");
    }
    public String estadoecp = "";
    public void set(int num, List<Integer> errors)
    {
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
    	orclient.sendData("asfa::ecp::estado="+num+","+msg);
    }
    public void startSound(String num)
    {
    	startSound(num, ASFA.basico);
    }
    public void startSound(String num, boolean basic)
    {
        orclient.sendData("noretain(asfa::sonido::iniciar=" + num + "," + (basic ? 1 : 0) + ")");
        ASFA.Registro.sonido(num, basic);
        serialClient.sendSonido(num, basic, true);
    }
    public void stopSound(String num)
    {
        orclient.sendData("noretain(asfa::sonido::detener="+num+")");
        ASFA.Registro.sonido("", false);
        serialClient.sendSonido(num, false, false);
    }
    public void stopSoundNoLog(String num)
    {
        orclient.sendData("noretain(asfa::sonido::detener="+num+")");
        serialClient.sendSonido(num, false, false);
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
