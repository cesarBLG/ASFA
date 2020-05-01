package ecp;

import java.util.LinkedList;
import java.util.Queue;

public class Captador {
    
    //public Queue<FrecASFA> frecs = new LinkedList<>();
    
	FrecASFA captador;
	
    public void nuevaFrecuencia(FrecASFA f)
    {
    	captador = f;
    	//frecs.add(f);
    }
    
    public double lastSent=0;
    
    public FrecASFA getData() {
    	/*if (frecs.isEmpty())
    		return FrecASFA.FP;
    	for (int i=0; i<frecs.size(); i++) {
    		FrecASFA f = (FrecASFA) frecs.toArray()[i];
    		if (f!=FrecASFA.FP)
    		{
    			System.out.print(f);
    			System.out.print(" ");
    			System.out.println(Clock.getSeconds()-lastSent);
    		}
    	}
    	if (lastSent == 0)
    		lastSent = Clock.getSeconds();
    	if (frecs.size() > 1)
    	{
    		if (lastSent != 0 && lastSent + 0.01 < Clock.getSeconds())
    		{
    			lastSent = 0;
    			return frecs.poll();
    		}
    	}
        return frecs.peek();*/
    	return captador;
    }
}
