package ecp;

import javax.swing.Timer;

import com.fazecast.jSerialComm.SerialPort;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

public class DIV {
	
    byte[] data = new byte[64];
    
    public DIV()
    {
    	if (Config.SerialDIV != null && !Config.SerialDIV.isEmpty()) new Thread(() -> leerDIVserial()).start();
    }
    
    public void leerDIVserial()
	{
    	try {
			SerialPort sp = SerialPort.getCommPort(Config.SerialDIV);
			sp.setBaudRate(9600);
			sp.setParity(SerialPort.EVEN_PARITY);
			sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
			sp.setDTR();
			sp.openPort();
			InputStream in = sp.getInputStream();
			in.skip(in.available());
			int timesRead = 0;
			double startTime = Clock.getSeconds();
			while (sp.isOpen())
			{
				if (sp.bytesAvailable() > 0)
				{
					byte[] data = new byte[64];
					int read = sp.readBytes(data, 64);
					if (read == 64)
					{
						setData(data, 0);
						timesRead++;
					}
				}
				if (timesRead>=3 || Clock.getSeconds()-startTime > 50) break;
				Thread.sleep(500);
			}
			sp.closePort();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public byte[] getData() {
    	synchronized(data)
    	{
    		if(dataCount == 64)
            {
            	//dataCount = 0;
            	return data;
            }
            else
            {
            	//dataCount = 0;
            	return null;
            }
    	}
    }
    int dataCount = 0;
    
    public void setData(byte[] data, int offset) {
    	if (data.length < 64) return;
    	synchronized(this.data)
    	{
    		System.arraycopy(data, offset, this.data, 0, 64);
    		dataCount = 64;
    	}
    }
}
