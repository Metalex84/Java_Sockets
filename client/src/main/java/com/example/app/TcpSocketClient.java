package com.example.app; // Añadir el paquete

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner; // Añadido para simular entrada de usuario
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpSocketClient { //

    public void connect(String address, int port) { //
        String serverData; //
        String request; //
        boolean continueConnected = true; //
        Socket socket = null; //
        BufferedReader in = null; //
        PrintStream out = null; //
        Scanner scanner = new Scanner(System.in); // Para leer comandos

        try { //
            System.out.println("Intentando conectar a: " + address + ":" + port); // Mensaje añadido
            socket = new Socket(InetAddress.getByName(address), port); //
            System.out.println("¡Conexión establecida!"); // Mensaje añadido

            in = new BufferedReader(new //
                    InputStreamReader(socket.getInputStream())); //
            out = new PrintStream(socket.getOutputStream()); //
            
            // el cliente debe estar conectado al puerto hasta que se cierre
            while (continueConnected) { //
                
                // 1. Leer el mensaje del servidor
                serverData = in.readLine(); //
                if (serverData == null) {
                    System.out.println("Servidor cerró la conexión.");
                    break;
                }
                System.out.println("\n<<< Servidor: " + serverData);

                // 2. se procesa la respuesta y se envia nueva petición
                // Simulación: Pedir al usuario que introduzca una petición
                System.out.print(">>> Cliente (Escribe 'stock' o 'FIN' para salir): ");
                request = scanner.nextLine();
                
                // request = getRequest(serverData); // (Usando input simulado)

                out.println(request); // (assegurem que acaba amb un final de linia)
                out.flush(); // (assegurem que s'envia)

                // comprobamos si se finaliza
                continueConnected = mustFinish(request); //
            }

        } catch (UnknownHostException ex) { //
            reportError("Error de conexión, no existe el socket", ex); //
        } catch (IOException ex) { //
            reportError("Error de conexión indefinido", ex); //
        } finally {
            close(socket); //
            scanner.close();
        }
    } //

    // Métodos mock para simular la lógica de negocio (deben ser definidos)
    // private String getRequest(String serverData) { return "PETICION DE EJEMPLO"; } 
    
    private boolean mustFinish(String request) {
        return !request.trim().equalsIgnoreCase("FIN");
    }
    
    private void reportError(String message, Exception ex) { // Implementación del método reportError (asumido en el PDF)
        System.err.println(message + ": " + ex.getMessage());
        Logger.getLogger(TcpSocketClient.class.getName()).log(Level.SEVERE, message, ex);
    }
    
    private void close(Socket socket) { // Implementación del método close (asumido en el PDF)
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("\nConexión cerrada por el cliente.");
            }
        } catch (IOException e) {
            reportError("Error al cerrar el socket del cliente", e);
        }
    }
    
    // Método MAIN para ejecución
    public static void main(String[] args) {
        TcpSocketClient client = new TcpSocketClient();
        // Asume que el servidor está en 'localhost' (127.0.0.1) y el puerto es 9090
        client.connect("127.0.0.1", 9090); 
    }
}
