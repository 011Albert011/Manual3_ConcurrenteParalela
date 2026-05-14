import Jpmi.*;
public class Nodo implements Proceso{

    String name;
    CanalSimple canalOut;
    CanalSimple canalIn;
    boolean iniciador;

    public Nodo(String name, CanalSimple canalIn, CanalSimple canalOut, Boolean iniciador){

        this.name=name;
        this.canalIn=canalIn;
        this.canalOut=canalOut;
        this.iniciador=iniciador;

    }

    public void run(){
        if(iniciador){
            System.out.println("Hola soy "+name+" y yo inicio la tanda");
            canalOut.send(new Mensaje(name, "'Pago de tanda'"));
        }

        for(;;){
            Mensaje msg = (Mensaje) canalIn.receive();
            System.out.println("Yo soy "+name+" y recive el "+msg.dato+" de mi "+msg.id);
            msg.id=name;
            System.out.println("Hola soy "+name+" y le envio "+msg.dato+" a mi vecino");
            canalOut.send(msg);
        }
    }
    
}
