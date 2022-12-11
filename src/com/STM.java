package com;

import dmi.Botones.Botón.TipoBotón;
import ecp.ASFA;
import ecp.Clock;

public class STM {
	enum STMState
	{
	    NP,
	    PO,
	    CO,
	    DE,
	    CS,
	    HS,
	    DA,
	    FA
	}
	STMState state;
	ASFA asfa;
	public boolean trip;
	boolean override;
	STMState requested;
	boolean conditional;
	double lastTime;
	public STM(ASFA asfa)
	{
		/*this.asfa = asfa;
		asfa.stm = this;
		new Thread(() -> {
			while(true) {
				Update();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();*/
	}
	public void Update()
	{
		boolean powered = asfa.Activated;
		STMState prevState = state;
	    if (!powered)
	        state = STMState.NP;
	    else if (state == STMState.NP)
	        state = STMState.PO;
	    else {
	        if (requested == STMState.CO) { // Configuration
	            if (state == STMState.PO)
	                state = STMState.CO;
	            else if (state != STMState.CO)
	                state = STMState.FA;
	        } else if (requested == STMState.DE) { // Data entry
	            if (state == STMState.CO)
	                state = STMState.DE;
	            else if (state != STMState.DE)
	                state = STMState.FA;
	        } else if (requested == STMState.CS && !conditional) { // Unconditional cold standby
	            if (state == STMState.CO || state == STMState.DE || state == STMState.HS || state == STMState.DA)
	                state = STMState.CS;
	            else if (state != STMState.CS)
	                state = STMState.FA;
	        } else if (requested == STMState.CS && conditional) { // Conditional cold standby
	            if (state == STMState.DA) {
	                if (!trip)
	                    state = STMState.CS;
	            } else if (state != STMState.CS)
	                state = STMState.FA;
	        } else if (requested == STMState.HS) { // Hot standby
	            if (state == STMState.CS)
	                state = STMState.HS;
	            else if (state != STMState.HS)
	                state = STMState.FA;
	        } else if (requested == STMState.DA) { // Data available
	            if (state == STMState.CS || state == STMState.HS)
	                state = STMState.DA;
	            else if (state != STMState.DA)
	                state = STMState.FA;
	        } else if (requested == STMState.FA) { // Failure
	            state = STMState.FA;
	        }
	    }
	    asfa.CON = state != STMState.CS;
	    asfa.AKT = state != STMState.DA;
	    if (state != prevState)
	    {
	    	System.out.println(state);
	    }
	    if (state == STMState.NP) return;
	    if (!asfa.FE) trip = false;
	    if (!override && asfa.RebaseAuto)
	    {
	    	asfa.display.orclient.sendData("noretain(stm::asfa::override=1)");
	    }
	    override = asfa.RebaseAuto;
	    if (Clock.getSeconds() - lastTime > 1)
	    {
	    	asfa.display.orclient.sendData("noretain(stm::asfa::state="+state.ordinal()+")");
	    	if (state == STMState.PO)
	    		asfa.display.orclient.sendData("noretain(stm::asfa::request="+STMState.CO.ordinal()+")");
	    	else if (state == STMState.CO && asfa.estadoInicio == 0)
	    		asfa.display.orclient.sendData("noretain(stm::asfa::request="+STMState.CS.ordinal()+")");
	    	if (trip) asfa.display.orclient.sendData("noretain(stm::asfa::trip=1)");
	    	lastTime = Clock.getSeconds();
	    }
	    requested = STMState.NP;
	    conditional = false;
	}
	public void trip()
	{
		trip = true;
    	asfa.display.orclient.sendData("noretain(stm::asfa::trip=1)");
	}
	void override()
	{
		if (!asfa.basico) asfa.display.startSound("S4");
		override = true;
		asfa.RebaseAuto = true;
        asfa.InicioRebase = Clock.getSeconds();
        asfa.display.iluminar(TipoBotón.Rebase, true);
	}
}
