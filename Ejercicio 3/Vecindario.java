import Jpmi.*;
import java.util.Scanner;
public class Vecindario {

    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);

        System.out.println("De que tamaño va a ser el anillo de vecinos?");
        int tamaño = scanner.nextInt();
        scanner.close();

        CanalSimple[] objetos_Dinero = new CanalSimple[tamaño];
        String[] ids = new String[tamaño];

        for(int i = 0; i < tamaño; i++){
            CanalSimple Pagos = new CanalSimple();
            int c = i + 1;
            objetos_Dinero[i]= Pagos;
            ids[i] = "Vecino "+c;
        }

        Proceso[] nodos = new Proceso[tamaño];
        for(int i = 0; i < tamaño; i++){
            nodos[i]= new Nodo(
                ids[i],
                objetos_Dinero[(i - 1 + tamaño) % tamaño],
                objetos_Dinero[i],
                i == 0

            );

        }

        Paralelo par = new Paralelo(nodos);
        par.run();

    }

}
