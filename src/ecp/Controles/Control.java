package ecp.Controles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import ecp.ASFA;
import ecp.Clock;
import ecp.Config;
import ecp.FrecASFA;
import ecp.Controles.TablaCurva.ModoCurvas;

public abstract class Control {

    public Curva IF = null;
    public Curva VC = null;
    public double TiempoInicial = 0;
    public double DistanciaInicial = 0;
    public double TiempoVigencia = 0;
    public double DistanciaVigencia = 0;
    public double TiempoVAlcanzada = 0;
    public double TiempoRec = 0;
    public int T;
    public int speed;
    public boolean curvasT120;
    public int ASFA_version;
    boolean modoRAM;
    public ASFA.Modo Modo;
    public boolean basico = false;
    public boolean Velado = false;
    
    static HashMap<List<String>,ConjuntoCurvas> AlmacenCurvas = new HashMap<>();
    
    public Control(double t0, double d0, double tv, double dv, TrainParameters param) {
        TiempoInicial = t0;
        DistanciaInicial = d0;
        TiempoVigencia = tv;
        DistanciaVigencia = dv;
        TiempoRec = Clock.getSeconds();
        curvasT120 = param.curvasT120;
        speed = param.Speed;
        T = param.T;
        Modo = param.Modo;
        modoRAM = param.modoRAM;
        basico = param.basico;
        ASFA_version = param.ASFA_version;
    }
    
    public static List<TablaCurva> cargarCurgasConfigurables()
    {
    	try {
    		FileReader file = new FileReader("curvas.cfg");
			BufferedReader bufferedReader = new BufferedReader(file);
			List<TablaCurva> tablas = new ArrayList<>();
			while (bufferedReader.ready()) {
				tablas.add(new TablaCurva(bufferedReader));
			}
			return tablas;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    public static void generarFamiliaCurvas(List<TablaCurva> tablas)
    {
    	AlmacenCurvas.clear();
    	for (TablaCurva t : tablas) {
    		List<String> l = Arrays.asList(new String[] {t.Control, t.Extra});
    		ConjuntoCurvas conj = AlmacenCurvas.get(l); 
    		if (conj == null) {
    			conj = new ConjuntoCurvas();
    			conj.Control = t.Control;
    			conj.Extra = t.Extra;
    			AlmacenCurvas.put(l, conj);
    		}
    		conj.addCurva(t);
    	}
    	try {
    		FileWriter file = new FileWriter("curvas.lua");
			BufferedWriter w = new BufferedWriter(file);
			w.write("ASFADcurva = {\n");
			for (ConjuntoCurvas conj : AlmacenCurvas.values())
			{
				conj.ToLua(w);
			}
			w.write("}\n");
			w.flush();
			file.close();
    	}catch(Exception e) {e.printStackTrace();}
    }
    public Curva[] obtenerCurvasAlmacen(int T)
    {
    	return obtenerCurvasAlmacen(T, false, null);
    }
    public Curva[] obtenerCurvasAlmacen(int T, boolean aumento, String especial)
    {
    	if (AlmacenCurvas == null) return null;
    	List<String> validez = Arrays.asList(new String[] {getClass().getSimpleName(), especial});
    	ConjuntoCurvas conj = AlmacenCurvas.get(validez);
    	if (conj == null) return null;
    	TablaCurva tab = conj.getTabla(ModoCurvas.fromModo(Modo, basico, modoRAM), aumento);
    	if (tab == null) return null;
    	return tab.getCurvas(T);
    }
    TablaCurva construirTablaPorDefecto(List<ModoCurvas> modos, boolean aumento, String especial)
    {
    	TablaCurva c = new TablaCurva();
    	c.Modos = modos;
    	c.ConAumento = aumento;
    	c.Control = getClass().getSimpleName();
    	c.Extra = especial;
    	int[] Ts;
    	if (modos.contains(ModoCurvas.RAM) || modos.contains(ModoCurvas.BAS_RAM) || modos.contains(ModoCurvas.BTS_RAM)) Ts = new int[] {50,60,70,80,90,100,110,120};
    	else Ts = new int[] {80,90,100,120,140,160,180,200};
    	for (int t : Ts) {
    		T = t;
    		Curva[] curvas = getCurvas_AESF(t, t);
    		if (curvas == null) continue;
    		if (ASFA_version < 3) curvas = getCurvas_ADIF(t);
    		c.tabla.put(t, curvas);
    	}
    	if (c.tabla.isEmpty()) return null;
    	return c;
    }
    List<TablaCurva> generarTablasControl()
    {
    	if (this instanceof ControlLVI && !(this instanceof ControlLVIL1F1) && !(this instanceof ControlPreanuncioLTV))
    	{
    		ControlLVI lvi = (ControlLVI)this;
        	List<TablaCurva> l = generarTablasControl(false, lvi.Frecuencia1.name()+"-"+lvi.Frecuencia2.name());
        	lvi.AumentarVelocidad();
        	l.addAll(generarTablasControl(true, lvi.Frecuencia1.name()+"-"+lvi.Frecuencia2.name()));
        	return l;
    	}
    	List<TablaCurva> l = generarTablasControl(false, null);
    	if (this instanceof ControlAumentable && ((ControlAumentable)this).Aumentable())
    	{
    		((ControlAumentable)this).AumentarVelocidad(true);
        	l.addAll(generarTablasControl(true, null));
    	}
    	if (this instanceof ControlLVIL1F1)
    	{
    		((ControlLVI)this).AumentarVelocidad();
        	l.addAll(generarTablasControl(true, null));
		}
    	return l;
    }
    List<TablaCurva> generarTablasControl(boolean aumento, String especial)
    {
    	List<TablaCurva> lista = new ArrayList<>();
    	ModoCurvas[] mod = {ModoCurvas.CONV, ModoCurvas.BAS_CONV, ModoCurvas.AV, ModoCurvas.BAS_AV, ModoCurvas.RAM, ModoCurvas.BAS_RAM, ModoCurvas.BTS, ModoCurvas.BTS_RAM};
    	for (ModoCurvas mc : mod) {
    		basico = mc == ModoCurvas.BAS_CONV || mc == ModoCurvas.BAS_AV || mc == ModoCurvas.BAS_RAM;
    		Modo = ASFA.Modo.CONV;
    		if (mc == ModoCurvas.AV || mc == ModoCurvas.BAS_AV) Modo = ASFA.Modo.AV;
    		if (mc == ModoCurvas.RAM || mc == ModoCurvas.BAS_RAM) Modo = ASFA.Modo.RAM;
    		if (mc == ModoCurvas.BTS || mc == ModoCurvas.BTS_RAM) Modo = ASFA.Modo.BTS;
    		modoRAM = mc == ModoCurvas.BTS_RAM || Modo == ASFA.Modo.RAM;
    		if ((this instanceof ControlFASF || this instanceof ControlSecuenciaAA || this instanceof ControlPasoDesvío) && (mc == ModoCurvas.BTS || mc == ModoCurvas.BTS_RAM)) continue;
    		if (this instanceof ControlLVI && !(this instanceof ControlLVIL1F1) && !(this instanceof ControlPreanuncioLTV) && aumento && modoRAM) continue;
        	TablaCurva c = construirTablaPorDefecto(new ArrayList<ModoCurvas>(Arrays.asList(new ModoCurvas[] {mc})), aumento, especial);
        	if (c == null) continue;
        	boolean exists = false;
        	for (TablaCurva t : lista) {
        		if (t.tabla.size() != c.tabla.size()) continue;
        		boolean eq = true;
        		for (Entry<Integer,Curva[]> e : t.tabla.entrySet()) {
        			if (!c.tabla.containsKey(e.getKey())) {eq = false;break;}
        			Curva[] c1 = e.getValue();
        			Curva[] c2 = c.tabla.get(e.getKey());
        			if (!c1[0].equals(c2[0]) || !c1[1].equals(c2[1])) {eq=false;break;}
        		}
        		if (eq)
        		{
        			exists = true;
        			t.Modos.add(mc);
        			break;
        		}
        	}
        	if (!exists) lista.add(c);
    	}
    	return lista;
    }
    public static void generarFicheroTablas()
    {
    	TrainParameters p = new TrainParameters();
    	try {
    		FileWriter file = new FileWriter("curvas.cfg");
			BufferedWriter bufferedWriter = new BufferedWriter(file);
			List<TablaCurva> tablas = new ArrayList<>();
			tablas.addAll(new ControlArranque(p).generarTablasControl());
			tablas.addAll(new ControlTransicion(p).generarTablasControl());
			tablas.addAll(new ControlViaLibre(p,0).generarTablasControl());
			tablas.addAll(new ControlViaLibreCondicional(0,p,false).generarTablasControl());
			tablas.addAll(new ControlAnuncioParada(0, p).generarTablasControl());
			tablas.addAll(new ControlAnuncioPrecaución(0, p).generarTablasControl());
			tablas.addAll(new ControlPreanuncioParada(0, p).generarTablasControl());
			tablas.addAll(new ControlPreviaSeñalParada(0, p).generarTablasControl());
			tablas.addAll(new ControlSeñalParada(p,0,0,false).generarTablasControl());
			tablas.addAll(new ControlZonaLimiteParada(p).generarTablasControl());
			tablas.addAll(new ControlSecuenciaAA(0,p).generarTablasControl());
			tablas.addAll(new ControlSecuenciaAN_A(0,p,false,true).generarTablasControl(false, "PRIMERA BALIZA"));
			tablas.addAll(new ControlSecuenciaAN_A(0,p,true,true).generarTablasControl(true, "PRIMERA BALIZA"));
			tablas.addAll(new ControlSecuenciaAN_A(0,p,false,false).generarTablasControl(false, "SEGUNDA BALIZA"));
			tablas.addAll(new ControlSecuenciaAN_A(0,p,true,false).generarTablasControl(true, "SEGUNDA BALIZA"));
			tablas.addAll(new ControlPasoDesvío(p,0,false).generarTablasControl(false, null));
			tablas.addAll(new ControlPasoDesvío(p,0,true).generarTablasControl(true, null));
			tablas.addAll(new ControlPreanuncioLTV(0,0,p).generarTablasControl());
			tablas.addAll(new ControlLVI(p, FrecASFA.L11, FrecASFA.L11, true, 0).generarTablasControl());
			tablas.addAll(new ControlLVI(p, FrecASFA.L11, FrecASFA.L10, true, 0).generarTablasControl());
			tablas.addAll(new ControlLVI(p, FrecASFA.L10, FrecASFA.L11, true, 0).generarTablasControl());
			tablas.addAll(new ControlLVI(p, FrecASFA.L10, FrecASFA.L10, true, 0).generarTablasControl());
			tablas.addAll(new ControlLVIL1F1(0,p).generarTablasControl());
			tablas.addAll(new ControlPNProtegido(p,0,0).generarTablasControl());
			tablas.addAll(new ControlPNDesprotegido(p,0,0).generarTablasControl());
			for (TablaCurva t : tablas) {
				t.ToFile(bufferedWriter);
			}
			bufferedWriter.flush();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    abstract Curva[] getCurvas_AESF(int T, int v);
    abstract Curva[] getCurvas_ADIF(int T);
    public void Curvas() {
		int Teff = T;
    	if (ASFA_version >= 3)
    	{
            int O = (int) Math.min(speed + 5, T);
            if (modoRAM) {
        		if (O <= 50) O = 50;
        		else if (O <= 60) O = 60;
        		else if (O <= 70) O = 70;
        		else if (O <= 80) O = 80;
        		else if (O <= 90) O = 90;
        		else if (O <= 100) O = 100;
        		else if (O <= 110) O = 110;
        		else O = 120;
            } else {
        		if (O <= 80) O = 80;
        		else if (O <= 90) O = 90;
        		else if (O <= 100) O = 100;
        		else if (O <= 120) O = 120;
        		else if (O <= 140) O = 140;
        		else if (O <= 160) O = 160;
        		else if (O <= 180) O = 180;
        		else O = 200;
            }
            if (O > T) O = T;
            if (this instanceof ControlViaLibreCondicional || this instanceof ControlPreanuncioParada ||
            		this instanceof ControlAnuncioParada || this instanceof ControlAnuncioPrecaución || 
            		this instanceof ControlPN || this instanceof ControlLVI || ASFA_version == 3) {
            	Teff = O;
            }
    	}
    	Curvas(Teff);
    }
    int escalonSuperior(int O)
    {
    	if (modoRAM) {
    		if (O == 50) O = 60;
    		else if (O == 60) O = 70;
    		else if (O == 70) O = 80;
    		else if (O == 80) O = 90;
    		else if (O == 90) O = 100;
    		else if (O == 100) O = 110;
    		else O = 120;
    	} else {
    		if (O == 80) O = 90;
    		else if (O == 90) O = 100;
    		else if (O == 100) O = 120;
    		else if (O == 120) O = 140;
    		else if (O == 140) O = 160;
    		else if (O == 160) O = 180;
    		else O = 200;
    	}
		return Math.min(O, T);
    }
    Curva[] getCurvas(int T)
    {
    	Curva[] curvas = null;
    	if (Config.UsarCurvasExternas) curvas = obtenerCurvasAlmacen(T);
    	if (curvas == null) curvas = ASFA_version >= 4 ? getCurvas_AESF(T, T) : getCurvas_ADIF(T);
    	return curvas;
    }
    public void Curvas(int Teff) {
    	Curva[] curvasT;
    	if(T==100 && curvasT120)
    	{
    		curvasT = getCurvas(120);
    		curvasT[1].OrdenadaOrigen = Math.min(curvasT[1].OrdenadaOrigen, 100 + (curvasT[1].OrdenadaOrigen - curvasT[0].OrdenadaOrigen));
    		curvasT[0].OrdenadaOrigen = Math.min(curvasT[0].OrdenadaOrigen, 100);
    	}
    	else
    	{
        	curvasT = getCurvas(T);
    	}
    	if (Teff < T)
    	{
    		Curva[] curvasTstar = getCurvas(Teff);
    		VC = curvasTstar[0];
    		IF = curvasTstar[1];
        	if (VC.OrdenadaFinal<curvasT[0].OrdenadaFinal)
        	{
        		Curvas(escalonSuperior(Teff));
        	}
    	}
    	else
    	{
    		VC = curvasT[0];
    		IF = curvasT[1];
    	}
    }
    public double getIF(double T) {
        return TiempoInicial != 0 ? IF.valor(T - TiempoInicial) : IF.OrdenadaFinal;
    }

    public double getVC(double T) {
        double v = TiempoInicial != 0 ? VC.valor(T - TiempoInicial) : VC.OrdenadaFinal;
        if (v == VC.OrdenadaFinal && TiempoVAlcanzada == 0) {
            TiempoVAlcanzada = T;
        }
        return v;
    }
    public boolean equals(Control c)
    {	
    	if (c == null) return false;
    	if (this != c) return false;
    	if (c instanceof ControlAumentable && this instanceof ControlAumentable
    			&& (((ControlAumentable) c).Aumentado() != ((ControlAumentable) this).Aumentado())) return false;
    	return true;
    }
}
