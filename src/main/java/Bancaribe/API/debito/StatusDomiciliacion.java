package Bancaribe.API.debito;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatusDomiciliacion {

    public static String getDom(String token, String urlAPI) throws IOException {

        URL url =new URL(urlAPI);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", token);
        connection.setRequestProperty("Content-Type", "aplication/json");

        //Construccion del cuerpo de la solicitud JSON
        int responseCode = connection.getResponseCode();
        System.out.println("Codigo de respuesta: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK){

            StringBuilder responde =new StringBuilder();

            try(BufferedReader reader =new BufferedReader(new InputStreamReader(connection.getInputStream()))){

                String line;
                while((line = reader.readLine()) != null){
                    responde.append(line);
                }
                connection.disconnect();
                return responde.toString();
            }

        } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
            StringBuilder conflictResquest =new StringBuilder();

            try(BufferedReader conflitReader =new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while((line = conflitReader.readLine()) != null){
                    conflictResquest.append(line);
                }
            }
            connection.disconnect();
            return conflictResquest.toString();

        }

        else {
            StringBuilder errorResponse =new StringBuilder();
            try(BufferedReader errorReader= new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }

            } catch (Exception e){
                System.out.println("Se esta Quedando en la exception con un: " + responseCode);
            }

            connection.disconnect();
            return errorResponse.toString();

        }

    }
}
