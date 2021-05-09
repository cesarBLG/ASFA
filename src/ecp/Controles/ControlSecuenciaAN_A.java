package ecp.Controles;

import ecp.ASFA;

public class ControlSecuenciaAN_A extends ControlFASF implements ControlReanudo {

	boolean AnteriorAumVel;
	boolean FirstBalise;
    public ControlSecuenciaAN_A(double time, TrainParameters param, boolean AnteriorAumVel, boolean FirstBalise) {
        super(time, 0, 0, 0, param);
        this.AnteriorAumVel = AnteriorAumVel;
        this.FirstBalise = FirstBalise;
        Curvas();
    }
    @Override
    public Curva[] obtenerCurvasAlmacen(int T)
    {
    	return obtenerCurvasAlmacen(T, AnteriorAumVel, FirstBalise ? "PRIMERA BALIZA" : "SEGUNDA BALIZA");
    }
	@Override
	Curva[] getCurvas_ADIF(int O) {
		Curva VC = null;
		Curva IF = null;
		if (Modo == ASFA.Modo.CONV) {
            if (!AnteriorAumVel) {
                double Vf = 60;
                if (FirstBalise || ASFA_version < 3) {
                    if (O >= 140) {
                        IF = new Curva(83, 63, 0.5, 5);
                        VC = new Curva(80, 60, 0.6, 3.5);
                    } else if (O == 120) {
                        IF = new Curva(83, 63, 0.36, 5);
                        VC = new Curva(80, 60, 0.46, 3.5);
                    } else if (O <= 100) {
                        IF = new Curva(63);
                        VC = new Curva(60);
                    }
                } else {
                    IF = new Curva(Vf + 3);
                    VC = new Curva(Vf);
                }
            } else {
                if (FirstBalise) {
                    if (O >= 140) {
                        IF = new Curva(103, 93, 0.5, 5);
                        VC = new Curva(100, 90, 0.6, 3.5);
                    } else if (O == 120) {
                        IF = new Curva(103, 93, 0.36, 5);
                        VC = new Curva(100, 90, 0.46, 3.5);
                    } else if (O <= 100) {
                        IF = new Curva(83, 63, 0.26, 5);
                        VC = new Curva(80, 60, 0.36, 2.5);
                    }
                } else {
                    if (O >= 160) {
                        IF = new Curva(93, 83, 0.5, 9);
                        VC = new Curva(90, 80, 0.6, 7.5);
                    } else if (O == 140) {
                        IF = new Curva(93, 83, 0.5, 10);
                        VC = new Curva(90, 80, 0.6, 7.5);
                    } else if (O == 120) {
                        IF = new Curva(93, 83, 0.36, 12);
                        VC = new Curva(90, 80, 0.46, 7.5);
                    } else if (O <= 100) {
                        IF = new Curva(63);
                        VC = new Curva(60);
                    }
                }
            }
        }
		if (Modo == ASFA.Modo.AV) {
            if (!AnteriorAumVel) {
                if(FirstBalise || ASFA_version < 3)
    			{
    				if (O >= 100) {
                        IF = new Curva(103);
                        VC = new Curva(100);
                    } else if (O < 100) {
                        IF = new Curva(O + 3, 83, 0.36, 5);
                        VC = new Curva(O, 80, 0.46, 3.5);
                    }
    			} else {
    				if (O >= 100) {
                        IF = new Curva(103);
                        VC = new Curva(100);
                    } else if (O < 100) {
                        IF = new Curva(83);
                        VC = new Curva(80);
                    }
                }
            } else {
            	if(FirstBalise)
    			{
    				if (O >= 140) {
                        IF = new Curva(143, 123, 0.5, 5);
                        VC = new Curva(140, 120, 0.6, 3.5);
                    } else if (O <= 120) {
                        IF = new Curva(O + 3);
                        VC = new Curva(O);
                    }
    			} else {
    				if (O >= 120) {
                        IF = new Curva(123, 103, 0.5, 5);
                        VC = new Curva(120, 100, 0.6, 3.5);
                    } else if (O < 120) {
                        IF = new Curva(O + 3);
                        VC = new Curva(O);
                    }
                }
            }
        }
		return new Curva[] {VC, IF};
	}
	@Override
	Curva[] getCurvas_AESF(int T, int v) {
		double vfc=0,v0c=0;
		if (Modo == ASFA.Modo.AV) {
			if (T>=140) {
				if (FirstBalise) {
					v0c = AnteriorAumVel ? 140 : 100;
					vfc = AnteriorAumVel ? 120 : 100;
				} else {
					v0c = AnteriorAumVel ? 120 : 100;
					vfc = 100;
				}
			} else if (T==120) {
				if (FirstBalise) {
					v0c = AnteriorAumVel ? 120 : 100;
					vfc = AnteriorAumVel ? 120 : 100;
				} else {
					v0c = AnteriorAumVel ? 120 : 100;
					vfc = 100;
				}
			} else if (T==100) {
				v0c = vfc = 100;
			} if (T<100) {
				if (FirstBalise) {
					v0c = T;
					vfc = AnteriorAumVel ? T : 80;
				} else {
					v0c = AnteriorAumVel ? T : 80;
					vfc = AnteriorAumVel ? T : 80;
				}
			}
		} else {
			if (T>=120) {
				if (FirstBalise) {
					v0c = AnteriorAumVel ? 100 : 80;
					vfc = AnteriorAumVel ? 90 : 60;
				} else {
					v0c = AnteriorAumVel ? 90 : 60;
					vfc = AnteriorAumVel ? 80 : 60;
				}
			} else {
				if (FirstBalise) {
					v0c = AnteriorAumVel ? 80 : 60;
					vfc = 60;
				} else {
					v0c = vfc = 60;
				}
			}
		}
		return Curva.generarCurvas(this, v0c, vfc);
	}

    boolean activado = false;
	@Override
	public void Activar(boolean val) {
		activado = val;
	}
	@Override
	public boolean Activado() {
		return activado;
	}
	double distancia = -1;
	@Override
	public double UltimaDistancia() {
		return distancia;
	}
	@Override
	public void ActualizarDistancia(double val) {
		distancia = val;
	}
}
