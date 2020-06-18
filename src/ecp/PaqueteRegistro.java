package ecp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import dmi.Botones.Botón.TipoBotón;
import ecp.Controles.Control;
import ecp.Controles.ControlAnuncioParada;
import ecp.Controles.ControlAnuncioPrecaución;
import ecp.Controles.ControlArranque;
import ecp.Controles.ControlAumentable;
import ecp.Controles.ControlBTS;
import ecp.Controles.ControlLVI;
import ecp.Controles.ControlManiobras;
import ecp.Controles.ControlPNDesprotegido;
import ecp.Controles.ControlPasoDesvío;
import ecp.Controles.ControlPreanuncioParada;
import ecp.Controles.ControlPreviaSeñalParada;
import ecp.Controles.ControlSecuenciaAA;
import ecp.Controles.ControlSecuenciaAN_A;
import ecp.Controles.ControlSeñalParada;
import ecp.Controles.ControlTransicion;
import ecp.Controles.ControlViaLibre;
import ecp.Controles.ControlViaLibreCondicional;
import ecp.Controles.ControlZonaLimiteParada;

public class PaqueteRegistro
{
	int Codigo;
	int Fecha;
	int Milisegundos;
	double Distancia;
	int Valor;
	int Vreal;
	int Vcontrol;
	int Vif;
	static LinkedList<PaqueteRegistro> paquetes = new LinkedList<PaqueteRegistro>();
	PaqueteRegistro(int codigo, int valor)
	{
		Codigo = codigo;
		Valor = valor;
		Fecha = (int) Clock.getSeconds();
		Milisegundos = (int)(((long)(Clock.getSeconds()*1000))%1000);
		Distancia = (int)Odometer.getDistance();
		Vreal = (int)(Odometer.getSpeed()*3.6);
		if(Main.ASFA.ControlActivo != null)
		{	
			Vcontrol = (int)(Main.ASFA.ControlActivo.getVC(Clock.getSeconds()));
			Vif = (int)(Main.ASFA.ControlActivo.getIF(Clock.getSeconds()));
		}
	}
	static FileOutputStream writer = null;
	static BufferedWriter excel = null;
	static Hashtable<Integer,String> codigos = new Hashtable<>();
	static String format_value(int codigo, int valor)
	{
		if(codigo == 0xFF02)
		{
			Date date = new Date(valor*1000L);
	        DateFormat d = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	        return d.format(date);
		}
		if(codigo == 0xFF06)
		{
			//TODO: return BCD
		}
		
		return Integer.toString(valor);
	}
	static void add(int codigo, int valor)
	{
		PaqueteRegistro p = new PaqueteRegistro(codigo, valor);
		if (writer == null)
		{
			codigos.put(0xFF02,"Cambio fecha/hora");
			codigos.put(0xFF03,"Encendido");
			codigos.put(0xFF04,"Apagado");
			codigos.put(0xFF05,"Frecuencia");
			codigos.put(0xFF06,"Codigo fallo");
			codigos.put(0xFF10,"Velocidad real");
			codigos.put(0xFF11,"Velocidad de control inicial");
			codigos.put(0xFF12,"Velocidad de control final");
			codigos.put(0xFF13,"Tipo de tren");
			codigos.put(0xFF14,"Modo de funcionamiento");
			codigos.put(0xFF20,"Freno de emergencia");
			codigos.put(0xFF42,"Ocultación cab 1");
			codigos.put(0xFF43,"Ocultación cab 2");
			codigos.put(0xFF50,"Interruptor ASFA básico cab 1");
			codigos.put(0xFF51,"Interruptor ASFA básico cab 2");
			codigos.put(0xFF52,"Pulsador anuncio parada cab 1");
			codigos.put(0xFF53,"Pulsador anuncio parada cab 2");
			codigos.put(0xFF54,"Pulsador anuncio precaución cab 1");
			codigos.put(0xFF55,"Pulsador anuncio precaución cab 2");
			codigos.put(0xFF56,"Pulsador preanuncio/VL cond cab 1");
			codigos.put(0xFF57,"Pulsador preanuncio/VL cond cab 2");
			codigos.put(0xFF58,"Pulsador aumento velocidad cab 1");
			codigos.put(0xFF59,"Pulsador aumento velocidad cab 2");
			codigos.put(0xFF5A,"Pulsador modo cab 1");
			codigos.put(0xFF5B,"Pulsador modo cab 2");
			codigos.put(0xFF5C,"Pulsador reconocimiento PN cab 1");
			codigos.put(0xFF5D,"Pulsador reconocimiento PN cab 2");
			codigos.put(0xFF5E,"Pulsador rebase autorizado cab 1");
			codigos.put(0xFF5F,"Pulsador rebase autorizado cab 2");
			codigos.put(0xFF60,"Pulsador rearme freno cab 1");
			codigos.put(0xFF61,"Pulsador rearme freno cab 2");
			codigos.put(0xFF62,"Pulsador alarma cab 1");
			codigos.put(0xFF63,"Pulsador alarma cab 2");
			codigos.put(0xFF64,"Pulsador ocultación cab 1");
			codigos.put(0xFF65,"Pulsador ocultación cab 2");
			codigos.put(0xFF66,"Pulsador reconocimiento LVI cab 1");
			codigos.put(0xFF67,"Pulsador reconocimiento LVI cab 2");
			codigos.put(0xFF68,"Pulsador conexión cab 1");
			codigos.put(0xFF69,"Pulsador conexión cab 2");
			codigos.put(0xFFF0,"Baliza recibida");
			codigos.put(0xFFF1,"Control activo");
			codigos.put(0xFFF2,"Indicación acústica");
			try {
				File f = new File("registro.cls");
				FileWriter f2 = new FileWriter("registro.csv");
				writer = new FileOutputStream(f);
				excel = new BufferedWriter(f2);
				excel.write("NP;FECHA;HORA;VARIABLE;VALOR;VEL.REAL;VEL.CONT;VEL.IF;DISTANCIA");
				excel.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ByteBuffer b = ByteBuffer.allocate(30);
		b.putShort((short) 0xDCBA);
		b.putInt(paquetes.size()+1);
		b.putShort((short)p.Codigo);
		b.putInt(p.Fecha);
		b.putShort((short)p.Milisegundos);
		b.putInt(paquetes.isEmpty() ? 0 : (int)(p.Distancia-paquetes.getLast().Distancia));
		b.putInt(p.Valor);
		b.putShort((short)p.Vreal);
		b.putShort((short)p.Vcontrol);
		b.putShort((short)p.Vif);
		short checksum=0;
		for(int i=4; i<28; i++)
		{
			checksum += b.get(i);
		}
		b.putShort(checksum);
		try {
			writer.write(b.array());
			excel.write(Integer.toString(paquetes.size()+1));
			excel.write(";");
			Date date = new Date(((long)p.Fecha*1000L)+(long)p.Milisegundos);
	        DateFormat d = new SimpleDateFormat("dd/MM/yyyy");
	        DateFormat t = new SimpleDateFormat("HH:mm:ss.SSS");
			excel.write(d.format(date));
			excel.write(";");
			excel.write(t.format(date));
			excel.write(";");
			excel.write(codigos.get(p.Codigo));
			excel.write(";");
			excel.write(format_value(p.Codigo, p.Valor));
			excel.write(";");
			excel.write(Integer.toString(p.Vreal));
			excel.write(";");
			excel.write(Integer.toString(p.Vcontrol));
			excel.write(";");
			excel.write(Integer.toString(p.Vif));
			excel.write(";");
			excel.write(Integer.toString(paquetes.isEmpty() ? 0 : (int)(p.Distancia-paquetes.getLast().Distancia)));
			excel.write(" m;");
			excel.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		paquetes.add(p);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				try {
					writer.close();
					excel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	static void cambio_hora(double horantigua)
	{
		add(0xFF02, (int)horantigua);
	}
	static void encendido()
	{
		add(0xFF03, 1);
	}
	static void apagado()
	{
		add(0xFF04, 1);
	}
	static void frecuencia(FrecASFA freq)
	{
		add(0xFF05, freq.ordinal());//TODO frecuencia/500Hz
	}
	static void codigo_fallo(int codigo)
	{
		add(0xFF06, codigo);
	}
	static void cambio_vreal()
	{
		add(0xFF10, (int)(Odometer.getSpeed()*3.6));
	}
	static void inicio_vcontrol()
	{
		add(0xFF11, (int)(Main.ASFA.ControlActivo.VC.OrdenadaOrigen));
	}
	static void fin_vcontrol()
	{
		add(0xFF12, (int)(Main.ASFA.ControlActivo.VC.OrdenadaFinal));
	}
	static void tipo_tren()
	{
		add(0xFF13, Main.ASFA.selectorT);
	}
	static void modo()
	{
		int modo = 0;
		boolean b = Main.ASFA.basico;
		switch(Main.ASFA.modo)
		{
			case CONV:
				if(b) modo=5;
				else modo=1;
				break;
			case AV:
				if(b) modo=6;
				else modo=2;
				break;
			case BTS:
				modo=3;
				break;
			case MBRA:
				modo=4;
				break;
			case EXT:
				modo=7;
				break;
			case MTO:
				modo=8;
				break;
			case RAM:
				if(b) modo=10;
				else modo=9;
				break;
			default:
				modo=0;
				break;
		}
		add(0xFF14, modo);
	}
	static void estado_urgencia()
	{
		add(0xFF20, Main.ASFA.FE ? 1 : 0);
	}
	static void baliza_recibida(FrecASFA freq)
	{
		add(0xFFF0, freq.ordinal());
	}
	static void control_activo()
	{
		int num=18;
		Control c = Main.ASFA.ControlActivo;
		if (c instanceof ControlArranque) num = 0;
		else if (c instanceof ControlTransicion) num = 1;
		else if (c instanceof ControlViaLibre) num = 2;
		else if (c instanceof ControlViaLibreCondicional) num = 3;
		else if (c instanceof ControlAnuncioParada) num = 4;
		else if (c instanceof ControlPreanuncioParada) num = 5;
		else if (c instanceof ControlSecuenciaAN_A) num = 6;
		else if (c instanceof ControlPreviaSeñalParada) num = 7;
		else if (c instanceof ControlSeñalParada)
		{
			if (((ControlSeñalParada)c).conRebase) num = 9;
			else num = 8;
		}
		else if (c instanceof ControlSecuenciaAA) num = 10;
		else if (c instanceof ControlAnuncioPrecaución) num = 11;
		else if (c instanceof ControlPNDesprotegido) num = 12;
		else if (c instanceof ControlZonaLimiteParada) num = 14;
		else if (c instanceof ControlPasoDesvío) num = 15;
		else if (c instanceof ControlLVI) num = 16;
		else if (c instanceof ControlBTS) num = 30;
		else if (c instanceof ControlManiobras) num = 31;
		//else if (c instanceof ControlCambioSeñalizacion) num = 17; TODO
		if (c instanceof ControlAumentable && ((ControlAumentable) c).Aumentado()) num += 32;
		else if (c instanceof ControlLVI && ((ControlLVI)c).AumentoVelocidad) num += 32;
		add(0xFFF1, num);
	}
	static void pulsador(TipoBotón pulsador, int display, boolean pulsado)
	{
		int num=0;
		switch(pulsador)
		{
			case AnPar:
				num = 0xFF52;
				break;
			case AnPre:
				num = 0xFF54;
				break;
			case PrePar:
			case VLCond:
				num = 0xFF56;
				break;
			case AumVel:
				num = 0xFF58;
				break;
			case Modo:
				num = 0xFF5A;
				break;
			case PN:
				num = 0xFF5C;
				break;
			case Rebase:
				num = 0xFF5E;
				break;
			case Rearme:
				num = 0xFF60;
				break;
			case Alarma:
				num = 0xFF62;
				break;
			case Ocultación:
				num = 0xFF64;
				break;
			case LVI:
				num = 0xFF66;
				break;
			case Conex:
				num = 0xFF68;
				break;
			case ASFA_básico:
				num = 0xFF50;
				break;
		}
		if(display == 2) num++;
		add(num, pulsado ? 1 : 0);
	}
	static void sonido(String snd, boolean basic)
	{
		int num=0;
		if (snd.equals("S1-1")) num = 1;
		else if (snd.equals("S2-1")) num = 2;
		else if (snd.equals("S2-2")) num = 3;
		else if (snd.equals("S2-3")) num = 4;
		else if (snd.equals("S2-4")) num = 5;
		else if (snd.equals("S2-5")) num = 6;
		else if (snd.equals("S2-6")) num = 7;
		else if (snd.equals("S3-1")) num = 8;
		else if (snd.equals("S3-2")) num = 9;
		else if (snd.equals("S3-3")) num = 10;
		else if (snd.equals("S3-4")) num = 11;
		else if (snd.equals("S3-5")) num = 12;
		else if (snd.equals("S4")) num = 14;
		else if (snd.equals("S5")) num = 15;
		else if (snd.equals("S6")) num = 16;
		if (basic) num += 32;
		add(0xFFF2, num);
	}
	static void ocultacion(int display, boolean activo)
	{
		add(display == 2 ? 0xFF43 : 0xFF42, activo ? 1 : 0);
	}
}
