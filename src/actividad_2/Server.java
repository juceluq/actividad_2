package actividad_2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server {

    private static final int PUERTO = 12345;
    private static final int FILAS = 3;
    private static final int COLUMNAS = 4;
    private static final int INTENTOS_MAX = 4;

    private static String[][] tabla = new String[FILAS][COLUMNAS];
    private static Map<String, int[]> premios = new HashMap<>();
    private static int clientIdContador = 0;
    private static int intentos;
    private static int premiosGanados;
    private static Random rd = new Random();

    public static void main(String[] args) {
        iniciarTabla();
        mostrarTabla();

        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado => ID cliente: " + getProxClientId());

                // Iniciar un nuevo hilo para manejar al cliente
                new Thread(() -> manejadorCliente(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized int getProxClientId() {
        return clientIdContador++;
    }

    private static void iniciarTabla() {
        String[] prizeNames = {"Crucero", "Entradas", "Masaje", "1000€"};

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                tabla[i][j] = "";
            }
        }

        for (String premio : prizeNames) {
            int row, column;
            do {
                row = rd.nextInt(FILAS);
                column = rd.nextInt(COLUMNAS);
            } while (!tabla[row][column].isEmpty()); // Repite si la posición ya está ocupada

            tabla[row][column] = premio;
            premios.put(premio, new int[]{row, column});
        }
    }

    private static void mostrarTabla() {
        System.out.println("Tablero Inicial:");
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                System.out.print(tabla[i][j] + "\t");
            }
            System.out.println();
        }
    }

    private static void manejadorCliente(Socket clientSocket) {
        int clientId = getProxClientId();

        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            // Enviar ID al cliente
            out.writeObject(clientId);
            out.flush();

            // Verificar si el juego continúa
            boolean continuar = verificarEstadoJuego(out);

            if (continuar) {
                intentos = 0;
                premiosGanados = 0;

                while (intentos < INTENTOS_MAX && premiosGanados < premios.size()) {
                    // Recibir las coordenadas del cliente
                    int[] coordenadas = (int[]) in.readObject();
                    int fila = coordenadas[0];
                    int columna = coordenadas[1];

                    // Verificar si hay un premio en esa posición
                    String premio = tabla[fila][columna];
                    if (!premio.isEmpty()) {
                        // Enviar el premio al cliente
                        out.writeObject("Felicidades, has encontrado " + premio + "!");
                        out.flush();

                        // Eliminar el premio del tablero
                        tabla[fila][columna] = "";
                        premios.remove(premio);
                        premiosGanados++;
                    } else {
                        // Enviar mensaje de que no hay premio en esa posición
                        out.writeObject("No hay premio en esa posición. Intento: " + (intentos + 1));
                        out.flush();
                    }

                    intentos++;
                }

                // Enviar notificación de juego finalizado al cliente
                if (intentos == INTENTOS_MAX || premiosGanados == premios.size()) {
                    out.writeObject("Juego finalizado. Intentos: " + intentos + ", Premios ganados: " + premiosGanados);
                    out.flush();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Cliente cerrado => ID cliente: " + clientId);
            System.out.println("==> Desconecta ID cliente: " + clientId);
        }
    }

    private static boolean verificarEstadoJuego(ObjectOutputStream out) throws IOException {
        boolean gameContinues = !premios.isEmpty();

        if (!gameContinues) {
            out.writeObject("El juego ha finalizado. No hay premios disponibles.");
            out.flush();
        } else {
            out.writeObject("");
        }

        return gameContinues;
    }
}
