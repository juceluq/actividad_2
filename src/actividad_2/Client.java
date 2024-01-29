package actividad_2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            // Recibir ID del cliente
            int clientId = (int) in.readObject();
            System.out.println("Cliente conectado al servidor => ID cliente: " + clientId);

            // Verificar si el juego continúa
            boolean gameContinues = !in.readObject().equals("El juego ha finalizado. No hay premios disponibles.");

            if (gameContinues) {
                while (true) {
                    // Solicitar al usuario las coordenadas
                    System.out.print("Ingrese la fila (0-2): ");
                    int row = scanner.nextInt();
                    System.out.print("Ingrese la columna (0-3): ");
                    int column = scanner.nextInt();

                    // Enviar las coordenadas al servidor
                    out.writeObject(new int[]{row, column});
                    out.flush();

                    // Recibir la respuesta del servidor
                    String response = (String) in.readObject();
                    System.out.println(response);

                    // Terminar si el juego ha finalizado
                    if (response.startsWith("Juego finalizado")) {
                        break;
                    }
                }
            } else {
                System.out.println("El juego ha finalizado. No hay premios disponibles.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
