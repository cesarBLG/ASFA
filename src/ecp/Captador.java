package ecp;

import java.util.LinkedList;
import java.util.Queue;

public class Captador {
    
    public Queue<FrecASFA> frecs = new LinkedList<>();
    
	//FrecASFA captador = FrecASFA.FP;
	
    public void nuevaFrecuencia(FrecASFA f)
    {
    	//captador = f;
    	frecs.add(f);
    }
    
    public double lastSent=0;
    
    public FrecASFA getData() {
    	if (frecs.isEmpty())
    		return FrecASFA.FP;
    	if (lastSent == 0)
    		lastSent = Clock.getSeconds();
    	else if (frecs.size() > 1)
    	{
    		if (lastSent != 0 && lastSent + 0.005 < Clock.getSeconds())
    		{
    			lastSent = 0;
    			return frecs.poll();
    		}
    	}
        return frecs.peek();
    	//return captador;
    }
}
