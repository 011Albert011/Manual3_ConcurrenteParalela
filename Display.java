package EJERCICIO5;
import Jpmi.*;
public class Display implements Proceso {
    CanalSimple in;
    Integer valor, limite=100;
    
    public Display(CanalSimple in) {
        this.in = in;
        valor = null;
    }   
    
    @Override
    public void run() {
        System.out.println("---SUCESION DE NUMEROS TRIANGULARES---");
        while (true) {
            valor = (Integer) in.receive();         
            
            if (valor > limite) {
                System.out.println("=========Fin: Valor >= "+limite);
                System.exit(0); 
            }
            System.out.println(valor);
        }
    }   
}