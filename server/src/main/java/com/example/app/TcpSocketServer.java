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
    private int clientCounter = 0; // Contador de clientes

    public void listen() { //
        ServerSocket serverSocket = null; //
        try { //
            serverSocket = new ServerSocket(PORT); //
            System.out.println("Servidor escuchando en el puerto " + PORT); //
            System.out.println("Esperando conexiones de clientes...");
            
            while (!end) { //
                Socket clientSocket = serverSocket.accept(); //
                clientCounter++;
                System.out.println("\n[Cliente #" + clientCounter + "] Conectado desde: " + 
                                   clientSocket.getInetAddress().getHostAddress());
                
                // Crear un nuevo Thread para manejar este cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientCounter);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start(); //
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

    // Clase interna para manejar cada cliente en un Thread separado
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int clientId;
        
        public ClientHandler(Socket socket, int id) {
            this.clientSocket = socket;
            this.clientId = id;
        }
        
        @Override
        public void run() {
            System.out.println("[Cliente #" + clientId + "] Thread iniciado: " + 
                             Thread.currentThread().getName());
            proccesClientMsg(clientSocket, clientId);
            closeClient(clientSocket, clientId);
            System.out.println("[Cliente #" + clientId + "] Thread finalizado.");
        }
    }
    
    public void proccesClientMsg(Socket clientSocket, int clientId) { //
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
                System.out.println("[Cliente #" + clientId + "] Mensaje recibido: " + clientMessage);
                
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
                    "[Cliente #" + clientId + "] Error", ex); //
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

    private void closeClient(Socket clientSocket, int clientId) { // Implementación del método closeClient (asumido en el PDF)
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("[Cliente #" + clientId + "] Conexión cerrada.");
            }
        } catch (IOException e) {
            Logger.getLogger(TcpSocketServer.class.getName()).log(Level.SEVERE,
                    "[Cliente #" + clientId + "] Error al cerrar el socket", e);
        }
    }
    
    // Método MAIN para ejecución
    public static void main(String[] args) {
        TcpSocketServer server = new TcpSocketServer();
        server.listen();
    }
}
