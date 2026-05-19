package EJERCICIO5;
import Jpmi.*;

public class Delta implements Proceso {
    CanalSimple canalIn, canalOut1, canalOut2;
    Object msg;
    
    public Delta(CanalSimple canalIn, CanalSimple canalOut1, CanalSimple canalOut2) {
        this.canalIn = canalIn;
        this.canalOut1 = canalOut1;
        this.canalOut2 = canalOut2;
        msg = null;
    }
    
    @Override
    public void run() {
        while (true) {
            msg = canalIn.receive();
            
            Paralelo par = new Paralelo(new Proceso[]{
                new Escribe(msg, canalOut1),
                new Escribe(msg, canalOut2)
            });
            
            par.run();  
        }
    }
}