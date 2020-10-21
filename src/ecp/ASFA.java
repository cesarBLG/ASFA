package ecp;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import dmi.*;
import dmi.Botones.Botón;
import dmi.Botones.Botón.TipoBotón;
import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.Controles.*;

public class ASFA {

    public DisplayInterface display = new DisplayInterface();

    public enum Modo {
        CONV,
        AV,
        RAM,
        BTS,
        MBRA,
        EXT,
        MTO
    }
    public DIV div;
    int T;
    int O;
    public int selectorT = 0;
    boolean curvasT120;
    TrainParameters param = new TrainParameters();
    byte[] divData; //Información del vehículo
    //Transición a LZB/ERTMS
    public boolean AKT = false; //Inhibir freno de urgencia
    public boolean CON = true; //Conexión de ASFA
    //Control de freno de emergencia
    boolean FE = true;
    //Controles
    Control ControlActivo;
    List<Control> Controles = new ArrayList<Control>();
    List<Control> ControlesPN = new ArrayList<Control>();
    List<ControlLVI> ControlesLVI = new ArrayList<ControlLVI>();
    //Estado
    public boolean Connected;
    public boolean Activated;
    public Modo modo;
    public Modo modo_ext;
    public boolean basico = false;
    //Parametros DIV
    boolean modoCONV;
    boolean modoAV;
    boolean modoRAM;
    boolean modoBTS;
    int Vbts;
    int Vmax;
    int Vbasico;
    int distReanudo;
    int tiempoRecParada;
    int distanciaRecParada;
    boolean recMultipleBasico;
    boolean curvasBasicoDigital;
    boolean captacionAV;
    
    //Lectura balizas
    public Captador captador = new Captador();
    boolean Eficacia;
    boolean EficaciaIrrecuperable = false;
    double TiempoValidarFrecuencia = 0.001;
    double TiempoValidarFP = 0.05;
    double TiempoPerdidaFP = 0.05;
    double TiempoAlarmaFrecNormal = 2; //Debería ser igual al tiempo de pérdida FP, se deja así por compatibilidad con simulador

    double InicioRebase;
    boolean RebaseAuto;
    public boolean Fase2;
    
    int ASFA_version=2;
    
    int estadoInicio = 0;

    public ASFA() {
        Main.ASFA = this;
        div = new DIV();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
            	while(!display.orclient.connected()) {
            		try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
                while (true) {
                    synchronized(this)
                    {
                    	Update();
                    	try {
                        	wait(Connected ? (frecRecibida == UltimaFrecProcesada ? 20 : 1) : 500);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
	                }
                }
            }
        });
        t.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                display.write("asfa::cg", 0);
            }
        });
    }

    public void Initialize()
    {
    	Controles.clear();
    	ControlesLVI.clear();
    	ControlesPN.clear();
    	InicioRebase = 0;
    	RebaseAuto = false;
        ControlTransitorio = null;
        ControlSeñal = null;
        AnteriorControlSeñal = null;
        UltimaFrecValida = FrecASFA.FP;
        UltimaFrecProcesada = FrecASFA.FP;
        frecRecibida = FrecASFA.FP;
        TiempoUltimaRecepcion = 0;
        DistanciaUltimaRecepcion = 0;
        VentanaL4 = -1;
        VentanaL10 = -1;
        VentanaL11 = -1;
        VentanaIgnoreL9 = -1;
        UltimaFP = 0;
        EficaciaIrrecuperable = false;
        AlarmaStart = 0;
        AlarmaEnd = 0;
        RecPNStart = 0;
        VeloActivo = false;
        VeloEliminable = false;
        inicioParada = -1;
        finParada = -1;
        RecStart = 0;
        RecEnd = 0;
        RecAumentado = false;
        SigNo = 2;
        PrevDist = 0;
        UltimaInfo = Info.Desconocido;
        UltimoControlAnuncioPrecaucion = 0;
        InicioControlDesvioEspecial = 0;
        ControlDesvioEspecialAumentado = false;
        
        display.reset();
    }
    
    public boolean averia = false;
    
    public synchronized void Conex() {
    	if (Connected) return;
    	display.iluminar(TipoBotón.Conex, false);
    	Initialize();
    	Connected = true;
		ASFA_version = Config.Version;
    	try
    	{
    		FileReader fileReader = new FileReader("div.ini");
    		BufferedReader bufferedReader = new BufferedReader(fileReader);
    		divData = new byte[64];
    		String line = bufferedReader.readLine();
    		while (line != null)
    		{
    			String[] token = line.trim().split(" ");
    			divData[Integer.parseInt(token[0].trim())] = (byte) Integer.parseInt(token[1].trim());
        		line = bufferedReader.readLine();
    		}
    		bufferedReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			divData = null;
		}
    	estadoInicio = 1;
        display.orclient.sendData("asfa::emergency=1");
        display.orclient.sendData("register(simulator_time)");
        try {
            display.iluminarTodos(true);
            display.startSound("S2-1", true);
            display.led_basico(0, 1);
            display.led_basico(1, 1);
            display.led_basico(2, 4);
            Thread.sleep(2000);
            display.stopSound("S2-1");
            display.led_basico(2, 0);
            Thread.sleep(1000);
            display.startSound("S2-1", false);
            display.led_basico(2, 1);
            Thread.sleep(2000);
            display.stopSound("S2-1");
            display.iluminarTodos(false);
            display.led_basico(0, 0);
            display.led_basico(1, 0);
            display.led_basico(2, 0);
            if (Config.Fabricante.equals("INDRA"))
            {
            	display.startSound("S5", false);
            	Thread.sleep(2000);
            	display.stopSound("S5");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte newDivData[] = div.getData();
        if (newDivData != null)
        {
        	divData = newDivData;
			try {
				FileWriter writer = new FileWriter("div.ini");
	        	for(int i=0; i<divData.length; i++)
	        	{
	        		if(divData[i]!=0) writer.write(Integer.toString(i)+" "+((int)divData[i] & 0xFF)+"\n");
	        	}
	        	writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else
        {
        	estadoInicio = 2;
        }
        if(selectorT < 1 || selectorT > 8) selectorT = 8;
        if(divData==null)
        {
        	averia = true;
            display.set(2, 0);
        	return;
        }
        Fase2 = (divData[15] & 1) != 0;
        captacionAV = (divData[15] & 4) != 0;
        modoCONV = (divData[15] & 16) != 0;
        modoAV = (divData[15] & 32) != 0;
        modoRAM = (divData[17] & 4) != 0;
        modoBTS = (divData[17] & 8) != 0;
        Vmax = divData[18] & 0xFF;
        Vbasico = divData[19] & 0xFF;
        Vbts = divData[38] & 0xFF;
        distReanudo = 10*(divData[35] & 0xFF);
        tiempoRecParada = divData[36] & 0xFF;
        distanciaRecParada = 10*(divData[37] & 0xFF);
        recMultipleBasico = (divData[14]&64)!=0;
        curvasBasicoDigital = (divData[14]&128)!=0;
        modo = modoRAM ? Modo.RAM : (modoCONV ? Modo.CONV : Modo.AV);
        basico = display.pressed(TipoBotón.ASFA_básico);
        if(modoRAM) T = 40 + selectorT * 10;
        else T = selectorT<3 ? (70 + selectorT * 10) : (40 + selectorT * 20);
        T = Math.min(T, Vmax);
        if (basico && T>Vbasico) T = Vbasico;
        curvasT120 = T == 100 && (divData[17] & 1) != 0;
        param.curvasT120 = curvasT120;
        param.T = T;
        param.Speed = 0;
        param.Modo = modo;
        param.modoAV = modoAV;
        param.modoCONV = modoCONV;
        param.modoRAM = modoRAM;
        param.ASFA_version = ASFA_version;
        param.basico = basico && !curvasBasicoDigital;
        display.orclient.sendData("asfa::fase=" + (Fase2 ? "2" : "1"));
        for (Entry<TipoBotón, EstadoBotón> b : display.botones.entrySet())
        {
        	if (b.getKey() != TipoBotón.ASFA_básico && b.getKey() != TipoBotón.Conex && b.getValue().averiado(5))
        	{
        		averia = true;
            	return;
        	}
        }
        
        display.set(estadoInicio-1, 0);
        
        while(!basico && !display.pantallaconectada)
    	{
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (display.pressed(TipoBotón.ASFA_básico))
            {
            	basico = true;
		        if(modoRAM) T = 40 + selectorT * 10;
		        else T = selectorT<3 ? (70 + selectorT * 10) : (40 + selectorT * 20);
		        T = Math.min(T, Vmax);
		        if (T>Vbasico) T = Vbasico;
		        param.T = T;
		        param.basico = !curvasBasicoDigital;
            }
    	}
        if (basico)
        {
            try {
            	display.startSound("S1-1", true);
                Thread.sleep(100);
                display.stopSound("S1-1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        Controles.add(new ControlArranque(param));

        if (basico) display.stop();
        else display.start();
        Activated = true;
        PaqueteRegistro.encendido();
        PaqueteRegistro.tipo_tren();
        PaqueteRegistro.modo();
        
        if (!CON) estadoInicio = 0;
    }
    void ApagarSonidos()
    {
    	display.stopSound("S1-1");
    	display.stopSound("S2-1");
    	display.stopSound("S2-2");
    	display.stopSound("S2-3");
    	display.stopSound("S2-4");
    	display.stopSound("S2-5");
    	display.stopSound("S2-6");
    	display.stopSound("S3-1");
    	display.stopSound("S3-2");
    	display.stopSound("S3-3");
    	display.stopSound("S3-4");
    	display.stopSound("S3-5");
    	display.stopSound("S4");
    	display.stopSound("S5");
    	display.stopSound("S6");
    }
    public void Desconex()
    {
    	display.iluminar(TipoBotón.Conex, true);
    	ApagarSonidos();
    	display.pantallaconectada = false;
    	display.stop();
    	display.iluminarTodos(false);
        display.led_basico(0, 0);
        display.led_basico(1, 0);
        display.led_basico(2, 0);
    	display.estadoecp = -1;
    	for (TipoBotón b : TipoBotón.values())
    	{
    		display.botones.get(b).lector = null;
    	}
    	Connected = Activated = false;
    	averia = false;
    	PaqueteRegistro.apagado();
    	Clock.reset_local_time();
    	display.orclient.sendData("unregister(simulator_time)");
        display.orclient.sendData("asfa::emergency=0");
    }
    private Control ControlTransitorio;
    private Control ControlSeñal;
    private Control AnteriorControlSeñal;
    private FrecASFA UltimaFrecValida;
    FrecASFA frecRecibida;
    private double TiempoUltimaRecepcion;
    private double DistanciaUltimaRecepcion;
    private double VentanaL4 = -1;
    private double VentanaL10 = -1;
    private double VentanaL11 = -1;
    private double VentanaIgnoreL9 = -1;

    private boolean prevFE=false;
    
    private class ControlesGuardados
    {
    	Modo modo;
    	List<Control> Controles;
        List<Control> ControlesPN;
        List<ControlLVI> ControlesLVI;
        Info UltimaInfo;
    }
    private ControlesGuardados ControlesGuardados;
    void GuardarControles()
    {
    	ControlesGuardados = new ControlesGuardados();
    	ControlesGuardados.Controles = Controles;
    	ControlesGuardados.ControlesPN = ControlesPN;
    	ControlesGuardados.ControlesLVI = ControlesLVI;
    	ControlesGuardados.UltimaInfo = UltimaInfo;
    	Controles = new ArrayList<>();
    	ControlesPN = new ArrayList<>();
    	ControlesLVI = new ArrayList<>();
    	ControlesGuardados.modo = modo;
    }
    public void Update() {
    	display.esperarPulsado(TipoBotón.Conex, this);
    	if (display.botones.get(TipoBotón.Conex).pulsado && !Connected) Conex();
    	if (!display.botones.get(TipoBotón.Conex).pulsado && Connected) Desconex();
    	display.iluminar(TipoBotón.Conex, !display.botones.get(TipoBotón.Conex).pulsado);
    	
    	//TODO: Enviar datos a registrador jurídico
    	
    	FE &= !AKT;
    	
    	if (FE != prevFE)
    	{
    		PaqueteRegistro.estado_urgencia();
            display.orclient.sendData("asfa::emergency=" + ((FE && Connected) ? "1" : "0"));
    	}
    	prevFE = FE;
        if (!Connected) {
            FE = true;
            return;
        }
        if (!AKT && !CON) {
            FE = true;
        }
        if (!CON && modo != Modo.EXT) {
            modo = Modo.EXT;
            if (modoRAM) modo_ext = Modo.RAM;
            else if (!modoAV && modoCONV) modo_ext = Modo.CONV;
            else if (modoAV && !modoCONV) modo_ext = Modo.AV;
            else //Trenes duales
            {
            	if (modo == Modo.AV) modo_ext = Modo.AV;
            	else modo_ext = Modo.CONV;
            }
            if (basico) display.start();
        	ApagarSonidos();
        	display.led_basico(0, 0);
        	display.led_basico(1, 0);
        	display.led_basico(2, 0);
        	display.controles.clear();
	    	display.iluminarTodos(false);
	    	for (TipoBotón b : TipoBotón.values())
	    	{
	    		display.botones.get(b).lector = null;
	    	}
	    	Initialize();
        }
        if (CON && modo == Modo.EXT) {
    		modo = modo_ext;
			param.Modo = modo;
			Controles.add(new ControlTransicion(param));
			UltimaInfo = Info.Desconocido;
			UltimaFrecProcesada = FrecASFA.FP;
            display.iluminar(TipoBotón.Alarma, false);
            if (basico) display.stop();
            PaqueteRegistro.modo();
        }
        
        for (Entry<TipoBotón, EstadoBotón> b : display.botones.entrySet())
        {
        	if (b.getKey() != TipoBotón.ASFA_básico && b.getKey() != TipoBotón.Conex && b.getValue().averiado())
        	{
        		EficaciaIrrecuperable = true;
        		Eficacia = false;
        		averia = true;
        		FE = true;
        	}
        }
        
		param.Speed = (int)MpS.ToKpH(Odometer.getSpeed());
		param.Modo = modo;

        if (modo == Modo.EXT) {
        	UltimaFrecProcesada = frecRecibida = captador.getData();
        	Eficacia = frecRecibida == FrecASFA.FP && !EficaciaIrrecuperable;
            display.iluminar(TipoBotón.Alarma, !Eficacia);
            display.display("Eficacia", Eficacia ? 1 : 0);
            display.display("Modo", modo.ordinal());
            display.display("ModoEXT", modo_ext.ordinal());
            if (modoAV && modoCONV)
            {
                display.esperarPulsado(TipoBotón.Modo, modo_ext);
                if (display.pulsado(TipoBotón.Modo, modo_ext))
                {
                	if (modo_ext == Modo.AV) modo_ext = Modo.CONV;
                	else modo_ext = Modo.AV;
                }
            }
            return;
        }

		boolean selec_basico = display.pressed(TipoBotón.ASFA_básico);
		if (basico != selec_basico)
		{
			if (Odometer.getSpeed() < 5/3.6f)
			{
				basico = selec_basico;
		        if(modoRAM) T = 40 + selectorT * 10;
		        else T = selectorT<3 ? (70 + selectorT * 10) : (40 + selectorT * 20);
		        T = Math.min(T, Vmax);
		        if (basico && T>Vbasico) T = Vbasico;
		        param.T = T;
		        param.basico = basico && !curvasBasicoDigital;
		        display.controles.clear();
		        if (!basico)
		        {
		        	display.start();
		        	display.led_basico(0, 0);
		        	display.led_basico(1, 0);
		        	display.led_basico(2, 0);
		        }
		        else
		        {
		        	display.stop();
		        }
				for(Control c : Controles)
				{
			        c.T = T;
			        c.basico = basico && !curvasBasicoDigital;
					c.Curvas();
				}
				PaqueteRegistro.modo();
			}
			else FE = true;
		}
		if (!basico && !display.pantallaconectada) FE = true;
		if (!basico) display.iluminar(TipoBotón.Modo, MpS.ToKpH(Odometer.getSpeed())<5);
		else display.iluminar(TipoBotón.Modo, modo == Modo.AV);
		
		if (Odometer.getSpeed() >= 5/3.6f)
			ControlesGuardados = null;
        display.esperarPulsado(TipoBotón.Modo, modo);
        display.botones.get(TipoBotón.Modo).tiempoPulsar = 3;
        if(display.pulsado(TipoBotón.Modo, modo)) {
        	display.botones.get(TipoBotón.Modo).siguientePulsacion = Clock.getSeconds() + 2;
        	if(MpS.ToKpH(Odometer.getSpeed())<5/3.6f) {
				if (!basico) display.iluminar(TipoBotón.Modo, true);
        		if(modo == Modo.CONV || modo == Modo.AV || modo == Modo.RAM) {
        			if (modo == Modo.CONV && modoAV)
        			{
    					modo = Modo.AV;
            			for(Control c : Controles)
            			{
            				c.Modo = modo;
            				c.Curvas();
            			}
    				}
        			else if(modoBTS)
        			{
                		if (ControlesGuardados == null) GuardarControles();
                		modo = Modo.BTS;
                		param.Modo = modo;
                		UltimaInfo = Info.Vía_libre;
                		Controles.clear();
                		ControlesLVI = ControlesGuardados.ControlesLVI;
                		ControlesPN = ControlesGuardados.ControlesPN;
                		Controles.addAll(ControlesPN);
                		Controles.addAll(ControlesLVI);
                		Controles.add(new ControlBTS(param, Vbts));
    				}
        			else
        			{
                		if (ControlesGuardados == null) GuardarControles();
                		UltimaInfo = Info.Vía_libre;
                		modo = Modo.MBRA;
                		param.Modo = modo;
                		Controles.clear();
                    	Controles.add(new ControlManiobras(param));
        			}
                    PaqueteRegistro.modo();
            	}
            	else if(modo == Modo.BTS) {
            		if (ControlesGuardados == null) GuardarControles();
            		UltimaInfo = Info.Vía_libre;
            		modo = Modo.MBRA;
            		param.Modo = modo;
            		Controles.clear();
                	Controles.add(new ControlManiobras(param));
                    PaqueteRegistro.modo();
            	}
            	else if(modo == Modo.MBRA) {
            		if (ControlesGuardados == null) GuardarControles();
            		display.iluminar(TipoBotón.Alarma, AlarmaStart != 0);
            		modo = modoRAM ? Modo.RAM : (modoCONV ? Modo.CONV : Modo.AV);
            		param.Modo = modo;
            		Controles.clear();
            		Controles.addAll(ControlesGuardados.Controles);
            		ControlesPN = ControlesGuardados.ControlesPN;
            		ControlesLVI = ControlesGuardados.ControlesLVI;
            		List<Control> bad = new ArrayList<>();
            		for(Control c : Controles)
            		{
            			if (c instanceof ControlBTS || c instanceof ControlManiobras)
            			{
            				bad.add(c);
            			}
            			else 
            			{
                			c.Modo = modo;
                			c.Curvas();
            			}
            		}
            		Controles.removeAll(bad);
            		if (ControlesGuardados.modo != Modo.CONV && ControlesGuardados.modo != Modo.AV && ControlesGuardados.modo != Modo.RAM)
            		{
            			ControlSeñal = AnteriorControlSeñal = null;
            	        SigNo = 2;
            	        PrevDist = 0;
            			Controles.add(new ControlArranque(param));
            			UltimaInfo = Info.Desconocido;
            		}
            		else UltimaInfo = ControlesGuardados.UltimaInfo;
                    PaqueteRegistro.modo();
            	}
        	}
        	else
        	{
        		if(modoAV && modoCONV && (modo == Modo.AV || modo == Modo.CONV))
        		{
        			if(modo == Modo.CONV) modo = Modo.AV;
        			else modo = Modo.CONV;
        			for(Control c : Controles)
        			{
        				c.Modo = modo;
        				c.Curvas();
        			}
                    PaqueteRegistro.modo();
        		}
        	}
        }
        if(modo == Modo.MBRA)
        {
        	Eficacia = false;
        	UltimaFrecProcesada = frecRecibida = captador.getData();
            display.iluminar(TipoBotón.Alarma, frecRecibida != FrecASFA.FP);
        }
        else {
            RecepciónBaliza();
            if (RecStart != 0 && RecStart < Clock.getSeconds()) {
                if (UltimaFrecValida == FrecASFA.L1) {
                    if (display.pulsado(TipoBotón.AnPar, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-2");
                        if (!Fase2) AnuncioParada();
                        RecStart = 0;
                    }
                    if (!Fase2) {
                        if (display.pulsado(TipoBotón.AnPre, RecStart)) {
                            display.stopSound("S2-1");
                            display.startSound("S2-3");
                            AnuncioPrecaucion();
                            RecStart = 0;
                        }
                        if (display.pulsado(TipoBotón.PrePar, RecStart)) {
                            display.stopSound("S2-1");
                            display.startSound("S2-4");
                            PreanuncioParada();
                            RecStart = 0;
                        }
                        if (display.pulsado(TipoBotón.PN, RecStart)) {
                            display.stopSound("S2-1");
                            display.startSound("S2-5");
                            if(modo == Modo.RAM)
                            {
                            	if(!ControlesPN.isEmpty())
                            	{
                            		Control c = ControlesPN.remove(0);
                            		Controles.remove(c);
                            	}
                            }
                            else PNDesprotegido();
                            RecStart = 0;
                        }
                        if (display.pulsado(TipoBotón.LVI, RecStart)) {
                            display.stopSound("S2-1");
                            display.startSound("S2-6");
                            ControlLVIL1F1 c = new ControlLVIL1F1(TiempoUltimaRecepcion, param);
                            ControlesLVI.add(c);
                            Controles.add(c);
                            RecStart = 0;
                        }
                    }
                }
                if (UltimaFrecValida == FrecASFA.L2) {
                    if (display.pulsado(TipoBotón.VLCond, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-2");
                        ViaLibreCondicional();
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L3) {
                    if (!Fase2) {
                        if (display.pulsado(TipoBotón.PN, RecStart)) {
                            display.startSound("S1-1");
                            if(ASFA_version >= 3) PNProtegido();
                            RecStart = 0;
                        }
                    }
                }
                if (UltimaFrecValida == FrecASFA.L5) {
                    if (display.pulsado(TipoBotón.PrePar, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-4");
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L6) {
                    if (display.pulsado(TipoBotón.AnPre, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-3");
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L7) {
                    display.iluminar(TipoBotón.Alarma, true);
                    if (AlarmaStart == 0) display.esperarPulsado(TipoBotón.Alarma, RecStart);
                    if (display.pulsado(TipoBotón.Alarma, RecStart)) {
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L8)
                {
                	if (display.pulsado(TipoBotón.PN, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-5");
                        PNDesprotegido();
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L9) {
                    if (display.pulsado(TipoBotón.PN, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-5");
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L10 || UltimaFrecValida == FrecASFA.L11) {
                    if (display.pulsado(TipoBotón.LVI, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-6");
                        RecStart = 0;
                    }
                }
                if (RecStart == 0) {
                    display.iluminarTodos(false);
                    if (UltimaFrecValida != FrecASFA.L10 && UltimaFrecValida != FrecASFA.L11) {
                        desactivarControlTransitorio();
                    }
                } else if (RecEnd < Clock.getSeconds()) {
                    notRec(UltimaFrecValida);
                }
                else if(RecEnd < Clock.getSeconds() + 0.5 && !RecAumentado && display.algunoPulsando(RecStart))
                {
                	RecAumentado = true;
                	RecEnd = Clock.getSeconds() + 0.5;
                }
            }
        	if(!RebaseAuto && display.botones.get(TipoBotón.Rebase).lector==null) display.esperarPulsado(TipoBotón.Rebase, RebaseAuto);
            if (display.pulsado(TipoBotón.Rebase, RebaseAuto) && !RebaseAuto) {
                if (!basico) display.startSound("S4");
                RebaseAuto = true;
                InicioRebase = Clock.getSeconds();
                display.iluminar(TipoBotón.Rebase, true);
            }
            if (InicioRebase + 10 < Clock.getSeconds()) {
                display.iluminar(TipoBotón.Rebase, false);
                RebaseAuto = false;
            }
            if (AlarmaStart != 0) {
                display.iluminar(TipoBotón.Alarma, true);
            	if(Eficacia)
            	{
            		display.esperarPulsado(TipoBotón.Alarma, AlarmaStart);
            		if (display.botones.get(TipoBotón.Alarma).pulsado && AlarmaEnd-AlarmaStart<=3) AlarmaEnd = Clock.getSeconds() + 0.5;
            		if (display.pulsado(TipoBotón.Alarma, AlarmaStart))
            		{
                        AlarmaStart = 0;
                        display.iluminar(TipoBotón.Alarma, false);
                        display.stopSound("S5");
                        if (UltimaFrecValida == FrecASFA.L7)
                        	RecStart = 0;
            		}
            	}
            }
        }
        actualizarControles();
        if (FE && MpS.ToKpH(Odometer.getSpeed()) < 5 && AlarmaStart == 0 && !EficaciaIrrecuperable) {
            display.esperarPulsado(TipoBotón.Rearme, FE);
            display.iluminar(TipoBotón.Rearme, estadoInicio!=2 || ((int)(Clock.getSeconds())%2==0));
            if (display.pulsado(TipoBotón.Rearme, FE)) {
                FE = false;
                estadoInicio = 0;
                display.iluminar(TipoBotón.Rearme, false);
            }
        }
        if(display.botones.get(TipoBotón.Ocultación).lector==null) display.esperarPulsado(TipoBotón.Ocultación, VeloActivo);
        if(display.pulsado(TipoBotón.Ocultación, VeloActivo))
        {
        	if (VeloActivo && VeloEliminable)
        	{
        		VeloActivo = false;
        		PaqueteRegistro.ocultacion(1, false);
        	}
        	else if (!VeloActivo) Velo();
        }
        actualizarEstado();
    }
    double UltimaFP = 0;
    double UltimoCambio = 0;

    FrecASFA UltimaFrecProcesada;

    void RecepciónBaliza() {
        InfoSeñalDistinta = false;
        FrecASFA last = frecRecibida;
        frecRecibida = captador.getData();
        if (frecRecibida != last) UltimoCambio = Clock.getSeconds();
        if (frecRecibida == FrecASFA.FP) {
        	if (UltimoCambio + TiempoValidarFP < Clock.getSeconds())
        	{
                if (!EficaciaIrrecuperable) Eficacia = true;
                UltimaFP = Clock.getSeconds();
                UltimaFrecProcesada = frecRecibida;
        	}
        } 
        else if (frecRecibida == FrecASFA.AL) {
            if (AlarmaStart == 0 && UltimaFP + TiempoPerdidaFP < Clock.getSeconds()) {
            	Eficacia = false;
                Alarma();
                UltimaFrecProcesada = frecRecibida;
            }
        } else /*if (UltimoCambio + TiempoValidarFrecuencia < Clock.getSeconds())*/ {
        	if (ASFA_version > 2) {
            	if(finParada != -1) {
            		finParada = -1;
            		display.stopSound("S3-4");
            	}
                for(Control c : Controles) {
                	if (c instanceof ControlSeñalParada) {
                		ControlSeñalParada csp = (ControlSeñalParada) c;
                		if (csp.recStart != -1) {
            				csp.recStart = -1;
            				display.stopSound("S3-5");
            				display.botones.get(TipoBotón.Rebase).lector = null;
            				display.iluminar(TipoBotón.Rebase, false);
            				csp.recCount++;
                		}
                	}
                }
        	}
            if (frecRecibida != UltimaFrecProcesada && UltimaFrecProcesada != FrecASFA.FP) {
                if (AlarmaStart == 0 && UltimoCambio + 0.5 < Clock.getSeconds()) {
                    Eficacia = false;
                    EficaciaIrrecuperable = true;
                    Alarma();
                }
                return;
            }
            if (frecRecibida == UltimaFrecProcesada) {
                if (AlarmaStart == 0 && Odometer.getSpeed() > 5/3.6 && UltimoCambio + TiempoAlarmaFrecNormal < Clock.getSeconds()) {
                    Eficacia = false;
                    Alarma();
                }
                return;
            }
            if (frecRecibida == FrecASFA.L9 && VentanaIgnoreL9 != -1 && VentanaIgnoreL9 + 35 > Odometer.getDistance()) {
                UltimaFrecProcesada = frecRecibida;
            	return;
            }
            if(modo == Modo.BTS && frecRecibida != FrecASFA.L4 && frecRecibida != FrecASFA.L9 && frecRecibida != FrecASFA.L10 && frecRecibida != FrecASFA.L11) {
            	UltimaFrecProcesada = frecRecibida;
            	return;
            }
            if (RecStart != 0 && UltimaFrecValida != FrecASFA.L3) {
                if ((frecRecibida != FrecASFA.L4 || VentanaL4 == -1) && ((frecRecibida != FrecASFA.L10 && frecRecibida != FrecASFA.L11) || (VentanaL10 == -1 && VentanaL11 == -1))) {
                    notRec(UltimaFrecValida);
                    Velo();
                }
            }
            if (frecRecibida != FrecASFA.L10 && frecRecibida != FrecASFA.L11) VentanaL10 = VentanaL11 = -1;
            if (frecRecibida != FrecASFA.L4 && VentanaL4 != -1)
            {
            	ControlesPN.add(ControlTransitorio);
            	ControlTransitorio = null;
            	VentanaL4 = -1;
            }
            PaqueteRegistro.baliza_recibida(frecRecibida);
            UltimaFrecValida = frecRecibida;
            TiempoUltimaRecepcion = Clock.getSeconds();
            DistanciaUltimaRecepcion = Odometer.getDistance();
            if (frecRecibida == FrecASFA.L1) {
                display.iluminar(TipoBotón.AnPar, true);
            	display.esperarPulsado(TipoBotón.AnPar, TiempoUltimaRecepcion);
                if (Fase2) {
                    display.startSound("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                    AnuncioParada();
                } else {
                    ControlTransitorio = new ControlAnuncioParada(TiempoUltimaRecepcion, param);
                    if(modo == Modo.RAM)
                    {
                    	if(!ControlesPN.isEmpty())
                    	{
                        	display.iluminar(TipoBotón.PN, true);
                        	display.esperarPulsado(TipoBotón.PN, TiempoUltimaRecepcion);
                    	}
                    }
                    else
                    {
                    	if (!basico || recMultipleBasico)
                    	{
                        	display.iluminar(TipoBotón.PN, true);
                        	display.iluminar(TipoBotón.PrePar, true);
                        	display.esperarPulsado(TipoBotón.PrePar, TiempoUltimaRecepcion);
                        	display.esperarPulsado(TipoBotón.PN, TiempoUltimaRecepcion);
                    	}
                    }
                    if (!basico || recMultipleBasico)
                	{
	                    display.iluminar(TipoBotón.LVI, true);
	                    display.iluminar(TipoBotón.AnPre, true);
	                	display.esperarPulsado(TipoBotón.AnPre, TiempoUltimaRecepcion);
	                	display.esperarPulsado(TipoBotón.LVI, TiempoUltimaRecepcion);
                	}
                    display.startSound("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                }
            }
            if (frecRecibida == FrecASFA.L2) {
            	if(modo == Modo.RAM) Alarma();
            	else
            	{
                    if (Vmax > 160) {
                        display.iluminar(TipoBotón.VLCond, true);
                    	display.esperarPulsado(TipoBotón.VLCond, TiempoUltimaRecepcion);
                        display.startSound("S2-1");
                        StartRec(TiempoUltimaRecepcion);
                    } else {
                        display.startSound("S1-1");
                    }
                    ViaLibreCondicional();
            	}
            }
            if (frecRecibida == FrecASFA.L3) {
                display.startSound("S1-1");
                if (Fase2) {
                    ViaLibre();
                } else {
                    ControlTransitorio = new ControlViaLibre(param, TiempoUltimaRecepcion);
                    display.iluminar(TipoBotón.PN, true);
                	display.esperarPulsado(TipoBotón.PN, TiempoUltimaRecepcion);
                    StartRec(TiempoUltimaRecepcion);
                }
            }
            if (frecRecibida == FrecASFA.L4) {
                if (VentanaL4 == -1) {
                    display.startSound("S1-1");
                    if(ASFA_version >= 3) ControlTransitorio = new ControlPNProtegido(param, TiempoUltimaRecepcion, DistanciaUltimaRecepcion);
                    VentanaL4 = DistanciaUltimaRecepcion;
                } else {
                    desactivarControlTransitorio();
                	if (modo == Modo.AV)
                	{
                		Urgencias();
                		if (modoCONV)
                		{
                			modo = Modo.CONV;
                			for(Control c : Controles)
                			{
                				c.Modo = modo;
                				c.Curvas();
                			}
                            PaqueteRegistro.modo();
                		}
                		else
                		{
                            Eficacia = false;
                            EficaciaIrrecuperable = true;
                		}
                	}
                }
            }
            if (frecRecibida == FrecASFA.L5) {
            	if(modo == Modo.RAM) Alarma();
            	else {
                    display.iluminar(TipoBotón.PrePar, true);
                	display.esperarPulsado(TipoBotón.PrePar, TiempoUltimaRecepcion);
                    display.startSound("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                    PreanuncioParada();
            	}
            }
            if (frecRecibida == FrecASFA.L6) {
                display.iluminar(TipoBotón.AnPre, true);
            	display.esperarPulsado(TipoBotón.AnPre, TiempoUltimaRecepcion);
                display.startSound("S2-1");
                StartRec(TiempoUltimaRecepcion);
                AnuncioPrecaucion();
            }
            if (frecRecibida == FrecASFA.L7) {
                display.startSound("S6");
                if (ASFA_version >= 3) StartRec(TiempoUltimaRecepcion + 1);
                PreviaParada();
            }
            if (frecRecibida == FrecASFA.L8) {
                display.startSound("S6");
                if(!RebaseAuto) Urgencias();
                if(modo == Modo.RAM && !Fase2)
                {
                	display.esperarPulsado(TipoBotón.PN, TiempoUltimaRecepcion);
                	display.iluminar(TipoBotón.PN, true);
                	StartRec(TiempoUltimaRecepcion);
                	ControlTransitorio = new ControlSeñalParada(param, TiempoUltimaRecepcion, DistanciaUltimaRecepcion, RebaseAuto);
                }
                else Parada();
            }
            if (frecRecibida == FrecASFA.L9) {
                display.iluminar(TipoBotón.PN, true);
            	display.esperarPulsado(TipoBotón.PN, TiempoUltimaRecepcion);
                display.startSound("S2-1");
                StartRec(TiempoUltimaRecepcion);
                PNDesprotegido();
            }
            if (frecRecibida == FrecASFA.L10) {
                if (VentanaL11 != -1 && VentanaL11 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    display.startSound("S1-1");
                    int Vf = 50;
                    if(modoRAM) Vf = 40;
                    ControlLVI c = new ControlLVI(param, Vf, modo != Modo.RAM, ControlTransitorio.TiempoInicial);
                    Controles.add(c);
                    ControlesLVI.add(c);
                    VentanaL11 = -1;
                    desactivarControlTransitorio();
                } else if (VentanaL10 != -1 && VentanaL10 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    display.startSound("S1-1");
                    int Vf = 120;
                    if(modoRAM) Vf = 70;
                    ControlLVI c = new ControlLVI(param, Vf, false, ControlTransitorio.TiempoInicial);
                    Controles.add(c);
                    ControlesLVI.add(c);
                    VentanaL10 = -1;
                    desactivarControlTransitorio();
                } else {
                    if (!Fase2) {
                        VentanaIgnoreL9 = DistanciaUltimaRecepcion;
                    }
                    VentanaL10 = DistanciaUltimaRecepcion;
                    display.iluminar(TipoBotón.LVI, true);
                	display.esperarPulsado(TipoBotón.LVI, TiempoUltimaRecepcion);
                    display.startSound("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                    ControlTransitorio = new ControlLVI(param, 30, false, TiempoUltimaRecepcion);
                }
            }
            if (frecRecibida == FrecASFA.L11) {
                if (VentanaL11 != -1 && VentanaL11 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    display.startSound("S1-1");
                    int Vf = (int) ControlTransitorio.VC.OrdenadaFinal;
                    ControlLVI c = new ControlLVI(param, Vf, modo != Modo.RAM, ControlTransitorio.TiempoInicial);
                    Controles.add(c);
                    ControlesLVI.add(c);
                    VentanaL11 = -1;
                    desactivarControlTransitorio();
                } else if (VentanaL10 != -1 && VentanaL10 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    display.startSound("S1-1");
                    int Vf = 80;
                    if(modoRAM) Vf = 50;
                    ControlLVI c = new ControlLVI(param, Vf, modo != Modo.RAM, ControlTransitorio.TiempoInicial);
                    Controles.add(c);
                    ControlesLVI.add(c);
                    VentanaL10 = -1;
                    desactivarControlTransitorio();
                } else {
                    if (!Fase2) {
                        VentanaIgnoreL9 = DistanciaUltimaRecepcion;
                    }
                    VentanaL11 = DistanciaUltimaRecepcion;
                    display.iluminar(TipoBotón.LVI, true);
                	display.esperarPulsado(TipoBotón.LVI, TiempoUltimaRecepcion);
                    display.startSound("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                    ControlTransitorio = new ControlLVI(param, 30, false, TiempoUltimaRecepcion);
                }
            }
            if (ControlTransitorio != null) {
                if (ControlTransitorio instanceof ControlLVI) {
                    ControlesLVI.add((ControlLVI) ControlTransitorio);
                }
                /*if (ControlTransitorio instanceof ControlPNProtegido) {
                    ControlesPN.add(ControlTransitorio);
                }*/
                Controles.add(ControlTransitorio);
            }
            UltimaFrecProcesada = frecRecibida;
        }
    }
    double AlarmaStart = 0;
    double AlarmaEnd = 0;

    void Alarma() {
    	if (UltimaFP == 0)
    	{
    		UltimaFP = Clock.getSeconds();
    	}
        AlarmaStart = UltimaFP;
        AlarmaEnd = AlarmaStart + 3;
        display.startSound("S5");
    }
    double RecPNStart = 0;
    
    private void actualizarControles() {
    	if(modo != Modo.MBRA)
    	{
            if (AlarmaStart != 0 && AlarmaEnd < Clock.getSeconds()) {
                Urgencias();
            }
            if (VentanaL10 != -1 && VentanaL10 + 8 < Odometer.getDistance()) {
                ControlTransitorio = null;
                VentanaL10 = -1;
                Urgencias();
            }
            if (VentanaL11 != -1 && VentanaL11 + 8 < Odometer.getDistance()) {
                ControlTransitorio = null;
                VentanaL11 = -1;
                Urgencias();
            }
            if(VentanaL4 != -1 && VentanaL4 + 35 < Odometer.getDistance())
            {
            	ControlesPN.add(ControlTransitorio);
            	ControlTransitorio = null;
            	VentanaL4 = -1;
            }
    	}
    	if(modo == Modo.CONV || modo == Modo.AV)
    	{
            if (PrevDist + (modo == Modo.AV ? 600 : 450) < Odometer.getDistance() && SigNo == 0) {
                SigNo = 2;
                if (ControlSeñal instanceof ControlPreviaSeñalParada) {
                    Urgencias();
                    UltimaInfo = Info.Anuncio_parada;
                    Velo();
                    addControlSeñal(new ControlAnuncioParada(0, param));
                } else if (ControlSeñal instanceof ControlSecuenciaAN_A) {
                	addControlSeñal(ControlSeñal);
                }
            }
    	}
        ArrayList<Control> Caducados = new ArrayList<Control>();
        for (Control c : Controles) {
            if ((c.DistanciaVigencia != 0 && c.DistanciaVigencia + c.DistanciaInicial < Odometer.getDistance()) || (c.TiempoVigencia != 0 && c.TiempoVAlcanzada != 0 && c.TiempoVigencia + c.TiempoVAlcanzada < Clock.getSeconds())) {
                Caducados.add(c);
            }
        }
        if (ControlesPN.size() > 6) {
            Urgencias();
            ControlesPN.remove(0);
        }
        if (ControlesLVI.size() > 4) {
            Control candidato = null;
            for (Control lvi : ControlesLVI) {
                if (candidato == null || lvi.getVC(Clock.getSeconds()) > lvi.getVC(Clock.getSeconds())) {
                    candidato = lvi;
                }
            }
            Caducados.add(candidato);
        }
        if (ControlesLVI.size() != 0) {
            ControlLVI lvi = null;
            for (ControlLVI c : ControlesLVI) {
                if (c == null) {
                    Caducados.add(c);
                } else if (lvi == null || lvi.getVC(Clock.getSeconds()) > c.getVC(Clock.getSeconds())) {
                    lvi = c;
                }
            }
            if (RecStart==0 && lvi.isReached(Clock.getSeconds(), MpS.ToKpH(Odometer.getSpeed()))) {
                display.iluminar(TipoBotón.LVI, true);
                display.esperarPulsado(TipoBotón.LVI, lvi);
                if (display.pulsado(TipoBotón.LVI, lvi)) {
                    display.iluminar(TipoBotón.LVI, false);
                    Caducados.add(lvi);
                }
                if (lvi instanceof ControlLVIL1F1 && lvi.TiempoInicial + 5 < Clock.getSeconds() && ASFA_version >= 3) {
                    ControlLVIL1F1 control = (ControlLVIL1F1) ((lvi instanceof ControlLVIL1F1) ? lvi : null);
                    if(display.botones.get(TipoBotón.AumVel).lector == null) display.esperarPulsado(TipoBotón.AumVel, lvi);
                    if (display.pulsado(TipoBotón.AumVel, lvi)) {
                        control.SpeedUp();
                    }
                    Object obj = display.botones.get(TipoBotón.Ocultación).lector;
                    if(obj != ControlReanudo.class && (obj == null || obj.equals(finParada) || obj.equals(VeloActivo))) display.esperarPulsado(TipoBotón.Ocultación, lvi);
                    if (display.pulsado(TipoBotón.Ocultación, lvi)) {
                        control.SpeedDown();
                    }
                }
            }
        }
        if (ControlesPN.size() != 0) {
            if (Odometer.getSpeed() < MpS.FromKpH(modo==Modo.RAM ? 20 : 40)) {
                boolean desproteger = false;
                for (Control c : ControlesPN) {
                    if (c instanceof ControlPNProtegido) {
                        Caducados.add(c);
                        TiempoUltimaRecepcion = Clock.getSeconds();
                        DistanciaUltimaRecepcion = Odometer.getDistance();
                        desproteger = true;
                    }
                }
                if (desproteger) {
                    PNDesprotegido();
                }
            }
            if(modo == Modo.RAM)
            {
                for (Control c : ControlesPN) {
                    if (c instanceof ControlPNDesprotegido) {
                    	if(!((ControlPNDesprotegido) c).Rec && c.DistanciaInicial + 100 < Odometer.getDistance())
                    	{
                    		if(RecStart==0 && RecPNStart==0)
                    		{
                    			RecPNStart = Clock.getSeconds();
                    			display.startSound("S2-1");
                    			display.iluminar(TipoBotón.PN, true);
                    			display.esperarPulsado(TipoBotón.PN, RecPNStart);
                    		}
                    		if(RecPNStart!=0)
                    		{
                    			if(RecPNStart+3<Clock.getSeconds())
                    			{
                    				RecPNStart = 0;
                    				Urgencias();
                    				Caducados.add(c);
                    				DistanciaUltimaRecepcion = c.DistanciaInicial;
                    				TiempoUltimaRecepcion = c.TiempoInicial;
                    				Parada();
                    			}
                    			if(display.pulsado(TipoBotón.PN, RecPNStart))
                    			{
                                    display.stopSound("S2-1");
                                    display.startSound("S2-5");
                    				((ControlPNDesprotegido) c).Rec = true;
                    				RecPNStart = 0;
                        			display.iluminar(TipoBotón.PN, false);
                    			}
                    		}
                    	}
                        if(c.DistanciaInicial + 400 < Odometer.getDistance())
                        {
                        	if(RecStart==0 && RecPNStart==0)
                        	{
                        		display.esperarPulsado(TipoBotón.PN, c);
                        		display.iluminar(TipoBotón.PN, true);
                        		if(display.pulsado(TipoBotón.PN, c))
                        		{
                        			display.iluminar(TipoBotón.PN, false);
                        			Caducados.add(c);
                        		}
                        		else break;
                        	}
                        }
                    }
                }
            }
        }
        Controles.removeAll(Caducados);
        ControlesPN.removeAll(Caducados);
        ControlesLVI.removeAll(Caducados);
        Control anteriorControlActivo = ControlActivo;
        ControlActivo = null;
        for (Control c : Controles) {
        	ControlActivo = controlPrioritario(c, ControlActivo);
        }
        if(!ControlActivo.equals(anteriorControlActivo)) PaqueteRegistro.control_activo();
        boolean some = false;
        for (Control c : Controles)
        {
            if(c instanceof ControlReanudo && ASFA_version >= 3)
            {
            	ControlReanudo cr = (ControlReanudo)c;
            	if(Odometer.getSpeed() < 1) cr.Activar(true);
            	else if(cr.Activado())
            	{
            		if(cr.UltimaDistancia()==-1 || (distReanudo > 0 && cr.UltimaDistancia() + distReanudo < Odometer.getDistance()))
            		{
            			display.startSound("S3-4");
            			some = true;
            			display.esperarPulsado(TipoBotón.Ocultación, ControlReanudo.class);
            			if(display.pulsado(TipoBotón.Ocultación, ControlReanudo.class))
            			{
            				display.stopSound("S3-4");
            				cr.ActualizarDistancia(Odometer.getDistance());
            			}
            			break;
            		}
            	}
            }
        }
        if(!some)
        {
        	if(display.botones.get(TipoBotón.Ocultación).lector == ControlReanudo.class)
        	{
            	if (finParada == -1) display.stopSound("S3-4");
        		display.botones.get(TipoBotón.Ocultación).lector = null;
        	}
        }
        for(Control c : Controles)
        {
        	if (c instanceof ControlSeñalParada && ASFA_version >= 3)
        	{
        		ControlSeñalParada csp = (ControlSeñalParada) c;
        		if(csp.TiempoVAlcanzada==0 && ((modo == Modo.CONV && csp.recCount<2) || (modo == Modo.AV && csp.recCount<3)))
        		{
        			if((csp.recCount == 0 && csp.DistanciaInicial + 200 < Odometer.getDistance()) || (csp.recCount > 0 && ((distanciaRecParada > 0 && csp.lastDistRec + distanciaRecParada < Odometer.getDistance()) || (tiempoRecParada > 0 && csp.lastTimeRec + tiempoRecParada < Clock.getSeconds()))))
        			{
        				if(csp.recStart < 0)
        				{
        					csp.lastTimeRec = Clock.getSeconds();
        					csp.recStart = Clock.getSeconds();
        					csp.lastDistRec = Odometer.getDistance();
        				}
        				display.startSound("S3-5");
        			}
        			if(csp.recStart >= 0)
        			{
        				display.esperarPulsado(TipoBotón.Rebase, csp);
        				display.iluminar(TipoBotón.Rebase, true);
        				if(display.pulsado(TipoBotón.Rebase, csp))
        				{
        					csp.recStart = -1;
        					display.stopSound("S3-5");
        					csp.recCount++;
            				display.iluminar(TipoBotón.Rebase, false);
        				}
        				else if(csp.recStart + 5 < Clock.getSeconds())
        				{
        					csp.recStart = -1;
        					display.stopSound("S3-5");
        					display.botones.get(TipoBotón.Rebase).lector = null;
            				display.iluminar(TipoBotón.Rebase, false);
        					csp.recCount++;
        					Urgencias();
        				}
        			}
        		}
        		break;
        	}
        }
        Control UltimoControl = Controles.get(Controles.size()-1);
        if (UltimoControl instanceof ControlPasoDesvío) UltimoControl = Controles.get(Controles.size()-2);
        if (UltimoControl instanceof ControlAumentable && UltimoControl.TiempoRec + 10 > Clock.getSeconds() && ((ControlAumentable) UltimoControl).Aumentable()) {
            display.iluminar(TipoBotón.AumVel, true);
            display.esperarPulsado(TipoBotón.AumVel, UltimoControl);
            if (display.pulsado(TipoBotón.AumVel, UltimoControl)) {
                ((ControlAumentable) UltimoControl).AumentarVelocidad(true);
                if (UltimoControl instanceof ControlPreanuncioParada) {
                    UltimaInfo = Info.Preanuncio_AV;
                }
            }
        } else if (UltimoControl instanceof ControlLVI && ((ControlLVI)UltimoControl).Aumentable && UltimoControl.TiempoInicial + 5 > Clock.getSeconds()) {
            display.esperarPulsado(TipoBotón.AumVel, UltimoControl);
            display.iluminar(TipoBotón.AumVel, true);
            if (display.pulsado(TipoBotón.AumVel, UltimoControl)) {
                ((ControlLVI) UltimoControl).AumentarVelocidad();
            }
        } else {
            display.iluminar(TipoBotón.AumVel, false);
        }
    }
    Control controlPrioritario(Control c1, Control c2)
    {
    	if(c1 == null) return c2;
    	if(c2 == null) return c1;
    	if(c1.getVC(Clock.getSeconds()) < c2.getVC(Clock.getSeconds())) return c1;
    	if(c1.getVC(Clock.getSeconds()) > c2.getVC(Clock.getSeconds())) return c2;
    	if(c1.VC.OrdenadaFinal < c2.VC.OrdenadaFinal) return c1;
    	if(c1.VC.OrdenadaFinal > c2.VC.OrdenadaFinal) return c2;
    	if(c1 instanceof ControlPasoDesvío) return c1;
    	if(c2 instanceof ControlPasoDesvío) return c2;
    	if(c1 instanceof ControlSecuenciaAA) return c1;
    	if(c2 instanceof ControlSecuenciaAA) return c2;
    	if(c1 instanceof ControlLVI) return c1;
    	if(c2 instanceof ControlLVI) return c2;
    	if(c1 instanceof ControlPNDesprotegido) return c1;
    	if(c2 instanceof ControlPNDesprotegido) return c2;
    	if(c1 instanceof ControlPNProtegido) return c1;
    	if(c2 instanceof ControlPNProtegido) return c2;
    	return c1;
    }
    
    boolean VeloActivo = false;
    boolean VeloEliminable = false;
    private void Velo()
    {
    	Object obj = display.botones.get(TipoBotón.Ocultación).lector;
    	if (obj!=null && obj.equals(VeloActivo)) display.botones.get(TipoBotón.Ocultación).lector = null;
    	VeloEliminable = false;
    	for (Control c : Controles)
    	{
    		if (c instanceof ControlFASF)
    		{
    			c.Velado = true;
    		}
    	}
    	VeloActivo = true;
    	UltimaInfo = Info.Vía_libre;
		PaqueteRegistro.ocultacion(1, true);
    }
    
    double inicioParada = -1;
    double finParada = -1;
    private boolean sobre1=false;
    private boolean sobre2=false;
    private double prevVreal=0;
    private double prevVC=0;
    int sobrevelocidad=0;
    private void actualizarEstado() {
        double max = ControlActivo.getIF(Clock.getSeconds());
        double control = ControlActivo.getVC(Clock.getSeconds());
        double target = ControlActivo.VC.OrdenadaFinal;
        double overspeed1 = control + 0.25 * (max - control);
        double overspeed2 = control + 0.5 * (max - control);
        double vreal = MpS.ToKpH(Odometer.getSpeed());
        if(prevVC == ControlActivo.VC.OrdenadaOrigen && control<prevVC) PaqueteRegistro.inicio_vcontrol();
        if(control == ControlActivo.VC.OrdenadaFinal && control<prevVC) PaqueteRegistro.fin_vcontrol();
        if (Math.abs(vreal-prevVreal)>=2 || (prevVreal > 0 && vreal == 0))
        {
        	PaqueteRegistro.cambio_vreal();
            prevVreal = vreal;
        }
        prevVC = control;
        if (FE) {
        	if(sobre2)
        	{
        		sobre2=false;
                display.stopSound("S3-2");
        	}
        	if(sobre1)
        	{
        		sobre1=false;
                display.stopSound("S3-1");
        	}
            sobrevelocidad = 0;
        } else if (vreal >= max && !FE) {
        	if(sobre2)
        	{
        		sobre2=false;
                display.stopSound("S3-2");
        	}
        	if(sobre1)
        	{
        		sobre1=false;
                display.stopSound("S3-1");
        	}
            Urgencias();
            sobrevelocidad = 0;
        } else if (vreal >= overspeed2 && !basico) {
        	if(!sobre2)
        	{
        		sobre2=true;
                display.startSound("S3-2");
        	}
        	if(sobre1)
        	{
        		sobre1=false;
                display.stopSound("S3-1");
        	}
            sobrevelocidad = 2;
        } else if (vreal >= overspeed1) {
        	if(!sobre1)
        	{
        		sobre1=true;
                display.startSound("S3-1");
        	}
        	if(sobre2)
        	{
        		sobre2=false;
                display.stopSound("S3-2");
        	}
            sobrevelocidad = 1;
        } else if (vreal > control - 3) {
        	if(sobre2)
        	{
        		sobre2=false;
                display.stopSound("S3-2");
        		sobre1=true;
                display.startSound("S3-1");
                sobrevelocidad = 1;
        	}
    	} else {
        	if(sobre2)
        	{
        		sobre2=false;
                display.stopSound("S3-2");
        	}
        	if(sobre1)
        	{
        		sobre1=false;
                display.stopSound("S3-1");
        	}
            sobrevelocidad = 0;
        }
        if((modo == Modo.CONV || modo == Modo.AV || modo == Modo.RAM) && ASFA_version >= 3)
        {
        	if(vreal < 1)
            {
            	if(inicioParada == -1)
            	{
            		inicioParada = Clock.getSeconds();
            		if(finParada != -1)
            		{
                		Object obj = display.botones.get(TipoBotón.Ocultación).lector;
                		if(obj!=null && obj.equals(finParada)) display.botones.get(TipoBotón.Ocultación).lector = null;
            			display.stopSound("S3-4");
            			finParada = -1;
            		}
            	}
            	else if(inicioParada + 5*60 < Clock.getSeconds()) finParada = Clock.getSeconds();
            }
            else inicioParada = -1;
            if(vreal > 1 && finParada != -1)
            {
            	display.startSound("S3-4");
            	if(finParada + 5 < Clock.getSeconds())
            	{
                	if(display.botones.get(TipoBotón.Ocultación).lector!=ControlReanudo.class) display.esperarPulsado(TipoBotón.Ocultación, finParada);
                	if(display.pulsado(TipoBotón.Ocultación, finParada))
                	{
                		display.stopSound("S3-4");
                		finParada = -1;
                	}
            	}
            }
        }
        if (basico)
        {
        	boolean csp = false;
        	boolean cpp = false;
            for (Control c : Controles) {
            	if (c instanceof ControlSeñalParada)
            		csp = true;
            	if (c instanceof ControlPreviaSeñalParada)
            		cpp = true;
            }
            boolean c1 = ControlActivo instanceof ControlPasoDesvío || ControlActivo instanceof ControlSecuenciaAA;
            if (ControlActivo instanceof ControlPNDesprotegido && !((ControlPNDesprotegido)ControlActivo).segundaCurva)
            	c1 = true;
            boolean c2 = ControlActivo instanceof ControlAnuncioParada || ControlActivo instanceof ControlAnuncioPrecaución ||
            		ControlActivo instanceof ControlPreanuncioParada || ControlActivo instanceof ControlLVI || ControlActivo instanceof ControlPNDesprotegido;
            if (csp)
            	c2 = false;
        	display.led_basico(0, Eficacia ? (estadoInicio == 2 ? 2 : 1) : 0);
        	display.led_basico(1, c1 ? 2 : (c2 ? 1 : 0));
        	display.led_basico(2, csp ? 4 : (cpp ? 3 : (ControlActivo instanceof ControlViaLibreCondicional ? 2 : 0)));
        	return;
        }
        display.display("Sobrevelocidad", sobrevelocidad);
        display.display("Urgencia", FE ? 1 : 0);
        int targetdisplay;
        if (ControlActivo instanceof ControlViaLibre || modo == Modo.MBRA) {
            targetdisplay = 0;
        } else if (ControlActivo instanceof ControlPreviaSeñalParada || target == control) {
            targetdisplay = 1;
        } else {
            targetdisplay = 2;
        }
        boolean Desv = false;
        boolean SecAA = false;
        boolean PNprot = false;
        boolean PNdesp = false;
        boolean parpPN = false;
        boolean parpInfo = false;
        Info infoMostrada = UltimaInfo;
        for (Control c : Controles) {
        	if (c.Velado) continue;
            if (c instanceof ControlPasoDesvío) {
                Desv = true;
            }
            if (c instanceof ControlSecuenciaAA) {
                SecAA = true;
            }
            if(c instanceof ControlPNProtegido && ControlesPN.contains(c)) PNprot = true;
            if(c instanceof ControlPNDesprotegido) PNdesp = true;
            if(c instanceof ControlReanudo)
            {
            	ControlReanudo cr = (ControlReanudo)c;
            	if(Odometer.getSpeed() >= 1 && cr.Activado() && (cr.UltimaDistancia()==-1 || cr.UltimaDistancia() + distReanudo < Odometer.getDistance()))
            	{
            		if(c instanceof ControlPNDesprotegido) parpPN = true;
            		if(c instanceof ControlFASF) parpInfo = true;
            	}
            }
            if(c instanceof ControlSeñalParada)
            {
            	ControlSeñalParada csp = (ControlSeñalParada) c;
            	if(csp.recStart != -1) parpInfo = true;
            	infoMostrada = csp.conRebase ? Info.Rebase : Info.Parada;
            }
            if(c instanceof ControlPreviaSeñalParada)
            {
            	infoMostrada = Info.Parada;
            }
        }
        display.display("Paso Desvío", Desv ? 1 : 0);
        display.display("Secuencia AA", SecAA ? 1 : 0);
        display.display("PN sin protección", PNdesp ? (parpPN ? 2 : 1) : 0);
        display.display("PN protegido", PNprot ? 1 : 0);
        int lvi = 0;
        if (ControlesLVI.size() != 0) {
            for (ControlLVI c : ControlesLVI) {
            	if (c.Velado) continue;
            	if (lvi == 0) lvi = 2;
                if (!c.isReached(Clock.getSeconds(), MpS.ToKpH(Odometer.getSpeed()))) {
                    lvi = 1;
                }
            }
        }
        display.display("LVI", lvi);
        display.display("Eficacia", Eficacia ? 1 : 0);
        display.display("Velo", VeloActivo ? 1 : 0);
        double vel = MpS.ToKpH(Odometer.getSpeed());
        if(estadoInicio != 0)
        {
            display.display("Tipo", 0);
            display.display("Modo", -1);
        	display.display("Velocidad", T);
            if (Config.Fabricante.equalsIgnoreCase("LOGYTEL"))
           	{
                display.display("Info", infoMostrada.ordinal()<<1 | (parpInfo ? 1 : 0));
                display.display("Velocidad Objetivo", (ControlActivo instanceof ControlPreviaSeñalParada || ControlActivo instanceof ControlZonaLimiteParada) ? 0 : (int) target);
                display.display("EstadoVobj", targetdisplay);
            }
            else
            {
                display.display("Info", Info.Vía_libre.ordinal()<<1);
                display.display("Velocidad Objetivo", 0);
                display.display("EstadoVobj", 0);
            }
        }
        else
        {
            display.display("Tipo", T);
            display.display("Modo", modo.ordinal());
            display.display("Velocidad", vel < 0.3 ? 0 : (int) Math.ceil(vel-0.01f));
            display.display("Info", infoMostrada.ordinal()<<1 | (parpInfo ? 1 : 0));
            display.display("Velocidad Objetivo", (ControlActivo instanceof ControlPreviaSeñalParada || ControlActivo instanceof ControlZonaLimiteParada) ? 0 : (int) target);
            display.display("EstadoVobj", targetdisplay);
        }
    }

    void desactivarControlTransitorio() {
        if (ControlTransitorio != null) {
            Controles.remove(ControlTransitorio);
            if (ControlTransitorio instanceof ControlLVI) {
                ControlesLVI.remove((ControlLVI) ControlTransitorio);
            }
            ControlTransitorio = null;
        }
    }

    void Urgencias() {
        if (!FE) {
            display.startSound("S3-3");
        }
        FE = true;
    }

    private void notRec(FrecASFA frec) {
        display.iluminarTodos(false);
        
        for(EstadoBotón b : display.botones.values())
        {
        	if(b.lector!=null && b.lector.equals(RecStart)) b.lector = null;
        }
        if (frec.ordinal() < 10) {
            desactivarControlTransitorio();
        }
        if (frec == FrecASFA.L3) {
        	ViaLibre();
        }
        else if (frec == FrecASFA.L8)
        {
        	Parada();
        }
        else {
            Urgencias();
            if (frec == FrecASFA.L1) {
                if (!Fase2) AnuncioParada();
            }
        }
        RecStart = 0;
    }
    private double RecStart;
    private double RecEnd;
    private boolean RecAumentado = false;

    private void StartRec(double time) {
        RecEnd = RecStart = time;
        RecEnd += 3;
        RecAumentado = false;
    }
    private int SigNo = 2;
    private double PrevDist = 0;

    private void EnlaceBalizas() {
        if (UltimaFrecValida == FrecASFA.L7) {
            if (SigNo == 0 && ControlSeñal instanceof ControlPreviaSeñalParada) {
                if (Odometer.getDistance() - PrevDist < 80) {
                    SigNo = 1;
                } else {
                    Urgencias();
                    SigNo = 0;
                    AnteriorControlSeñal = ControlSeñal;
                }
            } else {
                SigNo = 0;
                AnteriorControlSeñal = ControlSeñal;
            }
        }
        else if (modo == Modo.RAM || UltimaFrecValida == FrecASFA.L8 || (SigNo != 2 && DistanciaUltimaRecepcion - PrevDist < (modo == Modo.AV ? 600 : 450))) {
            if (SigNo == 2) {
                AnteriorControlSeñal = ControlSeñal;
            }
            else SigNo = 2;
        } else {
            SigNo = 0;
            AnteriorControlSeñal = ControlSeñal;
        }
        PrevDist = DistanciaUltimaRecepcion;
    }
    private boolean InfoSeñalDistinta;
    private Info UltimaInfo = Info.Desconocido;

    private double UltimoControlAnuncioPrecaucion = 0;
    private double InicioControlDesvioEspecial = 0;
    private boolean ControlDesvioEspecialAumentado = false;
    private void addControlSeñal(Control c) {
        ArrayList<Control> Caducados = new ArrayList<Control>();
        for (Control control : Controles) {
            if(control instanceof ControlFASF) {
            	if (InfoSeñalDistinta && control instanceof ControlSeñalParada && !(c instanceof ControlPreviaSeñalParada)) {
                    if (control.TiempoVAlcanzada == 0) {
                        control.TiempoVAlcanzada = TiempoUltimaRecepcion;
                    }
                }
                else if (InfoSeñalDistinta && control instanceof ControlAnuncioPrecaución && modo == Modo.RAM)
                {
                	if (control.DistanciaInicial == 0) {
                        control.DistanciaInicial = DistanciaUltimaRecepcion;
                    }
                }
                else {
                    Caducados.add(control);
                }
                if(control instanceof ControlSeñalParada)
                {
                	display.botones.get(TipoBotón.Rebase).lector = null;
                	display.stopSound("S3-5");
    				display.iluminar(TipoBotón.Rebase, false);
                }
            }
            if(control instanceof ControlSecuenciaAA || control instanceof ControlPasoDesvío) Caducados.add(control);
        }
        Controles.removeAll(Caducados);
        if (modo != Modo.RAM && (!basico || curvasBasicoDigital) && AnteriorControlSeñal instanceof ControlPreanuncioParada && c instanceof ControlAnuncioParada) {
        	boolean aumento = ((ControlPreanuncioParada)AnteriorControlSeñal).AumentoVelocidad;
        	if (!aumento && SigNo != 0 && ControlSeñal instanceof ControlSecuenciaAN_A) c = ControlSeñal;
        	else c = new ControlSecuenciaAN_A(TiempoUltimaRecepcion, param, aumento, SigNo == 0);
        }
        c.TiempoRec = Clock.getSeconds();
        if(c instanceof ControlReanudo)
        {
        	ControlReanudo cr = (ControlReanudo)c;
        	cr.ActualizarDistancia(-1);
        	cr.Activar(false);
        }
        VeloEliminable = true;
        ControlSeñal = c;
        Controles.add(c);
        if(modo != Modo.RAM && (!basico || curvasBasicoDigital))
        {
            if (AnteriorControlSeñal instanceof ControlAnuncioPrecaución) {
            	if (SigNo == 0 && (ControlSeñal instanceof ControlViaLibre || ControlSeñal instanceof ControlViaLibreCondicional)
            		&& UltimoControlAnuncioPrecaucion != 0 && UltimoControlAnuncioPrecaucion + (modo == Modo.AV ? 600 : 450) > Odometer.getDistance())
				{
            		InicioControlDesvioEspecial = DistanciaUltimaRecepcion;
            		ControlDesvioEspecialAumentado = ((ControlAnuncioPrecaución)AnteriorControlSeñal).AumentoVelocidad;
				}
                Controles.add(new ControlPasoDesvío(param, Clock.getSeconds(), ((ControlAnuncioPrecaución)AnteriorControlSeñal).AumentoVelocidad));
            } else if (InicioControlDesvioEspecial != 0) {
            	if (InicioControlDesvioEspecial != 0 && InicioControlDesvioEspecial + (modo == Modo.AV ? 600 : 450) > Odometer.getDistance())
            	{
            		Controles.add(new ControlPasoDesvío(param, Clock.getSeconds(), ControlDesvioEspecialAumentado));
            	}
            	else InicioControlDesvioEspecial = 0;
            }
            if ((AnteriorControlSeñal instanceof ControlAnuncioParada || AnteriorControlSeñal instanceof ControlSecuenciaAN_A) && ControlSeñal instanceof ControlAnuncioParada) {
                Controles.add(new ControlSecuenciaAA(Clock.getSeconds(), param));
            }
            UltimoControlAnuncioPrecaucion = (ControlSeñal instanceof ControlAnuncioPrecaución) ? DistanciaUltimaRecepcion : 0;
        }
    }

    private void ViaLibre() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlViaLibre)) {
            InfoSeñalDistinta = true;
        }
        Control c = new ControlViaLibre(param, TiempoUltimaRecepcion);
        UltimaInfo = Info.Vía_libre;
        addControlSeñal(c);
    }

    private void ViaLibreCondicional() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlViaLibreCondicional)) {
            InfoSeñalDistinta = true;
        }
        Control c;
        if (Vmax > 160) {
            boolean Fixed = false;
            if (ControlSeñal != null && ControlSeñal.getVC(Clock.getSeconds()) <= 160) {
                Fixed = true;
            }
            if (SigNo != 0 && !Fixed) {
                if (ControlSeñal instanceof ControlViaLibreCondicional) {
                    c = ControlSeñal;
                } else if (ASFA_version >= 3){
                    c = new ControlViaLibreCondicional(ControlSeñal.TiempoInicial, param, false);
                } else {
                	c = new ControlViaLibreCondicional(TiempoUltimaRecepcion, param, Fixed);
                }
            } else {
                c = new ControlViaLibreCondicional(TiempoUltimaRecepcion, param, Fixed);
            }
        } else {
            c = new ControlViaLibre(param, TiempoUltimaRecepcion);
        }
        UltimaInfo = Info.Vía_libre_condicional;
        addControlSeñal(c);
    }
    private void AnuncioParada() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlAnuncioParada)) {
            InfoSeñalDistinta = true;
        }
        Control c;
        if(modo == Modo.RAM)
        {
        	if(ControlSeñal instanceof ControlAnuncioParada) c = ControlSeñal;
        	else c = new ControlAnuncioParada(TiempoUltimaRecepcion, param);
        }
        else
        {
            if (SigNo != 0 && ControlSeñal != null) {
                if (ControlSeñal instanceof ControlAnuncioParada) {
                    c = ControlSeñal;
                } else if (ASFA_version >= 3){
                    c = new ControlAnuncioParada(ControlSeñal.TiempoInicial, param);
                } else {
                    c = new ControlAnuncioParada(TiempoUltimaRecepcion, param);
                }
            } else {
                c = new ControlAnuncioParada(TiempoUltimaRecepcion, param);
            }
        }
        UltimaInfo = Info.Anuncio_parada;
        addControlSeñal(c);
    }

    private void AnuncioPrecaucion() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlAnuncioPrecaución)) {
            InfoSeñalDistinta = true;
        }
        Control c;
        if(modo == Modo.RAM)
        {
        	if(ControlSeñal instanceof ControlAnuncioPrecaución) c = ControlSeñal;
        	else c = new ControlAnuncioPrecaución(TiempoUltimaRecepcion, param);
        }
        else
        {
            if (SigNo != 0 && ControlSeñal != null) {
                if (ControlSeñal instanceof ControlAnuncioPrecaución) {
                    ControlAnuncioPrecaución cs = (ControlAnuncioPrecaución) ControlSeñal;
                	c = cs;
                    if(cs.Aumentado())
                    {
                    	cs.AumentarVelocidad(false);
                    	cs.TiempoInicial = TiempoUltimaRecepcion;
                    }
                } else if (ASFA_version >= 3){
                    c = new ControlAnuncioPrecaución((ControlSeñal instanceof ControlAumentable && ((ControlAumentable) ControlSeñal).Aumentado()) ? TiempoUltimaRecepcion : ControlSeñal.TiempoInicial, param);
                } else {
                    c = new ControlAnuncioPrecaución(TiempoUltimaRecepcion, param);
                }
            } else {
                c = new ControlAnuncioPrecaución(TiempoUltimaRecepcion, param);
            }
        }
        UltimaInfo = Info.Anuncio_precaución;
        addControlSeñal(c);
    }

    private void PreanuncioParada() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlPreanuncioParada)) {
            InfoSeñalDistinta = true;
        }
        Control c;
        if (SigNo != 0 && ControlSeñal != null) {
            if (ControlSeñal instanceof ControlPreanuncioParada) {
                ControlPreanuncioParada cs = (ControlPreanuncioParada) ControlSeñal;
                c = cs;
                if(cs.Aumentado())
                {
                	cs.AumentarVelocidad(false);
                	cs.TiempoInicial = TiempoUltimaRecepcion;
                }
            } else if (ASFA_version >= 3){
                c = new ControlPreanuncioParada(ControlSeñal.TiempoInicial, param);
            } else {
                c = new ControlPreanuncioParada(TiempoUltimaRecepcion, param);
            }
        } else {
            c = new ControlPreanuncioParada(TiempoUltimaRecepcion, param);
        }
        UltimaInfo = Info.Preanuncio;
        addControlSeñal(c);
    }

    private void PreviaParada() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlPreviaSeñalParada)) {
            InfoSeñalDistinta = true;
        }
        if (SigNo == 1) {
            ZonaLimiteParada();
        } else {
            Control c = new ControlPreviaSeñalParada(TiempoUltimaRecepcion, param);
            addControlSeñal(c);
        }
        UltimaInfo = Info.Parada;
    }

    private void ZonaLimiteParada() {
        if (!(ControlSeñal instanceof ControlZonaLimiteParada)) {
            InfoSeñalDistinta = true;
        }
        Control c = new ControlZonaLimiteParada(param);
        addControlSeñal(c);
    }

    private void Parada() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlSeñalParada)) {
            InfoSeñalDistinta = true;
        }
        Control c = new ControlSeñalParada(param, TiempoUltimaRecepcion, DistanciaUltimaRecepcion, RebaseAuto);
        UltimaInfo = RebaseAuto ? Info.Rebase : Info.Parada;
        addControlSeñal(c);
    }

    private void PNProtegido() {
        ControlPNProtegido c = new ControlPNProtegido(param, TiempoUltimaRecepcion, DistanciaUltimaRecepcion);
        Controles.add(c);
        ControlesPN.add(c);
    }

    private void PNDesprotegido() {
        ControlPNDesprotegido c = new ControlPNDesprotegido(param, TiempoUltimaRecepcion, DistanciaUltimaRecepcion);
        Controles.add(c);
        ControlesPN.add(c);
    }

}
