package dmi.Pantalla;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import dmi.Pantalla.Pantalla.ModoDisplay;
import ecp.ASFA.Modo;

public class PantallaGPIO {
	Pantalla pantalla;
	public PantallaGPIO(Pantalla pantalla)
	{
		this.pantalla = pantalla;
		new Thread(() ->  {
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(5092);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (ss.isBound())
			{
				try {
					Socket s = ss.accept();
					while(true)
					{
						dataReceived((byte) s.getInputStream().read());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	void dataReceived(byte b)
	{
		if (b == 'd' || b == 'n')
		{
			if (pantalla.modo_asfa != Modo.EXT) pantalla.set(b == 'd' ? ModoDisplay.Día : ModoDisplay.Noche);
		}
		if (b == 's')
		{
			pantalla.set();
		}
		if (b == '0' || b == '1')
		{
			boolean activa = b=='1';
			if (activa != pantalla.activa)
			{
				pantalla.activa = activa;
				if (pantalla.conectada)
				{
					if (activa) pantalla.start();
					else pantalla.stop();
				}
			}
			
		}
	}
}
