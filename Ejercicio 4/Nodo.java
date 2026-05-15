import Jpmi.*;
public class Nodo implements Proceso{
    String name;
    CanalSimple canalOut;
    CanalSimple[] canalesIn;
    boolean Engrapador;
    int rondas;
    

    public Nodo(String name, CanalSimple canalOut, CanalSimple []canalesIn, boolean Engrapador, int rondas){
        this.name=name;
        this.canalOut=canalOut;
        this.canalesIn= canalesIn;
        this.Engrapador= Engrapador;
        this.rondas=rondas;
        
    }


    public void run(){
        if(Engrapador){
            runEngrapador();
            }else{
                runEmpleado();
            }
    }

    public void runEmpleado(){
        for(int i = 0; i < rondas; i++){
            System.out.println(name + " deja su copia en la mesa.");
            canalOut.send(new Papel(name, "copia"+ (i+1)));
        }
    }

    public void runEngrapador(){
        for(int i = 0; i < rondas; i ++){
            for(int j = 0; j < canalesIn.length; j++){
                Papel pp = (Papel) canalesIn[j].receive();
                System.out.println(name + " recoge: "+ pp.papel + " de " + pp.id);

            }
            System.out.println(name + " ENGRAPA las " + canalesIn.length + " copias.");
        }

    }



    
    
}
