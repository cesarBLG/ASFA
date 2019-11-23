package ecp;

import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DIV {

    byte[] data = new byte[64];

    public byte[] getData() {
        Timer t = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                synchronized (DIV.this) {
                    DIV.this.notify();
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
        }
        /*if(dataCount == 64) */
        data[38] = (byte) 140;
        data[14] = 16 | 32;
        data[16] = 1;
        data[18] = (byte) 180; 
        data[35] = 100;
        data[36] = 60;
        data[37] = 50;
        return data;
        //else return null;
    }
    int dataCount = 0;

    public void add(byte b) {
        data[dataCount++] = b;
        if (dataCount == 64) {
            synchronized (this) {
                notify();
            }
        }
    }
}
