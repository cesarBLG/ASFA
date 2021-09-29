package dmi.Pantalla;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import dmi.Pantalla.Pantalla.ModoDisplay;
import ecp.ASFA.Modo;

public class PantallaGPIO {
	Pantalla pantalla;
	List<Socket> sockets = new ArrayList<>();
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
					sockets.add(s);
					new Thread(() -> {
						try {
							while(true) {
								dataReceived((byte) s.getInputStream().read());
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sockets.remove(s);
					}).start();
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
			pantalla.habilitar(b == '1');
		}
	}
	void write(byte b)
	{
		sockets.forEach((s) -> {
			try {
				s.getOutputStream().write(b);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
}
