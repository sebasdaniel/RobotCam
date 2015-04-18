/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package camararobotica;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;

import java.util.Scanner;

/*
 * Clase en la cual se establece la la configuracion de los sensores y la cantidad de fotografias a tomar
 * se encarga de envair la informacion al programa arduino para que este ordene los movimientos del servo
 * en los respectivos angulos y a la camara la cantidad de fotos que debe tomar y el tiempo que tiene para ello
 */
public class Configuracion {
    
    private static final int NUMERO_ANGULOS = 3;
    private static final int NUMERO_CELULARES = 2;
    
    private int numFotos;
    private int[] angulos;
    private String[] celulares;
    private ArrayList<String> usuarios;
    
    public Configuracion(){
        
        numFotos = 0;
        angulos = new int[NUMERO_ANGULOS];
        celulares = new String[NUMERO_CELULARES];
        usuarios = new ArrayList<>();
    }

    /**
     * @return the numFotos
     */
    public int getNumFotos() {
        return numFotos;
    }

    /**
     * @param numFotos the numFotos to set
     */
    public void setNumFotos(int numFotos) {
        this.numFotos = numFotos;
    }

    /**
     * @return the angulos
     */
    public int[] getAngulos() {
        return angulos;
    }

    /**
     * @param angulos the angulos to set
     */
    public void setAngulos(int[] angulos) {
        this.angulos = angulos;
    }

    /**
     * @return the usuarios
     */
    public ArrayList<String> getUsuarios() {
        return usuarios;
    }
    
    /**
     * @param usuarios the usuarios to set
     */
    public void setUsuarios(ArrayList<String> usuarios) {
        this.usuarios = usuarios;
    }
    
    /**
     * @return the celulares
     */
    public String[] getCelulares() {
        return celulares;
    }

    /**
     * @param celulares the celulares to set
     */
    public void setCelulares(String[] celulares) {
        this.celulares = celulares;
    }
    
    /*
     * este metodo se encarga de crear el archivo donde se guardan los angulos y el numero correspondiente
     * a la cantidad de fotos que se tomaran. Este archivo es creado con el nombre conf.txt
     * este metodo se encarga de la lectura de dicho archivo.
     */
    public void cargar(){
        try {
            
            File archivo=new File("conf.txt");
            
            if(archivo.exists()){
                
                Scanner entrada = new Scanner(archivo);
            
            if(entrada.hasNext()){
                
                this.numFotos = entrada.nextInt();
                
                for(int i=0; i<angulos.length; i++){
                    this.angulos[i] = entrada.nextInt();
                }
                
                for(int i=0; i<celulares.length; i++){
                    this.celulares[i] = entrada.next();
                }
            }
            
            while(entrada.hasNext()){
                    getUsuarios().add(entrada.next());
            }
            
            entrada.close();
            
            }else{
                
                archivo.createNewFile();
            }
            
        } catch (FileNotFoundException ex) {
            System.err.println("No se encontro el archivo");
        } catch (IOException  ex) {
       
            System.err.println("No se pudo crear el archivo");
        }
    }
    
    /*
     * Este metodo se encarga de guardar los valores correspondiente a la cantidad de fotos y
     * los agulos de giro del servomotor
     */
    
   public void guardar(){
       
        try {
            String tempUser = "";
            
            if(usuarios != null){
                
                for(String temp: usuarios){
                    
                    tempUser += " " + temp;
                }
            }
            
            Formatter salida = new Formatter("conf.txt");
            
            salida.format("%d %d %d %d %s %s%s\n", this.numFotos, this.angulos[0], this.angulos[1], this.angulos[2],
                    this.celulares[0], this.celulares[1], tempUser);
            
            salida.close();
            
        } catch (FileNotFoundException ex) {
            System.err.println("Error al crear el archivo");
        }
        
    }
   
}
