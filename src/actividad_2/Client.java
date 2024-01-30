package actividad_2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PUERTO = 12345;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_IP, SERVER_PUERTO);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            // Recibir ID del cliente
            int clienteId = (int) in.readObject();
            System.out.println("Cliente conectado al servidor => ID cliente: " + clienteId);

            // Verificar si el juego continúa
            boolean continuar = !in.readObject().equals("El juego ha finalizado. No hay premios disponibles.");

            if (continuar) {
                while (true) {
                    // Solicitar al usuario las coordenadas
                    System.out.print("Ingrese la fila (0-2): ");
                    int fila = scanner.nextInt();
                    System.out.print("Ingrese la columna (0-3): ");
                    int columna = scanner.nextInt();

                    // Enviar las coordenadas al servidor
                    out.writeObject(new int[]{fila, columna});
                    out.flush();

                    // Recibir la respuesta del servidor
                    String respuesta = (String) in.readObject();
                    System.out.println(respuesta);

                    // Terminar si el juego ha finalizado
                    if (respuesta.startsWith("Juego finalizado")) {
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
