import Jpmi.*;

public class Mesa {

    public static void main(String[] argv){

        int personas = 3;
        int Rondas = 10;

        CanalSimple[] personal = new CanalSimple[personas];
        for(int i = 0; i < personas; i++){
            personal[i] = new CanalSimple();
        }

        Proceso[] nodos = new Proceso[personas + 1]; //el que engrapa
        for(int i= 0; i < personas; i++){
            nodos[i]= new Nodo(
                "Empleado" + (i+1),
                personal[i],
                null,
                false,
                Rondas
            );


        }

        nodos[personas]= new Nodo(
            "Engrapador",
            null,
            personal,
            true,
            Rondas
            );

        Paralelo par = new Paralelo(nodos);
        par.run();
        






        





    }
    
}
