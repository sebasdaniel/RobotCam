/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package camararobotica;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_highgui;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/* Clase en cargada de administrar la comunicacion entre el PC y Arduino
 * A traves de hilos de ejecucion mantiente la coneccion e interaccion entre el sistema Arduino-Servo-Camara
 * Vigila que se lleven a cabo los respectivos eventos cuando se realicen lecturas por los sensores
 * Como la captura de fotografias, esto lo hace a poyandoce en librerias como OpenCV
 * 
 */

public class Centinela implements Runnable {
    
    private OutputStream output;
    private InputStream input;
    private SerialPort serialPort;
    
    private int angulos[];
    private String numerosCel[];
    private int fotos;
    private List<String> usuariosTwitter;
    
    private String evento;
    private boolean continuar;
    private OpenCVFrameGrabber grabber;
    private opencv_core.IplImage imagen;
    
    public Centinela(int angulosSensor[], String numeros[], int numFotos, List<String> usuarios) throws Exception{
        
        angulos = angulosSensor;
        numerosCel = numeros;
        fotos = numFotos;
        continuar = true;
        usuariosTwitter = usuarios;
        
        /* cambie el puerto de COM6 a COM3*/
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("COM3");
        
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            
            CommPort commPort = portIdentifier.open("arduino", 2000);

            if (commPort instanceof SerialPort) {
                
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                input = serialPort.getInputStream();
                output = serialPort.getOutputStream();
                
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
        
         try {
            
            grabber = new OpenCVFrameGrabber(1);
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);
            grabber.start();
               
        } catch (FrameGrabber.Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getStackTrace());
        }
        
    }

    @Override
    public void run() {
        
        if(output != null && input != null){
            try {
                Thread.sleep(2000);
                
                String datos = "";
                
                for(int i=0; i<angulos.length; i++){
                    
//                    String temp = "";
                    datos += angulos[i]+",";
//                    output.write(temp.getBytes());
//                    output.flush();
//                    Thread.sleep(200);
                }
                
                datos += numerosCel[0] + "," + numerosCel[1] + ",\n";
                
                output.write(datos.getBytes());
                output.flush();
                Thread.sleep(200);
                
//                for (String cels : numerosCel) {
//                    
////                    String temp = cels + "\n";
//                    datos = cels + ",";
////                    output.write(temp.getBytes());
////                    output.flush();
////                    Thread.sleep(200);
//                }
                
                byte[] buffer = new byte[1024];
                int len;
                
                while (continuar) {
                    
                    len = input.read(buffer);                    
                    evento += new String(buffer, 0, len);
                    
                    if(evento.contains("\n")){
                        ProcesarEvento(evento);
                        evento = "";
                    }
                    
                }
                output.write("0".getBytes());
                output.flush();
                
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(Centinela.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(serialPort != null){
            serialPort.close();
        }
        try {
            grabber.stop();
            grabber.release();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(Centinela.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    public void detener(){
        continuar = false;
    }
    
    /*
     * En este metodo se indica o se establece la captura de foto segun el sensor que hizo la lectura y en que esta la realizo
     * es decir si hizo la lectura por apertura s11, s21, s31 o cuando se cierra s10, s20, s30
     */
        
    public void ProcesarEvento(String event){
          
        if(event.contains("s11")){
            TomarFotos("s11");
        }else{
            if(event.contains("s21")){
                TomarFotos("s21");
            }else{
                if(event.contains("s31")){
                    TomarFotos("s31");
                }else{
                    if(event.contains("s10")){
                        TomarFotos("s10");
                    }else{
                        if(event.contains("s20")){
                            TomarFotos("s20");
                        }else{
                            if(event.contains("s30")){
                                TomarFotos("s30");
                            }
                        }
                    }
                }
            }
        }
    }
    
    /*
     * Este metodo TomarFotos apoyado en la libreria OpenCV, se encarga de tomar las fotografias y 
     * establecer una ruta para guardarlas en el formato definido .jpg
     * para la creacion y denominacion (nombre) del directorio donde se guardaran las fotografias se tiene encuenta la fecha
     * en la cual se realiza la lectura de los sensores.
     * en cuanto al nombre de las fotografias fue escogido la hora, minuto, segundo, sensor que hace la lectura y un numero identificador
     * dentro de este metodo actua un hilo (Thread) que procesa el evento de tomado de foto y especifica el tiempo para la captura
     */
    
    public void TomarFotos(String nombre){
        
        Calendar tiempo= Calendar.getInstance();
        DecimalFormat formato=new DecimalFormat("00");
        String fecha=formato.format(tiempo.get(Calendar.DAY_OF_MONTH))+formato.format(tiempo.get(Calendar.MONTH)+1)+tiempo.get(Calendar.YEAR);

        File directorio = new File("capturas\\"+fecha);
        
        if(!directorio.exists()){
            
            directorio.mkdir();
        }
        
        String nomHora= tiempo.get(Calendar.HOUR_OF_DAY)+formato.format(tiempo.get(Calendar.MINUTE))+formato.format(tiempo.get(Calendar.SECOND))+nombre+"-";
        String HoraTweet=tiempo.get(Calendar.HOUR_OF_DAY)+":"+formato.format(tiempo.get(Calendar.MINUTE))+":"+formato.format(tiempo.get(Calendar.SECOND));
        
        try {
            Thread.sleep(1000);
            imagen = grabber.grab();
            
            for (int i = 0; i < fotos; i++) {

                imagen = grabber.grab();
                opencv_highgui.cvSaveImage(directorio+"\\"+nomHora + i + ".jpg", imagen);

                Thread.sleep(100);

            }
            switch (nombre) {
                case "s11":
                case "s10":
                    enviarTweet(directorio+"\\"+nomHora+"0.jpg", "Movimiento detectado a las"+HoraTweet+" por el sensor "+nombre+"en la ventana derecha");
                    break;
                case "s21":
                case "s20":
                    enviarTweet(directorio+"\\"+nomHora+"0.jpg", "Movimiento detectado a las"+HoraTweet+" por el sensor "+nombre+"en la puerta");
                    break;        
                case "s31":
                case "s30":
                    enviarTweet(directorio+"\\"+nomHora+"0.jpg", "Movimiento detectado a las"+HoraTweet+" por el sensor "+nombre+"en la ventana izquierda");
                    break;
            }
            
            /*mensaje general
             *  enviarTweet(directorio+"\\"+nomHora+"0.jpg", "Movimiento detectado a las"+HoraTweet+" por el sensor "+nombre);    
             */
            
        } catch (InterruptedException | FrameGrabber.Exception ex) {
            Logger.getLogger(Centinela.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    public void enviarTweet(String imagen, String mensaje){
        
        MensajesTwitter mt = new MensajesTwitter(mensaje, usuariosTwitter, imagen);
        new Thread(mt).start();
    }
    
}