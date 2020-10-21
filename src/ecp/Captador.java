package ecp;

import java.util.LinkedList;
import java.util.Queue;

import ecp.ASFA.Modo;

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
    		if (lastSent + 0.005 < Clock.getSeconds())
    		{
    			lastSent = 0;
    			return frecs.poll();
    		}
    	}
        return frecs.peek();
    	//return captador;
    }
    
    public int[][] seguradesactivacionabajo = {{58560,62484,66671,71138,75904,80988,86447,93243,100572,108992,117006,126204}, {59160,63054,67212,71651,76380,81422,87042,93873,102001,108437,117065,126268}};
    public int[][] seguraactivacionabajo = {{59250,63220,67456,71976,76798,81943,87470,94346,101762,109325,118391,127698}, {59340,63246,67417,71871,76614,81670,87309,94160,102289,109714,118450,127762}};
    public int[][] seguraactivacionarriba = {{60760,64820,69164,73753,78742,84017,90496,97610,105283,112881,122487,132116}, {61260,65294,69603,74193,79090,84310,90957,98090,106571,112492,122549,132183}};
    public int[][] seguradesactivacionarriba = {{61440,65556,69949,74636,79636,84972,91519,98713,106473,113214,123871,133609}, {61440,65486,69808,74412,79323,84559,91224,98383,106901,113769,123933,133677}};
    public FrecASFA[] frecuencias = {FrecASFA.L1, FrecASFA.L2, FrecASFA.L3, FrecASFA.L4, FrecASFA.L5, FrecASFA.L6, FrecASFA.L7, FrecASFA.L8, FrecASFA.L9, FrecASFA.FP, FrecASFA.L10, FrecASFA.L11};
    //FrecASFA ultimaprocesada = FrecASFA.FP;
    public FrecASFA procesarFrecuencia(int freqHz)
    {
    	int num = Main.ASFA.captacionAV ? 1 : 0;
    	FrecASFA nueva = FrecASFA.AL;
    	for (int i=0; i<frecuencias.length; i++)
    	{
    		/*if (frecuencias[i] == ultimaprocesada && freqHz>=seguradesactivacionabajo[num][i] && freqHz<=seguradesactivacionarriba[i][num])
    			return ultimaprocesada;*/
    		if (freqHz>=seguraactivacionabajo[num][i] && freqHz<=seguraactivacionarriba[num][i])
    			return frecuencias[i];
    	}
    	//ultimaprocesada  = nueva;
    	return nueva;
    }
}
