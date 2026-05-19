package EJERCICIO5;
import Jpmi.*;
public class Lee implements Proceso {
     CanalSimple in;
     Object valor;    
     public Lee(CanalSimple in){
         this.in=in;
         valor=null;
     }    
     @Override
     public void run(){
         valor= in.receive();
     }    
     public Object getValor(){
         return valor;
     }
}
