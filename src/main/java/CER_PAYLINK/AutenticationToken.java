package CER_PAYLINK;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AutenticationToken {

    public static String newToken() throws IOException {
        final String USER = "jose";
        final String API_KEY = "LlPhr4UguzAEIoAkuLJW0szJziuxt2o6";
        final String endpoint = "https://pruebas.sypago.net:8086/api/v1/auth/token";

        URL url = new URL(endpoint);
        String auth = USER + ";" + API_KEY;
        String encodeAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + encodeAuth);
        connection.setRequestProperty("Content-Type" ,"application/json");

        try(DataOutputStream os = new DataOutputStream(connection.getOutputStream())){

            String jsonInputString = "{\"client_id\": \"" + USER + "\", \"secret\": \"" + API_KEY + "\"}";
            os.writeBytes(jsonInputString);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        if (responseCode == 200){
            StringBuilder response = new StringBuilder();

            try (BufferedReader reader =new  BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
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
                System.out.println("Error: " + e.getMessage());
            }
            connection.disconnect();
            return errorResponse.toString();
        }
    }

    public static String mapperToken() throws IOException {
        String jsonString = AutenticationToken.newToken();

        try {
            ObjectMapper mapper = new ObjectMapper();
            //Parseo del json
            JsonNode rootNode = mapper.readTree(jsonString);
            //Obtener el nodo
            JsonNode access_token = rootNode.get("access_token");

            //Extraer el valor como String
            if (access_token != null){
                String accessToken = access_token.asText();
            }
            else {
                System.out.println("El Token no fue encontrado!");
            }
            return access_token.asText();

        }catch (Exception e){
            System.out.println("Error al extraer el Token: " + e.getMessage());
            return e.getMessage();
        }
    }

}
