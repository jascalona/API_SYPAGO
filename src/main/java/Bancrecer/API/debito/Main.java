package Bancrecer.API.debito;
import java.io.IOException;
import static Bancrecer.API.debito.StatusDomiciliacion.getDom;

public class Main {

    public static void main(String[] args) {
        String token = "Barer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmZXpQcl9HSWhIZ05jOVc1cU5Td2FIQXBRMVRqeUlqbWtpY0d5V1hHUjFzIn0.eyJleHAiOjE3NTg2NjczNjgsImlhdCI6MTc1ODYzMTM2OCwianRpIjoiYjRkN2NmNTktNmRlOC00ZTZkLWEwMTctZjBmYWQ0OGQxZmViIiwiaXNzIjoiaHR0cHM6Ly9wcnVlYmFzLnN5cGFnby5uZXQ6ODA4MS9yZWFsbXMvc3lwYWdvIiwic3ViIjoiODIzOWZjMmQtMjllNi00NzQ4LWE0ZGItMDJlZjUwOTBmYjg2IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiam9zZSIsInNjb3BlIjoic3lwYWdvX2FwaV9rZXlfc2NvcGU6NTVhNGVjMzktNDI0Zi00NDIzLWI5MTgtYjgxMWZkMDQ3OTk2LlVzZXIiLCJjbGllbnRIb3N0IjoiMTcyLjIwLjAuMSIsImNsaWVudEFkZHJlc3MiOiIxNzIuMjAuMC4xIiwiY2xpZW50X2lkIjoiam9zZSJ9.jrTg28VeJp88FIA9bjP52qYR0XWbFmy0PUyuZLvRY5eFDcH3b6prOKH3YpZy-IP1CeUwGAMU0Kzvy8GvOycHfbHZKFKz9aBUesSrszX72m1hCmaYi5DdYRGlx_18-RcA3N0_Tne_FPLMwvKOYwjg_wxUg7SzQPTelCxsq9AGCH30fX2C04xKnMlflPMcdJBrOOG4FzoFGew8B8vgvZegQZb4FOJ6jn5S_fq0Qv0DN-Z5XTx71_MeM_tw9axDyvv2in2jnIdJ32Dc4Stamlb6Vb-99MMX7vK1hAXBeLISoUJcAdR3YB4gApHksOXS3nXM_yA6lWAXBQvMaMrW49IawQ";
        String transaction_id = "DA6CB42C272A";
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
