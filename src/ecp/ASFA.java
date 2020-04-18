package ecp;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public Modo modo;
    public boolean basico = false;
    //Parametros DIV
    boolean modoCONV;
    boolean modoAV;
    boolean modoRAM;
    boolean modoBTS;
    int Vbts;
    int Vmax;
    int distReanudo;
    int tiempoRecParada;
    int distanciaRecParada;
    boolean recMultipleBasico;
    boolean curvasBasicoDigital;
    public Captador captador = new Captador();
    int O;
    double InicioRebase;
    boolean Eficacia;
    boolean RebaseAuto;
    boolean Fase2;
    int ASFA_version=2;
    
    int estadoInicio = 0;

    public ASFA() {
        Main.ASFA = this;
        div = new DIV();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    Update();
                    try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            }
        });
        t.start();
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
        frecRecibida = FrecASFA.FP;
        TiempoUltimaRecepcion = 0;
        DistanciaUltimaRecepcion = 0;
        VentanaL4 = -1;
        VentanaL10 = -1;
        VentanaL11 = -1;
        VentanaIgnoreL9 = -1;
        UltimaFP = 0;
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
    
    public synchronized void Conex() {
    	if (Connected) return;
    	Initialize();
    	int defaultSelectorT;
    	try
    	{
    		FileReader fileReader = new FileReader("tipo.ini");
    		BufferedReader bufferedReader = new BufferedReader(fileReader);
    		defaultSelectorT = Integer.parseInt(bufferedReader.readLine().trim());
    		bufferedReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			defaultSelectorT=0;
		}
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
        else estadoInicio = 2;
        display.startSound("S1-1",false);
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
		}
        if (selectorT == 0)
        {
        	estadoInicio = 2;
        	selectorT = defaultSelectorT;
        }
        if(selectorT < 1 || selectorT > 8 || divData==null)
        {
            display.set(2, 0);
        	return;
        }
        Fase2 = (divData[15] & 1) == 1;
        modoCONV = (divData[15] & 16) != 0;
        modoAV = (divData[15] & 32) != 0;
        modoRAM = (divData[17] & 4) != 0;
        modoBTS = (divData[17] & 8) != 0;
        Vmax = divData[18] & 0xFF;
        Vbts = divData[38] & 0xFF;
        distReanudo = 10*(divData[35] & 0xFF);
        tiempoRecParada = divData[36] & 0xFF;
        distanciaRecParada = 10*(divData[37] & 0xFF);
        recMultipleBasico = (divData[14]&64)!=0;
        curvasBasicoDigital = (divData[14]&128)!=0;
        modo = modoRAM ? Modo.RAM : (modoCONV ? Modo.CONV : Modo.AV);
        if(modoRAM) T = 40 + selectorT * 10;
        else T = selectorT<3 ? (70 + selectorT * 10) : (40 + selectorT * 20);
        curvasT120 = T == 100 && (divData[17] & 1) != 0;
        T = Math.min(T, Vmax);
        param.curvasT120 = curvasT120;
        param.T = T;
        param.Speed = 0;
        param.Modo = modo;
        param.modoAV = modoAV;
        param.modoCONV = modoCONV;
        param.modoRAM = modoRAM;
        param.ASFA_version = ASFA_version;
        param.basico = basico && !curvasBasicoDigital;
        display.set(estadoInicio == 2 ? 1 : 0, 0);
        display.iluminarTodos(true);
        display.startSound("S2-1", true);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        display.startSound("S2-1", false);
        display.iluminarTodos(false);
        
        Controles.add(new ControlArranque(param));

        display.start();
        Connected = true;
        PaqueteRegistro.encendido();
        PaqueteRegistro.tipo_tren();
        PaqueteRegistro.modo();
    }
    public void Desconex()
    {
    	Connected = false;
    	PaqueteRegistro.apagado();
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
    public synchronized void Update() {
    	display.esperarPulsado(TipoBotón.Conex, this);
    	if (display.botones.get(TipoBotón.Conex).pulsado && !Connected) Conex();
    	if (!display.botones.get(TipoBotón.Conex).pulsado && Connected) Desconex();
    	
    	//TODO: Enviar datos a registrador jurídico
    	
    	if (FE != prevFE) PaqueteRegistro.estado_urgencia();
    	prevFE = FE;
        if (!Connected) {
            FE = true;
            return;
        }
        if (!AKT && !CON) {
            FE = true;
        } else if (AKT && !CON && modo != Modo.EXT) {
            FE = false;
            modo = Modo.EXT;
            Eficacia = false;
            PaqueteRegistro.modo();
        }
		param.Speed = (int)MpS.ToKpH(Odometer.getSpeed());
		param.Modo = modo;
        
        display.esperarPulsado(TipoBotón.Modo, modo);
        display.botones.get(TipoBotón.Modo).tiempoPulsar = 3;
        if(display.pulsado(TipoBotón.Modo, modo)) {
        	if(MpS.ToKpH(Odometer.getSpeed())<5) {
        		UltimaInfo = Info.Vía_libre;
        		if(modo == Modo.CONV || modo == Modo.AV || modo == Modo.RAM) {
        			if (modo == Modo.CONV && modoAV)
        			{
    					modo = Modo.AV;
    					display.iluminar(TipoBotón.Modo, true);
            			for(Control c : Controles)
            			{
            				c.Modo = modo;
            				c.Curvas();
            			}
    				}
        			else if(modoBTS)
        			{
        				display.iluminar(TipoBotón.Modo, false);
                		modo = Modo.BTS;
                		param.Modo = modo;
                		Controles.clear();
                		Controles.addAll(ControlesPN);
                		Controles.addAll(ControlesLVI);
                		Controles.add(new ControlBTS(param, Vbts));
    				}
        			else
        			{
        				display.iluminar(TipoBotón.Modo, false);
                		modo = Modo.MBRA;
                		param.Modo = modo;
                    	Controles.clear();
                    	ControlesPN.clear();
                    	ControlesLVI.clear();
                    	Controles.add(new ControlManiobras(param));
        			}
                    PaqueteRegistro.modo();
            	}
            	else if(modo == Modo.BTS) {
            		modo = Modo.MBRA;
            		param.Modo = modo;
                	Controles.clear();
                	ControlesPN.clear();
                	ControlesLVI.clear();
                	Controles.add(new ControlManiobras(param));
                    PaqueteRegistro.modo();
            	}
            	else if(modo == Modo.MBRA) {
            		modo = modoRAM ? Modo.RAM : (modoCONV ? Modo.CONV : Modo.AV);
        			display.iluminar(TipoBotón.Modo, modo == Modo.AV);
            		param.Modo = modo;
                	Controles.clear();
                	ControlesPN.clear();
                	ControlesLVI.clear();
                	Controles.add(new ControlArranque(param));
            		UltimaInfo = Info.Desconocido;
                    PaqueteRegistro.modo();
            	}
        	}
        	else
        	{
        		if(modoAV && modoCONV)
        		{
        			if(modo == Modo.CONV) modo = Modo.AV;
        			else modo = Modo.CONV;
        			display.iluminar(TipoBotón.Modo, modo == Modo.AV);
        			for(Control c : Controles)
        			{
        				c.Modo = modo;
        				c.Curvas();
        			}
                    PaqueteRegistro.modo();
        		}
        	}
        }
        if (modo == Modo.EXT) {
            return;
        }
        if(modo == Modo.MBRA) Eficacia = false;
        else {
            RecepciónBaliza();
            if (RecStart != 0 && RecStart < Clock.getSeconds()) {
                if (UltimaFrecValida == FrecASFA.L1) {
                    if (display.pulsado(TipoBotón.AnPar, RecStart)) {
                        display.stopSound("S2-1");
                        display.startSound("S2-2");
                        display.stopSound("S2-1");
                        AnuncioParada();
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
                	display.esperarPulsado(TipoBotón.Alarma, RecStart);
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
                display.startSound("S4");
                RebaseAuto = true;
                InicioRebase = Clock.getSeconds();
                display.iluminar(TipoBotón.Rebase, true);
            }
            if (InicioRebase + 10 < Clock.getSeconds()) {
                display.iluminar(TipoBotón.Rebase, false);
                RebaseAuto = false;
            }
            if (AlarmaStart != 0) {
            	display.esperarPulsado(TipoBotón.Alarma, AlarmaStart);
                display.iluminar(TipoBotón.Alarma, true);
            	if(Eficacia && display.botones.get(TipoBotón.Alarma).pulsado)
            	{
            		if(AlarmaEnd-AlarmaStart<=3) AlarmaEnd = Clock.getSeconds() + 0.5;
            	}
                if (Eficacia && display.pulsado(TipoBotón.Alarma, AlarmaStart)) {
                    AlarmaStart = 0;
                    display.iluminar(TipoBotón.Alarma, false);
                    display.stopSound("S5");
                }
                else display.iluminar(TipoBotón.Alarma, true);
            }
        }
        actualizarControles();
        if (FE && MpS.ToKpH(Odometer.getSpeed()) < 5 && AlarmaStart == 0) {
            display.esperarPulsado(TipoBotón.Rearme, FE);
            display.iluminar(TipoBotón.Rearme, estadoInicio!=2 || ((int)(Clock.getSeconds())%2==0));
            if (display.pulsado(TipoBotón.Rearme, FE)) {
                FE = false;
                estadoInicio = 0;
                display.display("Tipo", T);
                display.iluminar(TipoBotón.Rearme, false);
            }
        }
        actualizarEstado();
    }
    double UltimaFP = 0;

    void RecepciónBaliza() {
        InfoSeñalDistinta = false;
        FrecASFA last = frecRecibida;
        frecRecibida = captador.getData();
        if (frecRecibida == FrecASFA.FP) {
            Eficacia = true;
            UltimaFP = Clock.getSeconds();
        } else {
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
            Eficacia = false;
            if (frecRecibida != last) {
                PaqueteRegistro.baliza_recibida(frecRecibida);
                if (AlarmaStart == 0 && UltimaFP + 0.5 < Clock.getSeconds()) {
                    Alarma();
                }
            }
            if (frecRecibida == last) {
                if (AlarmaStart == 0 && Odometer.getSpeed() > 1 && UltimaFP + 0.5 < Clock.getSeconds()) {
                    //Alarma();
                }
                return;
            }
            if (frecRecibida == FrecASFA.L9 && VentanaIgnoreL9 != -1 && VentanaIgnoreL9 + 35 > DistanciaUltimaRecepcion) {
                return;
            }
            if(modo == Modo.BTS && frecRecibida != FrecASFA.L4 && frecRecibida != FrecASFA.L9 && frecRecibida != FrecASFA.L10&& frecRecibida != FrecASFA.L9) {
            	return;
            }
            if (RecStart != 0 && UltimaFrecValida != FrecASFA.L3) {
                if ((frecRecibida != FrecASFA.L10 && frecRecibida != FrecASFA.L11) || (VentanaL10 == -1 && VentanaL11 == -1)) {
                    notRec(UltimaFrecValida);
                }
            }
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
                    //ControlCambioInfraestructura
                    desactivarControlTransitorio();
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
        }
    }
    double AlarmaStart = 0;
    double AlarmaEnd = 0;

    void Alarma() {
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
            }
            if (VentanaL11 != -1 && VentanaL11 + 8 < Odometer.getDistance()) {
                ControlTransitorio = null;
                VentanaL11 = -1;
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
                    Velo(false);
                    addControlSeñal(new ControlAnuncioParada(0, param));
                }
                for (Control c : Controles) {
                    if (c instanceof ControlSecuenciaAN_A) {
                        addControlSeñal(ControlSeñal);
                    }
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
                if (lvi instanceof ControlLVIL1F1 && lvi.TiempoInicial + 5 < Clock.getSeconds()) {
                    ControlLVIL1F1 control = (ControlLVIL1F1) ((lvi instanceof ControlLVIL1F1) ? lvi : null);
                    if(display.botones.get(TipoBotón.AumVel).lector == null) display.esperarPulsado(TipoBotón.AumVel, lvi);
                    if (display.pulsado(TipoBotón.AumVel, lvi)) {
                        control.SpeedUp();
                    }
                    Object obj = display.botones.get(TipoBotón.Ocultación).lector;
                    if(obj != ControlReanudo.class && (obj == null || obj.equals(finParada))) display.esperarPulsado(TipoBotón.Ocultación, lvi);
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
            		if(cr.UltimaDistancia()==-1 || cr.UltimaDistancia() + distReanudo < Odometer.getDistance())
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
        	display.stopSound("S3-4");
        	if(display.botones.get(TipoBotón.Ocultación).lector == ControlReanudo.class) display.botones.get(TipoBotón.Ocultación).lector = null;
        }
        for(Control c : Controles)
        {
        	if (c instanceof ControlSeñalParada && ASFA_version >= 3)
        	{
        		ControlSeñalParada csp = (ControlSeñalParada) c;
        		if(csp.TiempoVAlcanzada==0 && ((modo == Modo.CONV && csp.recCount<2) || (modo == Modo.AV && csp.recCount<3)))
        		{
        			if((csp.recCount == 0 && csp.DistanciaInicial + 200 < Odometer.getDistance()) || (csp.recCount > 0 && ((csp.lastDistRec + distanciaRecParada < Odometer.getDistance()) || (csp.lastTimeRec + tiempoRecParada < Clock.getSeconds()))))
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
    private void Velo(boolean manual)
    {
    	Object obj = display.botones.get(TipoBotón.Ocultación).lector;
    	if (obj!=null && obj.equals(VeloActivo)) display.botones.get(TipoBotón.Ocultación).lector = null;
    	VeloEliminable = false;
    	for (Control c : Controles)
    	{
    		if (manual || c instanceof ControlFASF)
    		{
    			c.Velado = true;
    		}
    	}
    	UltimaInfo = Info.Vía_libre;
    }
    
    double inicioParada = -1;
    double finParada = -1;
    private boolean sobre1=false;
    private boolean sobre2=false;
    private double prevVreal=0;
    private double prevVC=0;
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
            display.display("Urgencia", FE ? 1 : 0);
            display.display("Sobrevelocidad", 0);
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
            display.display("Sobrevelocidad", 2);
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
            display.display("Sobrevelocidad", 1);
        } else if (vreal <= control - 3) {
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
            display.display("Sobrevelocidad", 0);
        }
        display.display("Urgencia", FE ? 1 : 0);
        if(modo == Modo.CONV || modo == Modo.AV || modo == Modo.RAM)
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
            	else if(inicioParada + 500 < Clock.getSeconds()) finParada = Clock.getSeconds();
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
        if(display.botones.get(TipoBotón.Ocultación).lector==null) display.esperarPulsado(TipoBotón.Ocultación, VeloActivo);
        if(display.pulsado(TipoBotón.Ocultación, VeloActivo))
        {
        	if (VeloActivo && VeloEliminable)
        	{
        		VeloActivo = false;
        	}
        	else Velo(true);
        }
        if(estadoInicio != 0)
        {
            display.display("Eficacia", Eficacia ? 1 : 0);
        	display.display("Velocidad", T);
        	return;
        }
        int targetdisplay;
        if (ControlActivo instanceof ControlViaLibre) {
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
        display.display("Modo", modo.ordinal());
        display.display("Velocidad Objetivo", (ControlActivo instanceof ControlPreviaSeñalParada || ControlActivo instanceof ControlZonaLimiteParada) ? 0 : (int) target);
        display.display("EstadoVobj", targetdisplay);
        display.display("Velocidad", (int) Math.ceil(MpS.ToKpH(Odometer.getSpeed())-0.000001f));
        display.display("Info", UltimaInfo.ordinal()<<1 | (parpInfo ? 1 : 0));
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
            Velo(false);
            if (frec == FrecASFA.L1) {
                AnuncioParada();
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
            c = new ControlSecuenciaAN_A(Clock.getSeconds(), param, ((ControlPreanuncioParada) ((AnteriorControlSeñal instanceof ControlPreanuncioParada) ? AnteriorControlSeñal : null)).AumentoVelocidad, SigNo == 0);
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
