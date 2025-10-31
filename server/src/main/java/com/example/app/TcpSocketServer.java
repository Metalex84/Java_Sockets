package com.example.app; // Añadir el paquete

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpSocketServer {
    static final int PORT = 9090; //
    private boolean end = false; //

    public void listen() { //
        ServerSocket serverSocket = null; //
        Socket clientSocket = null; //
        try { //
            serverSocket = new ServerSocket(PORT); //
            System.out.println("Servidor escuchando en el puerto " + PORT); // Mensaje añadido
            while (!end) { //
                clientSocket = serverSocket.accept(); //
                System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress().getHostAddress()); // Mensaje añadido
                // processamos la petición del cliente
                proccesClientMsg(clientSocket); //
                // cerramos el socket con el cliente
                closeClient(clientSocket); //
            }
            // cerramos el socket principal
            if (serverSocket != null && !serverSocket.isClosed()) { //
                serverSocket.close(); //
            }
        } catch (IOException ex) { //
            Logger.getLogger(TcpSocketServer.class.getName()).log(Level.SEVERE,
                    null, ex); //
        }
    } //

    public void proccesClientMsg(Socket clientSocket) { //
        boolean farewellMessage = false; //
        String clientMessage = ""; //
        BufferedReader in = null; //
        PrintStream out = null; //
        try { //
            in = new BufferedReader(new //
                    InputStreamReader(clientSocket.getInputStream())); //
            out = new PrintStream(clientSocket.getOutputStream()); //
            
            // Enviamos un mensaje inicial al cliente (protocolo de ejemplo)
            String initialMessage = "BIENVENIDO. Por favor, introduce tu solicitud.";
            out.println(initialMessage);
            out.flush();

            do { //
                clientMessage = in.readLine(); //
                System.out.println("Mensaje recibido del cliente: " + clientMessage); // Mensaje añadido
                
                if (clientMessage != null) {
                    // Aquí procesamos el mensaje del cliente
                    String dataToSend = processData(clientMessage); //
                    
                    farewellMessage = isFarewellMessage(clientMessage); //
                    
                    if (!farewellMessage) {
                        out.println(dataToSend); //
                        out.flush(); //
                    }
                }
            } while ((clientMessage) != null && !farewellMessage); //
            
            // Si el mensaje es de despedida, enviamos una confirmación final
            if (farewellMessage) {
                out.println("CONEXION CERRADA. ¡Hasta pronto!");
                out.flush();
            }

        } catch (IOException ex) { //
            Logger.getLogger(TcpSocketServer.class.getName()).log(Level.SEVERE,
                    null, ex); //
        } finally {
            // Se puede añadir cierre de streams aquí, aunque closeClient() lo manejará en el flujo principal.
        }
    } //

    // Métodos mock para simular la lógica de negocio (deben ser definidos)
    private String processData(String clientMessage) {
        if (clientMessage == null) return "ERROR: Mensaje nulo.";
        if (clientMessage.toLowerCase().contains("stock")) {
            return "El nivel de stock es bajo para el producto X.";
        }
        return "Respuesta al mensaje: " + clientMessage;
    }

    private boolean isFarewellMessage(String clientMessage) {
        if (clientMessage == null) return true;
        return clientMessage.trim().equalsIgnoreCase("FIN");
    }

    private void closeClient(Socket clientSocket) { // Implementación del método closeClient (asumido en el PDF)
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Conexión con el cliente cerrada.");
            }
        } catch (IOException e) {
            Logger.getLogger(TcpSocketServer.class.getName()).log(Level.SEVERE,
                    "Error al cerrar el socket del cliente", e);
        }
    }
    
    // Método MAIN para ejecución
    public static void main(String[] args) {
        TcpSocketServer server = new TcpSocketServer();
        server.listen();
    }
}
