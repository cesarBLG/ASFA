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
    	int crc_exp = (((data[offset+62]&0xFF)<<8) | (data[offset+63]&0xFF))&0xFFFF;
    	if (Crc16(data, offset, 62) != crc_exp) return;
    	synchronized(this.data)
    	{
    		System.arraycopy(data, offset, this.data, 0, 64);
    		dataCount = 64;
    	}
    }
    public void setDataAssumeValid(byte[] data)
    {
    	if (data.length < 64) return;
    	int crc32 = Crc32(data, 0, 58);
    	data[58] = (byte) (crc32>>>24);
    	data[59] = (byte) (crc32>>>16);
    	data[60] = (byte) (crc32>>>8);
    	data[61] = (byte) (crc32);
    	int crc16 = Crc16(data, 0, 62);
    	data[62] = (byte) (crc16>>>8);
    	data[63] = (byte) (crc16);
    	synchronized(this.data)
    	{
    		System.arraycopy(data, 0, this.data, 0, 64);
    		dataCount = 64;
    	}
    }
    public boolean isValid()
    {
    	int crc_exp = (((data[58]&0xFF)<<24) | ((data[59]&0xFF)<<16) | ((data[60]&0xFF)<<8) | (data[61]&0xFF)) & 0xFFFFFFFF;
    	return data!=null && dataCount == 64 && Crc32(data, 0, 58) == crc_exp;
    }
	int Crc16(byte[] data, int offset, int count)
	{
		int crc = 0xFFFF;
		for (int i = offset; i < offset + count; i++)
		{
		    crc = crc ^ (data[i] << 8);
		    for (int j = 0; j < 8; j++)
		    {
		        if ((crc & 0x8000) != 0)
		            crc = (crc << 1) ^ 0x1021;
		        else
		            crc = crc << 1;
		    }
		}
		return crc&0xFFFF;
	}
	int[] _lookupTable = {0x0,0x4a503df1,0x94a07be2,0xdef04613,0x6310ca35,0x2940f7c4,0xf7b0b1d7,0xbde08c26,0xc621946a,0x8c71a99b,0x5281ef88,0x18d1d279,0xa5315e5f,0xef6163ae,0x319125bd,0x7bc1184c,0xc6131525,0x8c4328d4,0x52b36ec7,0x18e35336,0xa503df10,0xef53e2e1,0x31a3a4f2,0x7bf39903,0x32814f,0x4a62bcbe,0x9492faad,0xdec2c75c,0x63224b7a,0x2972768b,0xf7823098,0xbdd20d69,0xc67617bb,0x8c262a4a,0x52d66c59,0x188651a8,0xa566dd8e,0xef36e07f,0x31c6a66c,0x7b969b9d,0x5783d1,0x4a07be20,0x94f7f833,0xdea7c5c2,0x634749e4,0x29177415,0xf7e73206,0xbdb70ff7,0x65029e,0x4a353f6f,0x94c5797c,0xde95448d,0x6375c8ab,0x2925f55a,0xf7d5b349,0xbd858eb8,0xc64496f4,0x8c14ab05,0x52e4ed16,0x18b4d0e7,0xa5545cc1,0xef046130,0x31f42723,0x7ba41ad2,0xc6bc1287,0x8cec2f76,0x521c6965,0x184c5494,0xa5acd8b2,0xeffce543,0x310ca350,0x7b5c9ea1,0x9d86ed,0x4acdbb1c,0x943dfd0f,0xde6dc0fe,0x638d4cd8,0x29dd7129,0xf72d373a,0xbd7d0acb,0xaf07a2,0x4aff3a53,0x940f7c40,0xde5f41b1,0x63bfcd97,0x29eff066,0xf71fb675,0xbd4f8b84,0xc68e93c8,0x8cdeae39,0x522ee82a,0x187ed5db,0xa59e59fd,0xefce640c,0x313e221f,0x7b6e1fee,0xca053c,0x4a9a38cd,0x946a7ede,0xde3a432f,0x63dacf09,0x298af2f8,0xf77ab4eb,0xbd2a891a,0xc6eb9156,0x8cbbaca7,0x524beab4,0x181bd745,0xa5fb5b63,0xefab6692,0x315b2081,0x7b0b1d70,0xc6d91019,0x8c892de8,0x52796bfb,0x1829560a,0xa5c9da2c,0xef99e7dd,0x3169a1ce,0x7b399c3f,0xf88473,0x4aa8b982,0x9458ff91,0xde08c260,0x63e84e46,0x29b873b7,0xf74835a4,0xbd180855,0xc72818ff,0x8d78250e,0x5388631d,0x19d85eec,0xa438d2ca,0xee68ef3b,0x3098a928,0x7ac894d9,0x1098c95,0x4b59b164,0x95a9f777,0xdff9ca86,0x621946a0,0x28497b51,0xf6b93d42,0xbce900b3,0x13b0dda,0x4b6b302b,0x959b7638,0xdfcb4bc9,0x622bc7ef,0x287bfa1e,0xf68bbc0d,0xbcdb81fc,0xc71a99b0,0x8d4aa441,0x53bae252,0x19eadfa3,0xa40a5385,0xee5a6e74,0x30aa2867,0x7afa1596,0x15e0f44,0x4b0e32b5,0x95fe74a6,0xdfae4957,0x624ec571,0x281ef880,0xf6eebe93,0xbcbe8362,0xc77f9b2e,0x8d2fa6df,0x53dfe0cc,0x198fdd3d,0xa46f511b,0xee3f6cea,0x30cf2af9,0x7a9f1708,0xc74d1a61,0x8d1d2790,0x53ed6183,0x19bd5c72,0xa45dd054,0xee0deda5,0x30fdabb6,0x7aad9647,0x16c8e0b,0x4b3cb3fa,0x95ccf5e9,0xdf9cc818,0x627c443e,0x282c79cf,0xf6dc3fdc,0xbc8c022d,0x1940a78,0x4bc43789,0x9534719a,0xdf644c6b,0x6284c04d,0x28d4fdbc,0xf624bbaf,0xbc74865e,0xc7b59e12,0x8de5a3e3,0x5315e5f0,0x1945d801,0xa4a55427,0xeef569d6,0x30052fc5,0x7a551234,0xc7871f5d,0x8dd722ac,0x532764bf,0x1977594e,0xa497d568,0xeec7e899,0x3037ae8a,0x7a67937b,0x1a68b37,0x4bf6b6c6,0x9506f0d5,0xdf56cd24,0x62b64102,0x28e67cf3,0xf6163ae0,0xbc460711,0xc7e21dc3,0x8db22032,0x53426621,0x19125bd0,0xa4f2d7f6,0xeea2ea07,0x3052ac14,0x7a0291e5,0x1c389a9,0x4b93b458,0x9563f24b,0xdf33cfba,0x62d3439c,0x28837e6d,0xf673387e,0xbc23058f,0x1f108e6,0x4ba13517,0x95517304,0xdf014ef5,0x62e1c2d3,0x28b1ff22,0xf641b931,0xbc1184c0,0xc7d09c8c,0x8d80a17d,0x5370e76e,0x1920da9f,0xa4c056b9,0xee906b48,0x30602d5b,0x7a3010aa};
	int Crc32(byte[] data, int offset, int count)
	{
	  int crc = 0;
	  for (int i=offset; i<offset+count; i++)
	  {
	    crc = _lookupTable[((crc >>> 24) ^ data[i]) & 0xFF] ^ (crc << 8);
	  }
	  return crc & 0xFFFFFFFF;
	}
}
