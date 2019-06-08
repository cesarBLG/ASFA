package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import ecp.Clock;
import ecp.FrecASFA;
import ecp.Odometer;

public class OR_Client {
	Socket s;
	OutputStream out;
	BufferedReader in;
	public OR_Client()
	{
		new Thread(() -> {
			while(s==null)
			{
				try
				{
					s = new Socket("localhost", 5090);
				}
				catch (IOException e)
				{
				}
			}
			while(!s.isConnected()) 
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = s.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendData("register(asfa_baliza)");
			sendData("register(speed)");
			while(true)
			{
				String s = readData();
				if(s==null) return;
				if(s.startsWith("asfa_baliza="))
				{
					String freq = s.substring(12);
					FrecASFA f = FrecASFA.valueOf(freq);
					COM.parse(8, f.ordinal());
				}
				if(s.startsWith("speed="))
				{
					String vel = s.substring(6);
					Odometer.speed = (float) Float.parseFloat(vel.replace(',', '.')) / 3.6;
				}
			}
		}).start();
	}
	void sendData(String s)
	{
		if(out==null) return;
		s = s+'\n';
		char[] c = s.toCharArray();
		try {
			for(int i=0; i<c.length; i++) {
				out.write(c[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	String readData()
	{
		try {
			return in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void send(int funct, int val)
	{
		if(funct == 4 && val<2) sendData("asfa_emergency(" + (val==1 ? '1' : '0') + ")");
        if (funct == 3 && (val & 1) != 1) sendData("asfa_target_speed("+(((int) (val >> 1) & 0xFF) * 5)+")");
	}
}
