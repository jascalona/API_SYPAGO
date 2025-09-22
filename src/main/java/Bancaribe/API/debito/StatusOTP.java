package Bancaribe.API.debito;
import javax.swing.plaf.synth.SynthOptionPaneUI;
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
public class StatusOTP {

    public static String otpStatusReport(String Token, String apiUrl, String internal_id) throws IOException{


        URL url =new URL(apiUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + Token);
        connection.setRequestProperty("Content-Type", "application/json");

        //Contruir el cuerpo de la solicitud JSON
        try(DataOutputStream os = new DataOutputStream(connection.getOutputStream())){
            String jsonInputString = "{\"PmtStsReq\": {\"TransactionId\": \"\" + TransactionId + \"\",\"LclInstrm\": \"01\",\"Purp\": \"string\",\"SendingBankCode\": \"\"}}";
            os.writeBytes(jsonInputString);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED ){
            StringBuilder response = new StringBuilder();

            try(BufferedReader reader = new BufferedReader (new InputStreamReader(connection.getInputStream()))) {
                String line;
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
            }
            connection.disconnect();
            return response.toString();
        }

        else {
            StringBuilder errorResponse = new StringBuilder();

            try(BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream())) ){
                String line;
                while((line = errorReader.readLine()) != null){
                    errorResponse.append(line);
                }
            }
            catch (Exception e){
                //Si el flujo de error no esta disponible, se ignora
            }
            connection.disconnect();
            throw new RuntimeException("La solicitud HTTP falló con el código: " + responseCode + ". Detalles de Error: "  + errorResponse.toString());
        }
    }

    public static void main(String [] args){
        String Token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmZXpQcl9HSWhIZ05jOVc1cU5Td2FIQXBRMVRqeUlqbWtpY0d5V1hHUjFzIn0.eyJleHAiOjE3NTQ5NTY2MzcsImlhdCI6MTc1NDkyMDYzNywianRpIjoiZGIzNjE5OWYtOWEyMC00NTI1LWJiZGQtZGRlNjQwNmEzOWY1IiwiaXNzIjoiaHR0cHM6Ly9wcnVlYmFzLnN5cGFnby5uZXQ6ODA4MS9yZWFsbXMvc3lwYWdvIiwic3ViIjoiMTA0ZjM0YTctZGVlMS00NGI0LTk1MWEtNWViYjU2MmI4MDA5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiam9zZSIsInNjb3BlIjoic3lwYWdvX2FwaV9rZXlfc2NvcGU6NTVhNGVjMzktNDI0Zi00NDIzLWI5MTgtYjgxMWZkMDQ3OTk2LlVzZXIiLCJjbGllbnRIb3N0IjoiMTcyLjIwLjAuMSIsImNsaWVudEFkZHJlc3MiOiIxNzIuMjAuMC4xIiwiY2xpZW50X2lkIjoiam9zZSJ9.sUI26trnGUkWiM6GE94kReRQtBI4C0-4hqEaVlDcKephbo80CTn3w6lVkX8bEwNDtL2xGILI3b29d_-8A_8lMKhB9PZoTuWEekW6mG5i8dyQ0WDm0zv5JRfnfQTIsJs-MlHRS6NNR9Y9SosGGxhQrUrn5dUHBowhJuoLJP_i6OLEZ6fGPtw90lKICzZdnllezSNMri86hv_Vgyju6951c1ukZrb7nr79dEe7dhZigmfo7mzaKzy5bTXHg2aFIdikjrkKbwgnffYuNhUW1bdqFq1Z2aK2-dPSd-KVndZktf0rD_TIGMf5rzIwxDLNEBy_4bxgn0lwFobzGLOpNNnCXA";
        String urlAPI = "https://pruebas.sypago.net:8086/api/v1/transaction/credit";
        String internal_id = "B30E237C80F2";

        try {
            System.out.println("Iniciando conexion Transaction OTP...");
            String respose = otpStatusReport(Token, urlAPI, internal_id);
            System.out.println("Respuesta Completada");
            System.out.println("Respuesta del servidor: " + respose);
        } catch (IOException e){
            System.out.println("Se produjo un error de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.out.println("Se produjo un error durante la solicitud HTTP: " + e.getMessage());
        }

    }
}
