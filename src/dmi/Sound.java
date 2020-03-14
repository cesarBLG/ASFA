package dmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ecp.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sound implements Runnable {

	public Queue<Runnable> tasks = new LinkedList<>();
	
	Sonido activo = null;

    public Sound()
    {
    	SwingUtilities.invokeLater(() -> new Thread(this).start());
    }
    public void Trigger(String s) {Trigger(s, false);}
    public void Trigger(String s, boolean basico) {
    	if(activo!=null && activo.equals(s) && activo.basico == basico) return;
    	Runnable r = () -> {
            clip.stop();
        	clip.flush();
        	clip.close();
        	Sonido son = new Sonido(s, basico);
        	activo = son;
        	try {
				clip.open(son.stream);
			} catch (LineUnavailableException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(son.loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
			else clip.start();
    	};
    	synchronized(tasks)
    	{
    		tasks.add(r);
    		tasks.notify();
    	}
        /*synchronized(activeSounds)
        {
        	if(activeSounds.contains(s)) return;
            activeSounds.add(new Sonido(s));
            activeSounds.notify();
        }*/
    }

    public void Stop(String s) {
    	if(activo==null || !activo.equals(s)) return;
    	activo = null;
    	synchronized(tasks)
    	{
    		tasks.add(() -> {clip.stop(); clip.flush();});
    		tasks.notify();
    	}
    }
    Clip clip = null;
    @Override
    public void run() {
    	try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	try {
			Runtime.getRuntime().exec("./sound");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	/*return;
    	try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	while(true)
		{
			Runnable r = null;
			synchronized(tasks)
			{
				if(tasks.isEmpty())
				{
					try
					{
						tasks.wait();
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else r = tasks.poll();
			}
			if(r!=null) r.run();
		}*/
    }
}

class Sonido {

    public String name;
    public static Hashtable<String, AudioInputStream> streams = new Hashtable<>();
    public boolean loop;
    public double startTime = 0;
    public AudioInputStream stream;
    public boolean basico;

    public Sonido(String name, boolean basico) {
        this.name = name;
        this.basico = basico;
        if(!streams.contains(name))
        {
			try {
				streams.put(name, AudioSystem.getAudioInputStream(getClass().getResource("/content/Sonido/" + (basico ? "/Basico/" : "") + name + ".wav")));
			} catch (UnsupportedAudioFileException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        stream = streams.get(name);
        loop = name == "S3-1" || name == "S3-2" || name == "S3-4" || name == "S3-5" || name == "S5";
        startTime = Clock.getSeconds();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Sonido) {
            return name == ((Sonido) obj).name;
        }
        if (obj instanceof String) {
            return name == (String) obj;
        }
        return super.equals(obj);
    }
}
