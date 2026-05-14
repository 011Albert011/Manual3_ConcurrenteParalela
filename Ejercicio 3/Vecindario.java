import Jpmi.*;
import java.util.Scanner;
public class Vecindario {

    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);

        System.out.println("De que tamaño va a ser el anillo de vecinos?");
        int tamaño = scanner.nextInt();
        scanner.close();

        CanalSimple[] objetos_regalo = new CanalSimple[tamaño];
        String[] ids = new String[tamaño];

        for(int i = 0; i < tamaño; i++){
            CanalSimple regalo = new CanalSimple();
            int c = i + 1;
            objetos_regalo[i]= regalo;
            ids[i] = "Vecino "+c;
        }

        int i=0;

        for(;;){

            i = i % tamaño;
            int indice = i % tamaño;

            Paralelo par = new Paralelo(new Proceso[]{
                new Proceso_Emisor(new Mensaje(ids[i], "'Canasta de tacos'"),objetos_regalo[i],objetos_regalo[(i+1)%tamaño]),
                new Proceso_Receptor(ids[(indice - 1 + tamaño) % tamaño], objetos_regalo[i]),
                new Proceso_Receptor(ids[(indice + 1) % tamaño], objetos_regalo[(i+1)%tamaño]),
            });
            par.run();
            i++;
            
        }
    }

}
