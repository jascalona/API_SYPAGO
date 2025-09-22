package Bancrecer.API;

import java.io.IOException;
import static Bancrecer.API.Autenticacion.geyStsusReports;

public class Main {

    public static void main(String[] args) {

        String user = "jose";
        String apiKEY = "u4M6xxNzKxMZQLHMngu3MW1gWI4O3tY7";
        String apiUrl = "https://pruebas.sypago.net:8086/api/v1/auth/token";
        String client_id = "jose";

        try {
            System.out.println("Iniciando SYPAGO request...");
            String response = geyStsusReports(user, apiKEY, apiUrl, client_id);
            System.out.println("Request completed.");
            System.out.println("Respuesta del Servidor: " + response);
        } catch (IOException e) {
            System.err.println("Se produjo un error de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.err.println("Se produjo un error durante la solicitud HTTP: " + e.getMessage());
        }
    }
}
