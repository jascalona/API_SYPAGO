package Bancrecer.API.debito;
import java.io.IOException;
import static Bancrecer.API.debito.StatusDomiciliacion.getDom;

public class Main {

    public static void main(String[] args) {
        String token = "Barer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmZXpQcl9HSWhIZ05jOVc1cU5Td2FIQXBRMVRqeUlqbWtpY0d5V1hHUjFzIn0.eyJleHAiOjE3NTg1OTk4MjEsImlhdCI6MTc1ODU2MzgyMSwianRpIjoiMzg3NmY4MjYtNDRlMy00MjMxLTk4ODAtZThjNDk4NDhiOWM2IiwiaXNzIjoiaHR0cHM6Ly9wcnVlYmFzLnN5cGFnby5uZXQ6ODA4MS9yZWFsbXMvc3lwYWdvIiwic3ViIjoiMjYwYzU0YjYtZGU1Mi00NmQ4LWEyYWQtMWIyOGEwYTQ1MzA3IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiam9zZSIsInNjb3BlIjoic3lwYWdvX2FwaV9rZXlfc2NvcGU6NTVhNGVjMzktNDI0Zi00NDIzLWI5MTgtYjgxMWZkMDQ3OTk2LlVzZXIiLCJjbGllbnRIb3N0IjoiMTcyLjIwLjAuMSIsImNsaWVudEFkZHJlc3MiOiIxNzIuMjAuMC4xIiwiY2xpZW50X2lkIjoiam9zZSJ9.dYfnNivO7l3V4JXXG3ojvT09yw6HEgXImTtEISY_pD6V47P_M7NjNh6Z0lc4hFA7X-NaH3_K6GoJgUs6AAZMxsDJIse_YV6iY7jHpo9TVXa-eVTUJZq-Nx7_I9ub1ea1H-fkBw22NB72sJyPAXp-TXtaFQvGb46_pbsOTWqVrTZQIYbnP9-vZ9CA6xyUe3NmMhiwwT4qEKikF0dI5G2UFv2INPzpuIzaRpbGfGhJ0CoGjY6lBd3x1tB5Gk1u_pbWz1wABAgqXEtbwBmLkIe8ZAivI6qvQyVHhxIq5oARoojBYQR28LzGKRFrNJFzQck8fWalWxnk1zOzZeGa9_hGBg";
        String transaction_id = "11111111";
        String apiUrl = "https://pruebas.sypago.net:8086/api/v1/transaction/" + transaction_id;

        String tes = "01058349143957777777";
        System.out.println(tes.length());
        try{
            System.out.println("Iniciando API Domiciliacion...");
            String response = getDom(token, apiUrl);
            System.out.println("\nRespuesta del servidor: " + response);

        } catch (IOException e){
            System.out.println("Ha ocurrido un error de E/S: " + e.getMessage());
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            System.out.println("Se produjo un error durante la solicitud HTTP: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
