package EJERCICIO5;
import Jpmi.*;

public class Plus implements Proceso {
    CanalSimple in1, in2, out;
    int valor1,valor2,suma; 
    
    public Plus(CanalSimple in1, CanalSimple in2, CanalSimple out) {
        this.in1 = in1;
        this.in2 = in2;
        this.out = out;
    }
    
    @Override
    public void run() {
        while (true) {
            Lee lee1 = new Lee(in1);
            Lee lee2 = new Lee(in2);
            Paralelo par = new Paralelo(new Proceso[]{ lee1, lee2 });
            par.run();
            
            // sumamos
            valor1 = (Integer) lee1.getValor();
            valor2 = (Integer) lee2.getValor();
            suma = valor1 + valor2;
            out.send(suma);
        }
    }
}