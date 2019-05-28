/*******************************************************************************
 * Copyright (C) 2017-2018 César Benito Lamata
 * 
 * This file is part of SCRT.
 * 
 * SCRT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SCRT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SCRT.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientListener
{
	public Socket socket;
	static int num = 0;
	int id;
	DataInputStream in;
	DataOutputStream out;
	boolean debug = false;
	public ClientListener(Socket socket)
	{
		id = num++;
		this.socket = socket;
		try
		{
			debug = socket.getInetAddress().equals(InetAddress.getByName("192.168.2.3"));
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					while(!socket.isClosed())
					{
						byte[] data = new byte[3];
		                in.read(data, 0, 3);
		                COM.parse(data);
					}
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}).start();
	}
	public void write(int data)
	{
		try
		{
			if(debug) out.writeBytes(Integer.toString(data & 0xFF) + " ");
			else out.writeByte(data);
		} 
		catch (IOException e)
		{
			//e.printStackTrace();
		}
	}
	public void write(byte[] b)
	{
		try
		{
			if(debug)
			{
				for(byte a : b)
				{
					out.writeBytes(Integer.toString(a & 0xFF) + " ");
				}
			}
			else out.write(b);
		} 
		catch (IOException e)
		{
			//e.printStackTrace();
		}
	}
}
