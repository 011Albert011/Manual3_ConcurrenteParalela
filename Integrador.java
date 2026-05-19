package EJERCICIO5;
import Jpmi.*;

public class Integrador {
    
    public static void main(String args[]) {
        CanalSimple in_naturales = new CanalSimple(); 
        CanalSimple a = new CanalSimple();      
        CanalSimple b = new CanalSimple();      
        CanalSimple c = new CanalSimple();   
        CanalSimple out = new CanalSimple();    
        
        Paralelo par = new Paralelo(new Proceso[]{
            new Naturales(in_naturales),
            new Plus(in_naturales, a, b),
            new Delta(b, out, c),
            new Prefijo0(c, a),
            new Display(out)
        });
        par.run();
    }
}