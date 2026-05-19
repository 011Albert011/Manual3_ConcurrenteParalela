package EJERCICIO5;
import Jpmi.*;
public class Naturales implements Proceso {
    CanalSimple out;
    
    public Naturales(CanalSimple out) {
        this.out = out;
    }   
    @Override
    public void run() {
        int i = 0;
        while (true) {
            out.send(i);
            i++;
        }
    }
}