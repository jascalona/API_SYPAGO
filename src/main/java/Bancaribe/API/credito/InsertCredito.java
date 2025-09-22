package Bancaribe.API.credito;
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

public class InsertCredito {
    public static String geyStatusReport(String Token, String apiUrl, String internal_id)  throws IOException {

        URL url = new URL(apiUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        // Correcto: se envía el token directamente
        connection.setRequestProperty("Authorization", "Bearer " + Token);
        connection.setRequestProperty("Content-Type" ,"application/json");

        // Construir el cuerpo de la solicitud JSON
        try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {

            // 1. Simula la generación de datos dinámicos
            String groupId = "7g8h9i0j1k2l";
            double amt = 1;
            String bancoCode = "0001";
            String cuentaNumero = "00018349143957065141";
            String nombreDestinatario = "Cliente BanPlus Juridico";
            String docTipoDestinatario = "J";
            String docNumeroDestinatario = "311845852";
            String bancoDestinatario = "0174";
            String tlfDestinatario = "04129854529";

            // 2. Construye el JSON usando concatenación de cadenas
            String jsonInputString = "{"
                    + "\"internal_id\": \"" + internal_id + "\","
                    + "\"group_id\": \"" + groupId + "\","
                    + "\"account\": {"
                    + "\"bank_code\": \"" + bancoCode + "\","
                    + "\"type\": \"CNTA\","
                    + "\"number\": \"" + cuentaNumero + "\""
                    + "},"
                    + "\"sub_product\": \"220\","
                    + "\"amount\": {"
                    + "\"amt\": " + amt + ","
                    + "\"currency\": \"VES\","
                    + "\"use_day_rate\": false"
                    + "},"
                    + "\"concept\": \"Pago de servicios\","
                    + "\"notification_urls\": {"
                    + "\"web_hook_endpoint\": \"https://www.sypago.com/notification\""
                    + "},"
                    + "\"receiving_user\": {"
                    + "\"name\": \"" + nombreDestinatario + "\","
                    + "\"document_info\": {"
                    + "\"type\": \"" + docTipoDestinatario + "\","
                    + "\"number\": \"" + docNumeroDestinatario + "\""
                    + "},"
                    + "\"account\": {"
                    + "\"bank_code\": \"" + bancoDestinatario + "\","
                    + "\"type\": \"CELE\","
                    + "\"number\": \"" + tlfDestinatario + "\""
                    + "}"
                    + "}"
                    + "}";

            os.writeBytes(jsonInputString);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        // Logica corregida para leer del InputStream en respuestas exitosas ademas de agregar (HTTP_OK) para esperar status code 200 ajustado para casos API
        if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED){
            StringBuilder response = new StringBuilder();

            try (BufferedReader reader =new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line;
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
            }
            connection.disconnect();
            return response.toString();
        }
        //Logica para leer el ErrorStram en respuesta de error
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
            return errorResponse.toString();
            //throw new RuntimeException("La solicitud HTTP falló con el código: " + responseCode + ". Detalles de Error: "  + errorResponse.toString());
        }
    }

    public static void main(String [] args){

        String Token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmZXpQcl9HSWhIZ05jOVc1cU5Td2FIQXBRMVRqeUlqbWtpY0d5V1hHUjFzIn0.eyJleHAiOjE3NTU4MTY0NzUsImlhdCI6MTc1NTc4MDQ3NSwianRpIjoiNmI0YmM2YjQtOGM1Yy00ODIwLWJiMzgtODg1NTIzZTBjN2U1IiwiaXNzIjoiaHR0cHM6Ly9wcnVlYmFzLnN5cGFnby5uZXQ6ODA4MS9yZWFsbXMvc3lwYWdvIiwic3ViIjoiNTc1NTk1YmItNTMyMy00MjkyLWI2NzktNWZiZDEzNmVmN2EzIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiam9zZSIsInNjb3BlIjoic3lwYWdvX2FwaV9rZXlfc2NvcGU6NTVhNGVjMzktNDI0Zi00NDIzLWI5MTgtYjgxMWZkMDQ3OTk2LlVzZXIiLCJjbGllbnRIb3N0IjoiMTcyLjIwLjAuMSIsImNsaWVudEFkZHJlc3MiOiIxNzIuMjAuMC4xIiwiY2xpZW50X2lkIjoiam9zZSJ9.HQINZrzxW62KF44nv35SKstfEsmO_gWPM6hHAjgphmFZvXnAoVV_HJ0T14EHhTDClQCb6iWx-BlUpy9mBCM6ymAQLmz8qzwf9pkKYa20qpgRtA2ZvnydZaNZPTIADgqSnnHl7ZR_wTnML6g41MpSW-3PNnGvife0KxrQ8rbsiaRcCz_GTUiqFMnfrgA4KoCmQYrOS_n0z7_AnuMmbJ2GEwv7-nxlutigwx8Cb5Ny9UPYGYw6Ya1_V_yachtHXAMFTHpUnYVn39lZoDC4VRwRnZ9LzmZNRM5oMWzy5AQBnu3TG_Oiy_hQHH4AXrNB20F72_NyVaGKSmbgElUxf_fjPw";
        String urlAPI = "https://pruebas.sypago.net:8086/api/v1/transaction/credit";
        String internal_id = "";

        try{
            System.out.println("Iniciando Credito SYPAGO....");
            String response = geyStatusReport(Token, urlAPI, internal_id);
            System.out.println("Request Completed" );
            System.out.println("Respuesta del Servidor: " + response);
        } catch (IOException e) {
            System.out.println("Se produjo un error de I/O" + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e){
            System.out.println("Se produjo un error durante la solicitud HTTP: " + e.getMessage());
        }
    }
}
