package dmi.Pantalla;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.StateSerializer;

import dmi.Pantalla.ÚltimaInfo.Info;
import ecp.Config;
import ecp.Main;
import ecp.ASFA.Modo;

public class PantallaSerializer extends StateSerializer {

	Pantalla pantalla;
	boolean ComunicacionECP;
	public PantallaSerializer(Pantalla pantalla)
	{
		super(Config.SerialPantalla);
		this.pantalla = pantalla;
		if (Config.SerialPantalla == null || Config.SerialPantalla == "") return;
		new Thread(() -> {
			while(true) update();
			}).start();
	}
	void setDisplay(byte[] data)
	{
		if (!pantalla.activa) return;
		pantalla.vreal.setValue((data[0]&255) + ((int)(data[1]&255)<<8));
    	pantalla.vtarget.set(data[3] > 2 ? data[3]-2 : data[3], data[2] & 255, data[3] > 2);
    	pantalla.info.setInfo(Info.values()[(data[4]&255)>>1], (data[4] & 1) != 0);
    	pantalla.controles.update(((data[5]>>4)&3) == 1 ? ((data[5]&64) != 0 ? 2 : 1) : 0, ((data[5]>>4)&3) == 2 ? ((data[5]&64) != 0 ? 2 : 1) : 0,
    			(data[6] & 3) == 1 ? 1 : 0, (data[6] & 3) == 2  ? 1 : 0, data[5] & 3);
    	if (data[7] == 0) pantalla.setModo(null);
    	else pantalla.setModo(Modo.values()[(data[7]&255)-1]);
    	if ((data[8]&31) > 25) pantalla.tipoTren.set(Modo.values()[(data[8]&31)-26].name());
    	else pantalla.tipoTren.set((int)(data[8]&31)*10);
    	if (pantalla.eficacia.fase2 != ((data[9]&4)!=0)) pantalla.eficacia.fase((data[9]&4)!=0);
    	pantalla.eficacia.set((data[9]&3)==2, (data[9]&3)!=0);
    	int urg = (data[9]>>3)&3;
    	pantalla.intervención.update(urg < 3 ? urg : 0, urg==3);
    	pantalla.velo.setActivo((data[9]&32) != 0);
	}
	void setConectada(byte[] data)
	{
		boolean conectada = (data[0]&1) != 0;
		boolean habilitada = (data[0]&2) != 0;
		//System.out.println(data[0]+" "+ComunicacionECP+" "+pantalla.activa+" "+pantalla.conectada);
		if (conectada != Main.dmi.activo)
		{
			Main.dmi.activo = conectada;
			if (!conectada)
			{
				ComunicacionECP = false;
				pantalla.apagar();
	    		if (Config.ApagarOrdenador)
	    		{
	    			try {
						Runtime.getRuntime().exec("shutdown -h now");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
			}
			else
			{
				pantalla.encender();
			}
		}
		pantalla.habilitar(habilitada);
	}
	void setEstadoECP(byte[] data)
	{
		if (ComunicacionECP) return;
		ComunicacionECP = true;
		int state = data[3] & 0xFF;
		pantalla.setup(state, new String(data, 4, data.length-5, StandardCharsets.UTF_8));
	}
	void update()
	{
		if (pantalla.conectada)
		{
			write(new Paquete(TipoPaquete.FeedbackDisplay, new byte[] {(byte) (pantalla.activa ? 1 : 0)}));
			if (!ComunicacionECP) write(new Paquete(TipoPaquete.PeticionEstado, new byte[] {0}));
		}
		try {
			synchronized(this)
			{
				wait(500);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void parse(Paquete paquete) {
		switch(paquete.tipo)
		{
			case IconosDisplay:
				setDisplay(paquete.data);
				break;
			case ConexionCabina:
				setConectada(paquete.data);
				break;
			case EstadoECP:
				setEstadoECP(paquete.data);
				break;
			default:
				break;
		}
	}

}
