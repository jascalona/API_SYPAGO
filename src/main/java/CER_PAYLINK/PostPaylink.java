package CER_PAYLINK;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.SimpleTimeZone;

public class PostPaylink
{
    private String internal_id;
    private String group_id;
    private final String API_URL_INIT = "https://pruebas.sypago.net:8086/api/v1/transaction/checkout";
    private final String URL_BLUEPRINT = "https://pruebas.sypago.net:8086/api/v1/transaction/paylink/blueprint";

    public PostPaylink(String internal_id, String group_id){
        if (internal_id == null || internal_id.trim().isEmpty()){
            System.out.println("Error al procesar el internal_id");
        }
        if (group_id == null || internal_id.trim().isEmpty()){
            System.out.println("Error al procesar el group_id");
        }
        this.internal_id = internal_id;
        this.group_id = group_id;
    }
    public String getInternal_id() {
        return this.internal_id;
    }

    public String postPaylink(String token) throws IOException {

        URL url = new URL(this.API_URL_INIT);

        HttpURLConnection connection = (HttpURLConnection)  url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        //Pasar el Token parea validacion
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");

        //Cuerpo de solicitud
        try(DataOutputStream os = new DataOutputStream(connection.getOutputStream())){
            //Datos
            // String internal_id = this.internal_id;
            // System.out.println("Desde el METODO:" + internal_id);
            String group_id = this.group_id;
            String bank_code = "0001";
            String type = "CNTA";
            String number = "00018349143957065141";
            String typeAmo = "ALMM";
            int amt = 1;
            String currency = "VES";
            int min_allow_amt = 1;
            int max_allow_amt= 1;
            boolean use_day_rate = false;
            String concept = "Cobro de Servicios";
            String sucessful_callback_url = "https://www.sypago.com/success";
            String failed_callback_url = "https://www.sypago.com/fail";
            String return_front_end_url = "https://www.sypago.com/return";
            String web_hook_endpoint = "https://www.sypago.com/notification";
            String name = "Cliente BanPlus Juridico";
            String typeDocument_info = "J";
            String numberR = "311845852";
            String bank_codeR = "0174";
            String typeR = "CELE";
            String numberCELE = "04129854529";
            int expiration = 300;
            //Modelo del JSON DE INICIO
            String jsonInputString = "{"
                    + "\"internal_id\":\"" + this.getInternal_id() + "\","
                    + "\"group_id\":\"" + group_id + "\","
                    + "\"account\":{"
                    + "\"bank_code\":\"" + bank_code + "\","
                    + "\"type\":\"" + type + "\","
                    + "\"number\":\"" + number + "\""
                    + "},"
                    + "\"amount\":{"
                    + "\"type\":\"" + typeAmo + "\","
                    + "\"amt\":" + amt + ","
                    + "\"currency\":\"" + currency + "\","
                    + "\"min_allow_amt\":" + min_allow_amt + ","
                    + "\"max_allow_amt\":" + max_allow_amt + ","
                    + "\"use_day_rate\":" + use_day_rate
                    + "},"
                    + "\"concept\":\"" + concept + "\","
                    + "\"notification_urls\":{"
                    + "\"sucessful_callback_url\":\"" + sucessful_callback_url + "\","
                    + "\"failed_callback_url\":\"" + failed_callback_url + "\","
                    + "\"return_front_end_url\":\"" + return_front_end_url + "\","
                    + "\"web_hook_endpoint\":\"" + web_hook_endpoint + "\""
                    + "},"
                    + "\"receiving_user\":{"
                    + "\"name\":\"" + name + "\","
                    + "\"document_info\":{"
                    + "\"type\":\"" + typeDocument_info + "\","
                    + "\"number\":\"" + numberR + "\""
                    + "},"
                    + "\"account\":{"
                    + "\"bank_code\":\"" + bank_codeR + "\","
                    + "\"type\":\"" + typeR + "\","
                    + "\"number\":\"" + numberCELE + "\""
                    + "}"
                    + "}"
                    //+ "\"expiration\":" + expiration
                    + "}";

            os.writeBytes(jsonInputString);
            os.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200){
                StringBuilder response =new StringBuilder();

                try (BufferedReader reader =new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                }
                connection.disconnect();
                StringBuilder id = new StringBuilder();
                //Atajar el JSON
                try {
                    ObjectMapper mapper =new ObjectMapper();
                    JsonNode rootNodo = mapper.readTree(response.toString());

                    JsonNode transaction_id = rootNodo.get("transaction_id");

                    if (transaction_id != null){
                        String transactionId = transaction_id.asText();
                        id.append(transaction_id.asText());
                    }
                    else {
                        System.out.println("No se genero el transaction ID");
                    }
                    return id.toString();

                } catch (Exception e){
                    System.out.println("Error al extraer el transaction_id: " + e.getMessage());
                    return "Error: " + e.getMessage();
                }

            }
            else {
                StringBuilder responseError = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                    String line;
                    while ((line = errorReader.readLine()) != null){
                        responseError.append(line);
                    }
                }
                catch (Exception e){
                    return "Error: " + e.getMessage();
                }
                connection.disconnect();
                return responseError.toString();
            }
        }
    }

    //Generate operation blueprint
    public String blueprint(String token) throws IOException {
        URL url = new URL(this.URL_BLUEPRINT);
        HttpURLConnection connection =(HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + token );
        connection.setRequestProperty("Content-Type", "application/json");

        try(DataOutputStream os = new DataOutputStream (connection.getOutputStream())){
            String internal_id = this.internal_id;
            String group_id = this.group_id;
            String bank_code = "0001";
            String type = "CNTA";
            String number = "00018349143957065141";
            String typeAmo = "ALMM";
            int amt = 1;
            String currency = "VES";
            int min_allow_amt = 1;
            int max_allow_amt = 10;
            boolean use_day_rate = false;
            String concept = "Cobro de Servicios";
            String sucessful_callback_url = "https://www.sypago.com/success";
            String failed_callback_url = "https://www.sypago.com/fail";
            String return_front_end_url = "https://www.sypago.com/return";
            String web_hook_endpoint = "https://www.sypago.com/notification";
            String name = "Cliente BanPlus Juridico";
            String typeDocument_info = "J";
            String numberR = "311845852";
            String bank_codeR = "0174";
            String typeR = "CELE";
            String numberCELE = "04129854529";
            int expiration = 300;

            String jsonString = "{" +
                    "  \"internal_id\": \"" + internal_id + "\"," +
                    "  \"group_id\": \"" + group_id + "\"," +
                    "  \"account\": {" +
                    "    \"bank_code\": \"" + bank_code + "\"," +
                    "    \"type\": \"" + type + "\"," +
                    "    \"number\": \"" + number + "\"" +
                    "  }," +
                    "  \"amount\": {" +
                    "    \"type\": \"" + typeAmo + "\"," +
                    "    \"amt\": " + amt + "," +
                    "    \"currency\": \"" + currency + "\"," +
                    "    \"min_allow_amt\": " + min_allow_amt + "," +
                    "    \"max_allow_amt\": " + max_allow_amt + "," +
                    "    \"use_day_rate\": " + use_day_rate +
                    "  }," +
                    "  \"concept\": \"" + concept + "\"," +
                    "  \"notification_urls\": {" +
                    "    \"sucessful_callback_url\": \"" + sucessful_callback_url + "\"," +
                    "    \"failed_callback_url\": \"" + failed_callback_url + "\"," +
                    "    \"return_front_end_url\": \"" + return_front_end_url + "\"," +
                    "    \"web_hook_endpoint\": \"" + web_hook_endpoint + "\"" +
                    "  }," +
                    "  \"receiving_user\": {" +
                    "    \"name\": \"" + name + "\"," +
                    "    \"document_info\": {" +
                    "      \"type\": \"" + typeDocument_info + "\"," +
                    "      \"number\": \"" + numberR + "\"" +
                    "    }," +
                    "    \"account\": {" +
                    "      \"bank_code\": \"" + bank_codeR + "\"," +
                    "      \"type\": \"" + typeR + "\"," +
                    "      \"number\": \"" + numberCELE + "\"" +
                    "    }" +
                    "  }," +
                    "  \"expiration\": " + expiration +
                    "}";
            os.writeBytes(jsonString);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200){
            StringBuilder response = new StringBuilder();
            try(BufferedReader reader =new BufferedReader (new InputStreamReader(connection.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
            }
            connection.disconnect();
            StringBuilder id = new StringBuilder();
            try {
                ObjectMapper mapper =new ObjectMapper();
                JsonNode rootNodo = mapper.readTree(response.toString());

                JsonNode blueprint_id = rootNodo.get("blueprint_id");

                if (blueprint_id != null){
                    String blueprintId = blueprint_id.asText();
                    id.append(blueprint_id.asText());
                }
                else {
                    System.out.println("No se genero el blueprint ID");
                }
                return id.toString();

            } catch (Exception e){
                System.out.println("Error al extraer el blueprint_id: " + e.getMessage());
                return "Error: " + e.getMessage();
            }


        }

        else{
            StringBuilder responseError = new StringBuilder();
            try(BufferedReader readerError =new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while ((line = readerError.readLine()) != null){
                    responseError.append(line);
                }
            }
            catch (Exception e){
                return e.getMessage();
            }
            connection.disconnect();
            return responseError.toString();
        }
    }

    //Solicitud del SesionId
    public String  obtain_sesionId(String token, String sesionURL) throws IOException {
        URL url =new URL(sesionURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        int responseCode = connection.getResponseCode();

        if (responseCode == 200){
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
            }
            connection.disconnect();

            String jsonString = response.toString();
            ObjectMapper mapper =new ObjectMapper();

            try {
                JsonNode rootNode = mapper.readTree(jsonString);
                JsonNode session_id = rootNode.get("session_id");
                if (session_id != null){
                    String id =  session_id.asText();
                }
                else {
                    System.out.println("El session_id no pudo ser generado!");
                }
                return session_id.asText();
            }
            catch (Exception e){
                System.out.println("Error: " + e.getMessage());
                return e.getMessage();
            }
        }
        else{
            StringBuilder responseError = new StringBuilder();

            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while ((line = errorReader.readLine()) != null){
                    responseError.append(line);
                }
            }catch (Exception e){
                return "Error: " + e.getMessage();
            }
            connection.disconnect();
            return responseError.toString();
        }

    }

    //Funcion para solicitar el estado del PayLink
    public String getPaylink(String token, String apiURL) throws IOException{
        URL url = new URL(apiURL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        int responseCode = connection.getResponseCode();
        //System.out.println("Response Code: " + responseCode);

        if (responseCode == 200){
            StringBuffer response = new StringBuffer();
            try (BufferedReader reader =new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
            }
            connection.disconnect();
            //Atajamos el Status y transaction_id
            StringBuilder transaccion =new StringBuilder();
            try {
                ObjectMapper mapper =new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.toString());
                JsonNode transactionId = rootNode.get("transaction_id");
                JsonNode statusNode = rootNode.get("status");

                if (transactionId != null || statusNode != null){
                    transaccion.append(transactionId.asText() + " | " + statusNode.asText());
                }
                else {
                    System.out.println("No se encontraron los nodos");
                }
            }catch (Exception e){
                System.out.println("Error: " + e.getMessage());
                return "Error de retorno: " + e.getMessage();
            }
            return transaccion.toString();
        }
        else {
            StringBuilder errorResponse = new StringBuilder();

            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while ((line = errorReader.readLine())!= null){
                    errorResponse.append(line);
                }
            }
            catch (Exception e){
                return "Error: " + e.getMessage();
            }
            connection.disconnect();
            return errorResponse.toString();
        }
    }
}
