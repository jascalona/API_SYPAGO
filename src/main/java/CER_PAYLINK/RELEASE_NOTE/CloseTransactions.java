package CER_PAYLINK.RELEASE_NOTE;
import CER_PAYLINK.AutenticationToken;
import CER_PAYLINK.PostPaylink;
import Utiliti.LabelTransacionID;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class CloseTransactions {

    private ArrayList transaction_id;
    private final String API_URL = "https://pruebas.sypago.net:8086/api/v1/transactions/cancel";

    public CloseTransactions(ArrayList transaction_id){
        if (transaction_id == null || transaction_id.isEmpty()){
            System.out.println("Error al procesar la lista de IDs");
        }
        this.transaction_id = transaction_id;
    }


    public String closeTransaction(String token) throws IOException {
        URL url = new URL(this.API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");

        try(DataOutputStream os = new DataOutputStream(connection.getOutputStream())){
            //Envio de datos
            ArrayList<String> transacion_id = this.transaction_id;
            String filter_type = "transaction_id";

            String idsJsonArrayContent = transacion_id.stream()
                    .map(s -> "\"" + s + "\"") // Agrega comillas dobles a cada elemento
                    .collect(java.util.stream.Collectors.joining(","));

            String idsJsonArray = "[" + idsJsonArrayContent + "]";

            String jsonInputString = "{"
                    + "\"ids\":" + idsJsonArray + ","
                    + "\"filter_type\":\"" + filter_type + "\""
                    + "}";
            os.writeBytes(jsonInputString);
            os.flush();
        }
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
            return response.toString();
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
                return "Error: agsafs " + e.getMessage();
            }
            connection.disconnect();
            return responseError.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        String token = AutenticationToken.mapperToken();
        String internal_id = LabelTransacionID.UIDD(12);
        String group_id = LabelTransacionID.UIDD(12);

        PostPaylink generate_operation =new  PostPaylink(internal_id, group_id);
        ArrayList operations =new ArrayList();

        int contador = 5;
        for (int i=0; i < contador; i++){
            String generate = generate_operation.postPaylink(token);
            operations.add(generate);
        }
        //System.out.println(operations);
        CloseTransactions constructor =new CloseTransactions(operations);
        System.out.println("---Response Server Endpoint Cancel---");
        System.out.println(constructor.closeTransaction(token));
    }

}
