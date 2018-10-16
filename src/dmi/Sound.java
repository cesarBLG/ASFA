package dmi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import ecp.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sound implements Runnable {

    final List<Sonido> activeSounds = new ArrayList<>();
    Sonido activo = null;

    public Sound()
    {
        new Thread(this).start();
    }

    public void Trigger(String s) {
        synchronized(activeSounds)
        {
        	if(activeSounds.contains(s)) return;
            activeSounds.add(new Sonido(s));
            activeSounds.notify();
        }
    }

    public void Stop(String s) {
        synchronized(activeSounds)
        {
            activeSounds.removeIf(x -> x.name.equals(s));
            activeSounds.notify();
        }
    }
    LineListener getListener()
    {
    	return new LineListener() {

            @Override
                public void update(LineEvent arg0) {
                    // TODO Auto-generated method stub
                    synchronized(activeSounds)
                    {
                        if(arg0.getType()==LineEvent.Type.STOP&&activo!=null&&!activo.loop)
                        {
                            activeSounds.remove(activo);
                            activeSounds.notify();
                        }        
                    }    
                }
                @Override
                public boolean equals(Object obj)
                {
                    return obj instanceof LineListener;
                }
        };
    }
    public Clip getClip()
    {
        Clip clip = null;
        try {
            clip = AudioSystem.getClip(null);
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        clip.addLineListener(getListener());
        return clip;
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
    	//if(true && true) return;
        SourceDataLine source;
        try {
        	AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource("/content/Sonido/S0.wav"));
			source = (SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, ais.getFormat()));
			source.open(ais.getFormat());
	        source.start();
	        Thread.sleep(10000);
	        source.drain();
	        source.close();
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        /*while(true)
        {
            synchronized(activeSounds)
            {
                if(clip!=null)
                {
                    clip.removeLineListener(getListener());
                	clip.close();
                    //if(clip.isRunning()) clip.stop();
                    if(clip.isOpen()) clip.close();
                }
                if(!activeSounds.isEmpty())
                {
                    clip = getClip();
                    activo = activeSounds.get(0);
                    try {
                        clip.open(AudioSystem.getAudioInputStream(activo.file));
                    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                        e.printStackTrace();
                    }
                    if (activo.loop) {
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    } else {
                        clip.setMicrosecondPosition((long) ((Clock.getSeconds() - activo.startTime) * 1000000f));
                        clip.start();
                    } 
                }
                try {
                    activeSounds.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }
}

class Sonido {

    public String name;
    public File file;
    public boolean loop;
    public double startTime = 0;

    public Sonido(String name) {
        this.name = name;
        file = new File("src/content/Sonido/" + name + ".wav");
        loop = name == "S3-1" || name == "S3-2" || name == "S5";
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
