package EJERCICIO5;
import Jpmi.*;

public class Prefijo0 implements Proceso {
    CanalSimple canalIn, canalOut;
    
    public Prefijo0(CanalSimple canalIn, CanalSimple canalOut) {
        this.canalIn = canalIn;
        this.canalOut = canalOut;
    }
    
    @Override
    public void run() {
        //Ingresamos el cero inicial
        canalOut.send(0);
        
        while (true) {
            Object msg = canalIn.receive();
            canalOut.send(msg);
        }
    }
}