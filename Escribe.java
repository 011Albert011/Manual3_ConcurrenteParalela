package EJERCICIO5;
import Jpmi.*;
public class Escribe implements Proceso {
    CanalSimple canalOut;
    Object valor;
    
    public Escribe(Object valor, CanalSimple canalOut){
        this.canalOut=canalOut;
        this.valor=valor;
    }
    
    public void run(){
        canalOut.send(valor);
    }
}