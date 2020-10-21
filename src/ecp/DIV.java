package ecp;

import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DIV {

    byte[] data = new byte[64];

    public byte[] getData() {
        /*Timer t = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                synchronized (DIV.this) {
                    DIV.this.notifyAll();
                }
            }

        });
        t.setRepeats(false);
        t.start();
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        if(dataCount == 64)
        {
        	dataCount = 0;
        	return data;
        }
        else
        {
        	dataCount = 0;
        	return null;
        }
    }
    int dataCount = 0;

    public void add(byte b) {
        if(dataCount<64) data[dataCount++] = b;
        /*if (dataCount == 64) {
            synchronized (this) {
                notifyAll();
            }
        }*/
    }
}
