import Jpmi.*;
public class Proceso_Receptor implements Proceso{
 
    CanalSimple canal[];
    Mensaje msg;
    String name;

    public Proceso_Receptor(String name, CanalSimple... canal){
        this.name=name;
        this.canal=canal;

    }

    public void run(){
        for(int i = 0; i < 1; i++){
            msg=(Mensaje)canal[i].receive();
            System.out.println("Yo soy el vecino "+name+" y recibi la "+msg.dato+" de mi vecino "+msg.id);

        }

    }

}
