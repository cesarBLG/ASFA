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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class TCP {
	List<ClientListener> clientListeners = new ArrayList<>();
	ServerSocket server;
	public void initialize()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					server = new ServerSocket(5000);
					while(true)
					{
						ClientListener c = new ClientListener(server.accept());
						clientListeners.add(c);
					}
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	public void free()
	{
		try
		{
			for(ClientListener c : clientListeners)
			{
				c.socket.close();
			}
			server.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void write(byte[] b)
	{
		for(ClientListener c : clientListeners)
		{
			c.write(b);
		}
	}
}
