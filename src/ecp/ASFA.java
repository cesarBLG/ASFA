package ecp;

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
    Sound Sound;

    public enum Modo {
        CONV,
        BasicoCONV,
        AV,
        BasicoAV,
        RAM,
        BTS,
        MBRA,
        EXT,
        MTO
    }
    public DMI dmi;
    public DIV div;
    int T;
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
    public Modo modo = Modo.CONV;
    public Captador captador = new Captador();
    double O;
    double InicioRebase;
    boolean Eficacia;
    boolean RebaseAuto;
    boolean Fase2;

    public ASFA() {
        Main.ASFA = this;
        dmi = new DMI();
        div = new DIV();
        Sound = new Sound();
        if (!Main.ORconnected) {
            Conex();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    while (true) {
                        Update();
                    }
                }
            });
            t.start();
        } else {
            dmi.pantalla.setup(display.OR.Connected ? 0 : 1);
        }

    }

    public void Conex() {
        divData = div.getData();
        Fase2 = (divData[14] & 1) == 1;
        T = 100;
        //T = Math.min(T, divData[18]);
        Connected = true;
        modo = Modo.CONV;
        Controles.add(new ControlArranque(T));
        dmi.pantalla.setup(divData == null/* || divData[0]==0*/ ? 2 : 0);
        Sound.Trigger("S0");
        display.iluminarTodos(true);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        display.iluminarTodos(false);
        dmi.pantalla.start();
        display.display("Tipo", T);
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

    public void Update() {
        if (!Connected) {
            FE = false;
            return;
        }
        if (!AKT && !CON) {
            FE = true;
        } else if (AKT && !CON) {
            FE = false;
            modo = Modo.EXT;
            Eficacia = false;
        }
        if(display.pulsado(TipoBotón.Modo) >= 0.5) {
        	if(MpS.ToKpH(Odometer.getSpeed())<5) {
        		UltimaInfo = Info.Vía_libre;
        		if(modo == Modo.CONV) {
            		Controles.clear();
            		Controles.addAll(ControlesPN);
            		Controles.addAll(ControlesLVI);
            		Controles.add(new ControlBTS(T, divData[38] & 0xFF));
            		modo = Modo.BTS;
            	}
            	else if(modo == Modo.BTS) {
                	Controles.clear();
                	ControlesPN.clear();
                	ControlesLVI.clear();
                	Controles.add(new ControlManiobras());
            		modo = Modo.MBRA;
            	}
            	else if(modo == Modo.MBRA) {
                	Controles.clear();
                	ControlesPN.clear();
                	ControlesLVI.clear();
                	Controles.add(new ControlArranque(T));
            		modo = Modo.CONV;
            		UltimaInfo = Info.Desconocido;
            	}
        	}
        }
        if (modo == Modo.EXT) {
            return;
        }
        O = Math.min(MpS.ToKpH(Odometer.getSpeed()) + 5, T);
		if (O <= 80) O = 80;
		else if (O <= 100) O = 100;
		else if (O <= 120) O = 120;
		else if (O <= 140) O = 140;
		else if (O <= 160) O = 160;
		else if (O <= 180) O = 180;
		else O = 200;
        O = T; //Deshacer el valor de O temporalmente
        if (O == 100 && (divData[16] & 1) != 0) {
            O = 120;
        }
        if(modo == Modo.MBRA) Eficacia = false;
        else {
            RecepciónBaliza();
            if (RecStart != 0 && RecStart < Clock.getSeconds()) {
                if (UltimaFrecValida == FrecASFA.L1) {
                    if (display.pulsado(TipoBotón.AnPar) >= 0.5) {
                        Sound.Trigger("S2-2");
                        AnuncioParada();
                        RecStart = 0;
                    }
                    if (!Fase2) {
                        if (display.pulsado(TipoBotón.AnPre) >= 0.5) {
                            Sound.Trigger("S2-3");
                            AnuncioPrecaucion();
                            RecStart = 0;
                        }
                        if (display.pulsado(TipoBotón.PrePar) >= 0.5) {
                            Sound.Trigger("S2-4");
                            PreanuncioParada();
                            RecStart = 0;
                        }
                        if (display.pulsado(TipoBotón.PN) >= 0.5) {
                            Sound.Trigger("S2-5");
                            PNDesprotegido();
                            RecStart = 0;
                        }
                        if (display.pulsado(TipoBotón.LVI) >= 0.5) {
                            Sound.Trigger("S2-6");
                            ControlLVIL1F1 c = new ControlLVIL1F1(TiempoUltimaRecepcion, O, modo);
                            ControlesLVI.add(c);
                            Controles.add(c);
                            RecStart = 0;
                        }
                    }
                }
                if (UltimaFrecValida == FrecASFA.L2) {
                    if (display.pulsado(TipoBotón.VLCond) >= 0.5) {
                        Sound.Trigger("S2-2");
                        ViaLibreCondicional();
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L3) {
                    if (!Fase2) {
                        if (display.pulsado(TipoBotón.PN) >= 0.5) {
                            Sound.Trigger("S1-1");
                            PNProtegido();
                            RecStart = 0;
                        }
                    }
                }
                if (UltimaFrecValida == FrecASFA.L5) {
                    if (display.pulsado(TipoBotón.PrePar) >= 0.5) {
                        Sound.Trigger("S2-4");
                        PreanuncioParada();
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L6) {
                    if (display.pulsado(TipoBotón.AnPre) >= 0.5) {
                        Sound.Trigger("S2-3");
                        AnuncioPrecaucion();
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L7) {
                    display.iluminar(TipoBotón.Alarma, true);
                    if (display.pulsado(TipoBotón.Alarma) >= 0.5) {
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L9) {
                    if (display.pulsado(TipoBotón.PN) >= 0.5) {
                        Sound.Trigger("S2-5");
                        PNDesprotegido();
                        RecStart = 0;
                    }
                }
                if (UltimaFrecValida == FrecASFA.L10 || UltimaFrecValida == FrecASFA.L11) {
                    if (display.pulsado(TipoBotón.LVI) >= 0.5) {
                        Sound.Trigger("S2-6");
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
            }
            if (display.pulsado(TipoBotón.Rebase) >= 0.5 && !RebaseAuto) {
                Sound.Trigger("S4");
                RebaseAuto = true;
                InicioRebase = Clock.getSeconds();
                display.iluminar(TipoBotón.Rebase, true);
            }
            if (InicioRebase + 10 < Clock.getSeconds()) {
                RebaseAuto = false;
                display.iluminar(TipoBotón.Rebase, false);
            }
            if (AlarmaStart != 0) {
                if (Eficacia && display.pulsado(TipoBotón.Alarma) >= 0.5) {
                    AlarmaStart = 0;
                    display.iluminar(TipoBotón.Alarma, false);
                    Sound.Stop("S5");
                } else {
                    display.iluminar(TipoBotón.Alarma, true);
                }
            }
        }
        actualizarControles();
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
            Eficacia = false;
            if (frecRecibida != last) {
                if (AlarmaStart == 0 && UltimaFP + 0.5 < Clock.getSeconds()) {
                    Alarma();
                }
            }
            if (frecRecibida == last) {
                if (AlarmaStart == 0 && Odometer.getSpeed() > 1 && UltimaFP + 0.5 < Clock.getSeconds()) {
                    Alarma();
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
                if (Fase2) {
                    Sound.Trigger("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                    AnuncioParada();
                } else {
                    ControlTransitorio = new ControlAnuncioParada(TiempoUltimaRecepcion, O, modo);
                    display.iluminar(TipoBotón.LVI, true);
                    display.iluminar(TipoBotón.PN, true);
                    display.iluminar(TipoBotón.PrePar, true);
                    display.iluminar(TipoBotón.AnPar, true);
                    display.iluminar(TipoBotón.AnPre, true);
                    Sound.Trigger("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                }
            }
            if (frecRecibida == FrecASFA.L2) {
                if (divData[18] > 160) {
                    display.iluminar(TipoBotón.VLCond, true);
                    Sound.Trigger("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                } else {
                    Sound.Trigger("S1-1");
                }
                ViaLibreCondicional();
            }
            if (frecRecibida == FrecASFA.L3) {
                Sound.Trigger("S1-1");
                if (Fase2) {
                    ViaLibre();
                } else {
                    ControlTransitorio = new ControlViaLibre(T, TiempoUltimaRecepcion);
                    display.iluminar(TipoBotón.PN, true);
                    StartRec(TiempoUltimaRecepcion);
                }
            }
            if (frecRecibida == FrecASFA.L4) {
                if (VentanaL4 == -1) {
                    Sound.Trigger("S1-1");
                    ControlTransitorio = new ControlPNProtegido(O, T, TiempoUltimaRecepcion, DistanciaUltimaRecepcion);
                } else {
                    //ControlCambioInfraestructura
                    desactivarControlTransitorio();
                }
            }
            if (frecRecibida == FrecASFA.L5) {
                display.iluminar(TipoBotón.PrePar, true);
                Sound.Trigger("S2-1");
                StartRec(TiempoUltimaRecepcion);
                PreanuncioParada();
            }
            if (frecRecibida == FrecASFA.L6) {
                display.iluminar(TipoBotón.AnPre, true);
                Sound.Trigger("S2-1");
                StartRec(TiempoUltimaRecepcion);
                AnuncioPrecaucion();
            }
            if (frecRecibida == FrecASFA.L7) {
                Sound.Trigger("S6");
                StartRec(TiempoUltimaRecepcion + 1);
                PreviaParada();
            }
            if (frecRecibida == FrecASFA.L8) {
                Sound.Trigger("S6");
                Parada();
            }
            if (frecRecibida == FrecASFA.L9) {
                display.iluminar(TipoBotón.PN, true);
                Sound.Trigger("S2-1");
                StartRec(TiempoUltimaRecepcion);
                PNDesprotegido();
            }
            if (frecRecibida == FrecASFA.L10) {
                if (VentanaL11 != -1 && VentanaL11 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    Sound.Trigger("S1-1");
                    int Vf = 0;
                    if (modo == Modo.CONV || modo == Modo.AV) {
                        Vf = 80;
                    }
                    ControlLVI c = new ControlLVI(O, Vf, true, ControlTransitorio.TiempoInicial);
                    Controles.add(c);
                    ControlesLVI.add(c);
                    VentanaL11 = -1;
                    desactivarControlTransitorio();
                } else if (VentanaL10 != -1 && VentanaL10 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    Sound.Trigger("S1-1");
                    int Vf = 0;
                    if (modo == Modo.CONV || modo == Modo.AV) {
                        Vf = 160;
                    }
                    ControlLVI c = new ControlLVI(O, Vf, true, ControlTransitorio.TiempoInicial);
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
                    Sound.Trigger("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                    ControlTransitorio = new ControlLVI(O, 40, false, TiempoUltimaRecepcion);
                }
            }
            if (frecRecibida == FrecASFA.L11) {
                if (VentanaL11 != -1 && VentanaL11 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    Sound.Trigger("S1-1");
                    double Vf = ControlTransitorio.VC.OrdenadaFinal;
                    ControlLVI c = new ControlLVI(O, Vf, true, ControlTransitorio.TiempoInicial);
                    Controles.add(c);
                    ControlesLVI.add(c);
                    VentanaL11 = -1;
                    desactivarControlTransitorio();
                } else if (VentanaL10 != -1 && VentanaL10 + 8 > DistanciaUltimaRecepcion) {
                    display.iluminar(TipoBotón.LVI, true);
                    Sound.Trigger("S1-1");
                    int Vf = 0;
                    if (modo == Modo.CONV || modo == Modo.AV) {
                        Vf = 120;
                    }
                    ControlLVI c = new ControlLVI(O, Vf, true, ControlTransitorio.TiempoInicial);
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
                    Sound.Trigger("S2-1");
                    StartRec(TiempoUltimaRecepcion);
                    ControlTransitorio = new ControlLVI(O, 40, false, TiempoUltimaRecepcion);
                }
            }
            if (ControlTransitorio != null) {
                if (ControlTransitorio instanceof ControlLVI) {
                    ControlesLVI.add((ControlLVI) ControlTransitorio);
                }
                Controles.add(ControlTransitorio);
            }
        }
    }
    double AlarmaStart = 0;

    void Alarma() {
        AlarmaStart = UltimaFP;
        display.iluminar(TipoBotón.Alarma, true);
        Sound.Trigger("S5");
    }

    private void actualizarControles() {
    	if(modo != Modo.MBRA)
    	{
            if (AlarmaStart != 0 && AlarmaStart + 3 < Clock.getSeconds()) {
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
    	}
    	if(modo == Modo.CONV)
    	{
            if (PrevDist + 450 < Odometer.getDistance() && SigNo == 0) {
                SigNo = 2;
                if (ControlSeñal instanceof ControlPreviaSeñalParada) {
                    Urgencias();
                    UltimaInfo = Info.Anuncio_parada;
                    //Velo();
                    addControlSeñal(new ControlAnuncioParada(0, O, modo));
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
            if (lvi.isReached(Clock.getSeconds(), MpS.ToKpH(Odometer.getSpeed()))) {
                display.iluminar(TipoBotón.LVI, true);
                if (display.pulsado(TipoBotón.LVI) >= 0.5) {
                    display.iluminar(TipoBotón.LVI, false);
                    Caducados.add(lvi);
                }
                if (lvi instanceof ControlLVIL1F1 && lvi.TiempoInicial + 5 < Clock.getSeconds()) {
                    ControlLVIL1F1 control = (ControlLVIL1F1) ((lvi instanceof ControlLVIL1F1) ? lvi : null);
                    if (display.pulsado(TipoBotón.AumVel) >= 0.5) {
                        control.SpeedUp();
                    }
                    if (display.pulsado(TipoBotón.Ocultación) >= 0.5) {
                        control.SpeedDown();
                    }
                }
            }
        }
        if (ControlesPN.size() != 0) {
            if (Odometer.getSpeed() < MpS.FromKpH(40)) {
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
        }
        for (Control c : Caducados) {
            Controles.remove(c);
            ControlesPN.remove(c);
            ControlesLVI.remove((ControlLVI) ((c instanceof ControlLVI) ? c : null));
        }
        ControlActivo = null;
        for (Control c : Controles) {
            if (c == null) {
                continue;
            }
            boolean Condition1 = ControlActivo == null || ControlActivo.getVC(Clock.getSeconds()) > c.getVC(Clock.getSeconds());
            if (Condition1) {
                ControlActivo = c;
                continue;
            }
            boolean Condition2 = ControlActivo.getVC(Clock.getSeconds()) == c.getVC(Clock.getSeconds()) && ControlActivo.VC.OrdenadaFinal > c.VC.OrdenadaFinal;
            if (Condition2) {
                ControlActivo = c;
                continue;
            }
        }
        if (ControlActivo instanceof ControlAumentable && ControlActivo.TiempoRec + 10 > Clock.getSeconds() && !((ControlAumentable) ControlActivo).Aumentado()) {
            display.iluminar(TipoBotón.AumVel, true);
            if (display.pulsado(TipoBotón.AumVel) >= 0.5) {
                ((ControlAumentable) ControlActivo).AumentarVelocidad(true);
                if (ControlActivo instanceof ControlPreanuncioParada) {
                    UltimaInfo = Info.Preanuncio_AV;
                }
            }
        } else if (ControlActivo instanceof ControlLVI && ControlActivo.TiempoInicial + 10 > Clock.getSeconds()) {
            display.iluminar(TipoBotón.AumVel, true);
            if (display.pulsado(TipoBotón.AumVel) >= 0.5) {
                ((ControlLVI) ControlActivo).AumentarVelocidad();
            }
        } else {
            display.iluminar(TipoBotón.AumVel, false);
        }
    }

    private void actualizarEstado() {
        double max = ControlActivo.getIF(Clock.getSeconds());
        double control = ControlActivo.getVC(Clock.getSeconds());
        double target = ControlActivo.VC.OrdenadaFinal;
        double overspeed1 = control + 0.25 * (max - control);
        double overspeed2 = control + 0.5 * (max - control);
        double vreal = MpS.ToKpH(Odometer.getSpeed());
        if (FE) {

        } else if (vreal >= max && !FE) {
            Sound.Stop("S3-2");
            Sound.Stop("S3-1");
            Urgencias();
            display.display("Urgencia", FE ? 1 : 0);
        } else if (vreal >= overspeed2) {
            Sound.Trigger("S3-2");
            Sound.Stop("S3-1");
            display.display("Sobrevelocidad", 2);
        } else if (vreal >= overspeed1) {
            Sound.Trigger("S3-1");
            Sound.Stop("S3-2");
            display.display("Sobrevelocidad", 1);
        } else if (vreal <= control - 3) {
            Sound.Stop("S3-1");
            Sound.Stop("S3-2");
            display.display("Sobrevelocidad", 0);
        }
        if (FE && vreal < 5) {
            display.iluminar(TipoBotón.Rearme, true);
            if (display.pulsado(TipoBotón.Rearme) >= 0.5) {
                FE = false;
                display.iluminar(TipoBotón.Rearme, false);
            }
        }
        display.display("Urgencia", FE ? 1 : 0);
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
        for (Control c : Controles) {
            if (c instanceof ControlPasoDesvío) {
                Desv = true;
            }
            if (c instanceof ControlSecuenciaAA || c instanceof ControlSecuenciaAN_A) {
                SecAA = true;
            }
        }
        display.display("Paso Desvío", Desv ? 1 : 0);
        display.display("Secuencia AA", SecAA ? 1 : 0);
        display.display("PN sin protección", ControlesPN.isEmpty() ? 0 : 1);
        //Display.PNProt = ControlesPN.Exists(x -> x instanceof ControlPNProtegido);
        int lvi = 0;
        if (ControlesLVI.size() != 0) {
            lvi = 2;
            for (ControlLVI c : ControlesLVI) {
                if (!c.isReached(Clock.getSeconds(), MpS.ToKpH(Odometer.getSpeed()))) {
                    lvi = 1;
                }
            }
        }
        display.display("LVI", lvi);
        display.display("Eficacia", Eficacia ? 1 : 0);
        display.display("Modo", modo.ordinal());
        display.display("Velocidad Objetivo", ControlActivo instanceof ControlPreviaSeñalParada ? 0 : (int) target);
        display.display("EstadoVobj", targetdisplay);
        display.display("Velocidad", (int) Math.ceil(MpS.ToKpH(Odometer.getSpeed())));
        display.display("Info", UltimaInfo.ordinal());
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
            Sound.Trigger("S3-3");
        }
        FE = true;
    }

    private void notRec(FrecASFA frec) {
        display.iluminarTodos(false);
        if (frec.ordinal() < 10) {
            desactivarControlTransitorio();
        }
        if (frec == FrecASFA.L3) {
            ViaLibre();
        } else {
            Urgencias();
            if (frec == FrecASFA.L1) {
                AnuncioParada();
            }
            //Velo();
        }
        RecStart = 0;
    }
    private double RecStart;
    private double RecEnd;

    private void StartRec(double time) {
        RecEnd = RecStart = time;
        RecEnd += 3;
    }
    private int SigNo = 2;
    private double PrevDist = 0;

    private void EnlaceBalizas() {
        if (UltimaFrecValida == FrecASFA.L7) {
            if (SigNo == 0) {
                if (Odometer.getDistance() - PrevDist < 80) {
                    SigNo = 1;
                } else {
                    Urgencias();
                    SigNo = 0;
                }
            } else {
                SigNo = 0;
            }
        } else if (UltimaFrecValida == FrecASFA.L8 || (SigNo != 2 && DistanciaUltimaRecepcion - PrevDist < 450)) {
            if (SigNo == 2) {
                AnteriorControlSeñal = ControlSeñal;
            }
            SigNo = 2;
        } else {
            SigNo = 0;
        }
        if (SigNo == 0) {
            AnteriorControlSeñal = ControlSeñal;
        }
        PrevDist = DistanciaUltimaRecepcion;
    }
    private boolean InfoSeñalDistinta;
    private Info UltimaInfo = Info.Desconocido;

    private void addControlSeñal(Control c) {
        ArrayList<Control> Caducados = new ArrayList<Control>();
        for (Control control : Controles) {
            if ((modo != Modo.RAM || InfoSeñalDistinta) && control instanceof ControlFASF) {
                if (control instanceof ControlSeñalParada) {
                    if (control.TiempoVAlcanzada == 0) {
                        control.TiempoVAlcanzada = TiempoUltimaRecepcion;
                    }
                } else {
                    Caducados.add(control);
                }
            }
        }
        Controles.removeAll(Caducados);
        if (AnteriorControlSeñal instanceof ControlPreanuncioParada && c instanceof ControlAnuncioParada) {
            c = new ControlSecuenciaAN_A(Clock.getSeconds(), O, ((ControlPreanuncioParada) ((AnteriorControlSeñal instanceof ControlPreanuncioParada) ? AnteriorControlSeñal : null)).AumentoVelocidad, SigNo == 0, modo);
        }
        double prevOF = 0;
        if(ControlSeñal!=null) prevOF = ControlSeñal.VC.OrdenadaFinal;
        c.TiempoRec = Clock.getSeconds();
        if (c instanceof ControlAumentable) {
            ControlAumentable ca = (ControlAumentable) c;
            ca.AumentarVelocidad(false);
        }
        if(prevOF > c.VC.OrdenadaFinal) c.TiempoInicial = TiempoUltimaRecepcion;
        ControlSeñal = c;
        Controles.add(c);
        if (AnteriorControlSeñal instanceof ControlAnuncioPrecaución) {
            Controles.add(new ControlPasoDesvío(Clock.getSeconds(), ((ControlAnuncioPrecaución) ((AnteriorControlSeñal instanceof ControlAnuncioPrecaución) ? AnteriorControlSeñal : null)).AumentoVelocidad, modo));
        }
        if (AnteriorControlSeñal instanceof ControlAnuncioParada && ControlSeñal instanceof ControlAnuncioParada) {
            Controles.add(new ControlSecuenciaAA(Clock.getSeconds(), modo));
        }
    }

    private void ViaLibre() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlViaLibre)) {
            InfoSeñalDistinta = true;
        }
        Control c = new ControlViaLibre(T, TiempoUltimaRecepcion);
        UltimaInfo = Info.Vía_libre;
        addControlSeñal(c);
    }

    private void ViaLibreCondicional() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlViaLibreCondicional)) {
            InfoSeñalDistinta = true;
        }
        Control c;
        if (divData[18] > 160) {
            boolean Fixed = false;
            if (ControlSeñal.getVC(Clock.getSeconds()) <= 160) {
                Fixed = true;
            }
            if (SigNo != 0 && !Fixed) {
                if (ControlSeñal instanceof ControlViaLibreCondicional) {
                    c = ControlSeñal;
                } else {
                    c = new ControlViaLibreCondicional(ControlSeñal.TiempoInicial, T, O, false);
                }
            } else {
                c = new ControlViaLibreCondicional(TiempoUltimaRecepcion, T, O, Fixed);
            }
        } else {
            c = new ControlViaLibre(T, TiempoUltimaRecepcion);
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
        if (SigNo != 0 && ControlSeñal != null) {
            if (ControlSeñal instanceof ControlAnuncioParada) {
                c = ControlSeñal;
            } else {
                c = new ControlAnuncioParada(ControlSeñal.TiempoInicial, O, modo);
            }
        } else {
            c = new ControlAnuncioParada(TiempoUltimaRecepcion, O, modo);
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
        if (SigNo != 0 && ControlSeñal != null) {
            if (ControlSeñal instanceof ControlAnuncioPrecaución) {
                c = ControlSeñal;
            } else {
                c = new ControlAnuncioPrecaución(ControlSeñal.TiempoInicial, ControlSeñal.DistanciaInicial, O, modo);
            }
        } else {
            c = new ControlAnuncioPrecaución(TiempoUltimaRecepcion, DistanciaUltimaRecepcion, O, modo);
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
                c = ControlSeñal;
            } else {
                c = new ControlPreanuncioParada(ControlSeñal.TiempoInicial, O, modo);
            }
        } else {
            c = new ControlPreanuncioParada(TiempoUltimaRecepcion, O, modo);
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
            Control c = new ControlPreviaSeñalParada(TiempoUltimaRecepcion, O, modo);
            addControlSeñal(c);
        }
        UltimaInfo = Info.Parada;
    }

    private void ZonaLimiteParada() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlZonaLimiteParada)) {
            InfoSeñalDistinta = true;
        }
        Control c = new ControlPreviaSeñalParada(0, O, modo);
        addControlSeñal(c);
    }

    private void Parada() {
        EnlaceBalizas();
        if (!(ControlSeñal instanceof ControlSeñalParada)) {
            InfoSeñalDistinta = true;
        }
        Control c = new ControlSeñalParada(T, modo, TiempoUltimaRecepcion);
        if (RebaseAuto) {
            UltimaInfo = Info.Rebase;
        } else {
            Urgencias();
            UltimaInfo = Info.Parada;
        }
        addControlSeñal(c);
    }

    private void PNProtegido() {
        ControlPNProtegido c = new ControlPNProtegido(O, T, TiempoUltimaRecepcion, DistanciaUltimaRecepcion);
        Controles.add(c);
        ControlesPN.add(c);
    }

    private void PNDesprotegido() {
        ControlPNDesprotegido c = new ControlPNDesprotegido(O, T, modo, TiempoUltimaRecepcion, DistanciaUltimaRecepcion);
        Controles.add(c);
        ControlesPN.add(c);
    }

}
