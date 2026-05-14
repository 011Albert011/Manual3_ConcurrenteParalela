import Jpmi.*;
public class Proceso_Emisor implements Proceso{
    
    CanalSimple canal[];
    Mensaje msg;

    public Proceso_Emisor(Mensaje msg, CanalSimple... canal){
        this.msg=msg;
        this.canal=canal;
    }

    public void run(){
        for(int i = 0; i < 2 ; i++){
            System.out.println("Hola soy el vecino "+msg.id+" y le envio esta "+msg.dato+" a mi vecino.");
            canal[i].send(msg);
        }
    }
}
