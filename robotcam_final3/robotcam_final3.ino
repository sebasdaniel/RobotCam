#include <SoftwareSerial.h>

#include <Servo.h>

const int sensorPin[] = {10, 11, 12}; // pines correspondiente a los sensores
int angulos[3]; // arreglo para almacenar los angulos de cada sensor
int cont = 0; // indice para controlar el arreglo de angulos
Servo motor; // variable para manipular el servomotor
String numCels[2];
SoftwareSerial myCell(7,8); // pines de comunicacion serial para el GPRS shield

boolean abierto0 = false; // variables de control para supervisar los sensores
boolean abierto1 = false;
boolean abierto2 = false;

boolean vigilar = false; // permite intercambiar de esperar el valor de angulos, a vigilar los sensores

int val; // temporal que recive el valor de los angulos
String entrada = "";

boolean completado = false; // cambiar por completado

void setup() { // se ejecuta una ves cuando el arduino inicia
  
  // activar/desactivar gprs
  pinMode(9, OUTPUT); // pin del gprs utilizado para activar/desactivar por software
//  digitalWrite(9,LOW);
//  delay(1000);
//  digitalWrite(9,HIGH);
//  delay(2000);
//  digitalWrite(9,LOW);
//  delay(3000);
  // fin activar/desactivar gprs
  
  pinMode(sensorPin[0], INPUT); // establecen que los pines que se usan como entrada, en este caso los correspondientes a los sensores
  pinMode(sensorPin[1], INPUT);
  pinMode(sensorPin[2], INPUT);
  
  motor.attach(5); // asigna el pin que va a usarse para controlar el motor
  
  myCell.begin(19200); // inicia la comunicacion serial con el gprs shield a 19200 baud
  
  Serial.begin(9600); // inicia la comunicacion serial en 9600 baud, que es la velocidad de transmicion
  delay(1000);
  
//  myCell.print("\r");
//  delay(1000);
//  myCell.print("AT+CMGF=1\r"); // enviar sms en modo texto
//  delay(1000);
//  myCell.print("AT+CMGD=1,4\r"); // borrar todos los mensajes
//  delay(1000);
}

void loop(){ // se ejecuta una y otra vez por siempre
  
  if(!vigilar){ // lo primero es esperar a que la aplicacion envie los angulos para cada sensor, los cuales se guardan en el arreglo angulos
    
    if(Serial.available() && !completado){ // si se han enviado datos por el serial y no se ha completado la lectura
      
      char inChar = Serial.read();
      if(inChar == '\n'){ // para verificar si se ha completado
        completado = true;
      }
      entrada += inChar;
      
    }else{
      
      // si se ha completado la lectura se procesan los datos
      if(completado){
        
        // inicio y final de lectura de un dato en concreto
        int initIndex = 0;
        int endIndex = entrada.indexOf(',');
        
        while(cont < 5){ // leemos los 5 datos
          
          String temp = "";
          
          for(int i=initIndex; i<endIndex; i++){ // leemos un dato
            temp += entrada[i];
          }
          
          if(cont < 3){ // si son los tres primero, son los angulos, sino son los numeros de celular
            angulos[cont] = temp.toInt();
          }else{
            numCels[(cont-3)] = temp;
          }
          
          initIndex = endIndex + 1;
          endIndex = entrada.indexOf(',', initIndex);
          
          cont++;
        }
        
        
//        if(entrada.length() > 0){
//          Serial.println("procesado:");
//          delay(100);
//          Serial.println(angulos[0]);
//          Serial.println(angulos[1]);
//          Serial.println(angulos[2]);
//          Serial.println();
//          delay(100);
//          Serial.println(numCels[0]);
//          Serial.println(numCels[1]);
//        }
        
        // activar/desactivar gprs
//        digitalWrite(9,LOW);
//        delay(1000);
//        digitalWrite(9,HIGH);
//        delay(2000);
//        digitalWrite(9,LOW);
//        delay(3000);
        // fin activar/desactivar gprs
        
        activarDesactivarGprs(); // por defecto inicia apagado el shield, entonces al iniciar el estdo de vigilancia se activa el gprs shield
        
        // configurar gprs
//        myCell.begin(19200);
//        delay(1000);
        myCell.print("\r");
        delay(1000);
        myCell.print("AT+CMGF=1\r"); // enviar sms en modo texto
        delay(1000);
        myCell.print("AT+CMGD=1,4\r"); // borrar todos los mensajes
        delay(1000);
        // fin configurar gprs
        
        entrada = "";
        vigilar = true;
        cont = 0;
        completado = false;
      }
    }
    
  }else{ // despues de recivir los valores de los angulos
    
    if(!abierto0){ // si el sensor esta cerrado, verifica si se abre
    
      if(!digitalRead(sensorPin[0])){ // en caso de abrirse el sensor
      
        Serial.println(String("s11")); // manda el evento correspondiente a este sensor a traves de la conexion serial
        motor.write(angulos[0]); // hace que el motor gire a la posicion correspondiente a dicho sensor, segun el angulo establecido
        if(numCels[0] != "n"){ // si s recivio una n quiere decir que no hay numero al que enviar mensaje por tanto solo se envia mensaje cuando haya un numero almacenado
          enviarMensaje(numCels[0],String("ventana derecha abierta"));
          delay(5000);
        }
        if(numCels[1] != "n"){ // comprobar si hay numero para enviar mensaje en el segundo registro
          enviarMensaje(numCels[1],String("ventana derecha abierta"));
          delay(5000);
        }
        abierto0 = true; // como esta abierto no debe entrar otra vez a esta condicion
      }
      
    }else{ // si el sensor esta abierto, verifica si se cierra
    
      if(digitalRead(sensorPin[0])){ // en caso de que se cierre el sensor
        Serial.println(String("s10")); // manda por medio del serial el evento correspondiete
        motor.write(angulos[0]);
        if(numCels[0] != "n"){
          enviarMensaje(numCels[0],String("ventana derecha cerrada"));
          delay(5000);
        }
        if(numCels[1] != "n"){
          enviarMensaje(numCels[1],String("ventana derecha cerrada"));
          delay(5000);
        }
        abierto0 = false; // como se cerro el sensor no debe entrar a este else si no al if.
      }
      
    } // se aplica lo mismo de este sensor para los otros sensores
    
    if(!abierto1){
      if(!digitalRead(sensorPin[1])){
        Serial.println(String("s21"));
        motor.write(angulos[1]);
        if(numCels[0] != "n"){
          enviarMensaje(numCels[0],String("puerta abierta"));
          delay(5000);
        }
        if(numCels[1] != "n"){
          enviarMensaje(numCels[1],String("puerta abierta"));
          delay(5000);
        }
        abierto1 = true;
      }
    }else{
      if(digitalRead(sensorPin[1])){
        Serial.println(String("s20"));
        motor.write(angulos[1]);
        if(numCels[0] != "n"){
          enviarMensaje(numCels[0],String("puerta cerrada"));
          delay(5000);
        }
        if(numCels[1] != "n"){
          enviarMensaje(numCels[1],String("puerta cerrada"));
          delay(5000);
        }
        abierto1 = false;
      }
    }
    
    if(!abierto2){
      if(!digitalRead(sensorPin[2])){
        Serial.println(String("s31"));
        motor.write(angulos[2]);
        if(numCels[0] != "n"){
          enviarMensaje(numCels[0],String("ventana izquierda abierta"));
          delay(5000);
        }
        if(numCels[1] != "n"){
          enviarMensaje(numCels[1],String("ventana izquierda abierta"));
          delay(5000);
        }
        abierto2 = true;
      }
    }else{
      if(digitalRead(sensorPin[2])){
        Serial.println(String("s30"));
        motor.write(angulos[2]);
        if(numCels[0] != "n"){
          enviarMensaje(numCels[0],String("ventana izquierda cerrada"));
          delay(5000);
        }
        if(numCels[1] != "n"){
          enviarMensaje(numCels[1],String("ventana izquierda cerrada"));
          delay(5000);
        }
        abierto2 = false;
      }
    }
    
    if(Serial.available()){ // Lee el codigo de desactivacion
      
      if(Serial.read() == '0'){ // Si el codigo es correcto desactiva la vigilancia y desactiva el gprs shield
        activarDesactivarGprs();
        vigilar = false;
      }
    }
    
  }
  
}

// metodo para enviar sms, recive como parametro el numero al que se le va a enviar el sms, y el contenido del sms
void enviarMensaje(String num, String msg){
  
  // ayuda al debug :)
  String salida = "mensaje enviado a: " + num;
  Serial.println(salida);
  delay(200);
  
  String temp = "AT+CMGS=\"" + num + "\"\r"; // codigo at para mandar sms al numero especificado
  String temp2 = msg + "\r"; // contenido del sms
  //myCell.print("AT+CMGS=\"3126167116\"\r");
  myCell.print(temp);
  delay(1000);
  myCell.print(temp2);
  delay(1000);
  myCell.write(26); // equivalente al Ctrl + z, para enviar el sms
//  delay(1000);
//  myCell.println();
  delay(5000);
}

void activarDesactivarGprs(){ // activa o desactiva el gprs mediante software, hace lo mismo que el boton fisico
  digitalWrite(9,LOW);
  delay(1000);
  digitalWrite(9,HIGH);
  delay(2000);
  digitalWrite(9,LOW);
  delay(3000);
}

