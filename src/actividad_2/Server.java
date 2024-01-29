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

    private static final int PORT = 12345;
    private static final int ROWS = 3;
    private static final int COLUMNS = 4;
    private static final int MAX_ATTEMPTS = 4;

    private static String[][] board = new String[ROWS][COLUMNS];
    private static Map<String, int[]> prizes = new HashMap<>();
    private static int clientIdCounter = 0;
    private static int attempts;
    private static int prizesWon;
    private static Random rd = new Random();

    public static void main(String[] args) {
        initializeBoard();
        displayBoard();

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado => ID cliente: " + getNextClientId());

                // Iniciar un nuevo hilo para manejar al cliente
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized int getNextClientId() {
        return ++clientIdCounter;
    }

    private static void initializeBoard() {
        prizes.put("Crucero", new int[]{rd.nextInt(ROWS), rd.nextInt(COLUMNS)});
        prizes.put("Entradas", new int[]{rd.nextInt(ROWS), rd.nextInt(COLUMNS)});
        prizes.put("Masaje", new int[]{rd.nextInt(ROWS), rd.nextInt(COLUMNS)});
        prizes.put("1000€", new int[]{rd.nextInt(ROWS), rd.nextInt(COLUMNS)});

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                board[i][j] = "";
            }
        }

        for (Map.Entry<String, int[]> entry : prizes.entrySet()) {
            String prize = entry.getKey();
            int[] position = entry.getValue();
            board[position[0]][position[1]] = prize;
        }
    }

    private static void displayBoard() {
        System.out.println("Tablero Inicial:");
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                System.out.print(board[i][j] + "\t");
            }
            System.out.println();
        }
    }

    private static void handleClient(Socket clientSocket) {
        int clientId = getNextClientId();

        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            // Enviar ID al cliente
            out.writeObject(clientId);
            out.flush();

            // Verificar si el juego continúa
            boolean gameContinues = checkGameStatus(out);

            if (gameContinues) {
                attempts = 0;
                prizesWon = 0;

                while (attempts < MAX_ATTEMPTS && prizesWon < prizes.size()) {
                    // Recibir las coordenadas del cliente
                    int[] coordinates = (int[]) in.readObject();
                    int row = coordinates[0];
                    int column = coordinates[1];

                    // Verificar si hay un premio en esa posición
                    String prize = board[row][column];
                    if (!prize.isEmpty()) {
                        // Enviar el premio al cliente
                        out.writeObject("Felicidades, has encontrado " + prize + "!");
                        out.flush();

                        // Eliminar el premio del tablero
                        board[row][column] = "";
                        prizes.remove(prize);
                        prizesWon++;
                    } else {
                        // Enviar mensaje de que no hay premio en esa posición
                        out.writeObject("No hay premio en esa posición. Intento: " + (attempts + 1));
                        out.flush();
                    }

                    attempts++;
                }

                // Enviar notificación de juego finalizado al cliente
                if (attempts == MAX_ATTEMPTS || prizesWon == prizes.size()) {
                    out.writeObject("Juego finalizado. Intentos: " + attempts + ", Premios ganados: " + prizesWon);
                    out.flush();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Cliente cerrado => ID cliente: " + clientId);
            System.out.println("==> Desconecta ID cliente: " + clientId);
        }
    }

    private static boolean checkGameStatus(ObjectOutputStream out) throws IOException {
        boolean gameContinues = prizes.size() > 0;

        if (!gameContinues) {
            out.writeObject("El juego ha finalizado. No hay premios disponibles.");
            out.flush();
        }

        return gameContinues;
    }
}
