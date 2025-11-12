package com.example.app; // Añadir el paquete

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpSocketServer {
    static final int PORT = 9090; //
    private volatile boolean end = false; //
    private int clientCounter = 0; // Contador de clientes
    private ServerSocket serverSocket = null;

    // Thread para escuchar comandos de administración
    private class ServerControlThread extends Thread {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n========================================");
            System.out.println("Comandos disponibles:");
            System.out.println("  'STOP' o 'SHUTDOWN' - Detener el servidor");
            System.out.println("  'STATUS' - Ver estado del servidor");
            System.out.println("  'HELP' - Mostrar esta ayuda");
            System.out.println("========================================\n");
            
            while (!end) {
                try {
                    if (scanner.hasNextLine()) {
                        String command = scanner.nextLine().trim().toUpperCase();
                        
                        switch (command) {
                            case "STOP":
                            case "SHUTDOWN":
                                System.out.println("\n[SERVIDOR] Iniciando apagado...");
                                shutdown();
                                break;
                            case "STATUS":
                                showStatus();
                                break;
                            case "HELP":
                                showHelp();
                                break;
                            case "":
                                // Línea vacía, ignorar
                                break;
                            default:
                                System.out.println("Comando desconocido: " + command + ". Escribe 'HELP' para ver comandos disponibles.");
                        }
                    }
                    Thread.sleep(100); // Pequeña pausa para no consumir CPU
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    // Ignorar otros errores de entrada
                }
            }
            scanner.close();
        }
        
        private void showStatus() {
            System.out.println("\n[ESTADO DEL SERVIDOR]");
            System.out.println("  Puerto: " + PORT);
            System.out.println("  Clientes conectados (histórico): " + clientCounter);
            System.out.println("  Estado: " + (end ? "Deteniendo..." : "Activo"));
            System.out.println();
        }
        
        private void showHelp() {
            System.out.println("\n[AYUDA - COMANDOS DISPONIBLES]");
            System.out.println("  STOP / SHUTDOWN - Detiene el servidor de forma ordenada");
            System.out.println("  STATUS          - Muestra el estado actual del servidor");
            System.out.println("  HELP            - Muestra este mensaje de ayuda");
            System.out.println();
        }
    }
    
    public void shutdown() {
        end = true;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[SERVIDOR] Servidor detenido correctamente.");
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Error al cerrar el servidor: " + e.getMessage());
        }
    }

    public void listen() { //
        try { //
            serverSocket = new ServerSocket(PORT); //
            serverSocket.setSoTimeout(1000); // Timeout de 1 segundo para accept()
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   SERVIDOR TCP INICIADO                ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("Servidor escuchando en el puerto " + PORT); //
            System.out.println("Esperando conexiones de clientes...");
            
            // Iniciar thread de control del servidor
            ServerControlThread controlThread = new ServerControlThread();
            controlThread.setDaemon(false);
            controlThread.start();
            
            while (!end) { //
                try {
                    Socket clientSocket = serverSocket.accept(); //
                    clientCounter++;
                    System.out.println("\n[Cliente #" + clientCounter + "] Conectado desde: " + 
                                       clientSocket.getInetAddress().getHostAddress());
                    
                    // Crear un nuevo Thread para manejar este cliente
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientCounter);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start(); //
                } catch (SocketTimeoutException e) {
                    // Timeout esperado, permite verificar la variable 'end'
                    continue;
                }
            }
            
        } catch (IOException ex) { //
            if (!end) { // Solo loggear si no fue un cierre intencional
                Logger.getLogger(TcpSocketServer.class.getName()).log(Level.SEVERE,
                        null, ex); //
            }
        } finally {
            // cerramos el socket principal si aún está abierto
            try {
                if (serverSocket != null && !serverSocket.isClosed()) { //
                    serverSocket.close(); //
                }
            } catch (IOException e) {
                // Ignorar error en el cierre
            }
            System.out.println("\n[SERVIDOR] Servidor finalizado. Total de clientes atendidos: " + clientCounter);
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
        
        // Agregar shutdown hook para cierre limpio con Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SERVIDOR] Señal de interrupción recibida (Ctrl+C)...");
            server.shutdown();
        }));
        
        server.listen();
    }
}
