package CER_PAYLINK.RELEASE_NOTE;
import CER_PAYLINK.AutenticationToken;
import CER_PAYLINK.PostPaylink;
import Utiliti.LabelTransacionID;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class UpdateTransactions {

    private final String API_URL = "https://pruebas.sypago.net:8086/api/v1/transactions/update";
    private  final String API_URL_BLUEPRINT = "https://pruebas.sypago.net:8086/api/v1/transactions/blueprint/update";

    private ArrayList transaction_id;

    public UpdateTransactions(ArrayList transaction_id){
        if (transaction_id == null || transaction_id.isEmpty()){
            System.out.println("Error al procesar la lista de IDs");
        }
        this.transaction_id = transaction_id;
    }

    //Cnstructor que recibe el blueprint_id

    public  String updateTransaction(String token) throws IOException {
        URL url = new URL(this.API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");

        try(DataOutputStream os = new DataOutputStream(connection.getOutputStream())){
            ArrayList<String> blueprint_id = this.transaction_id;
            String filter_type = "blueprint_id";

            String idJsonArrayContent = blueprint_id.stream()
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(","));
            String idJsonArray = "[" + idJsonArrayContent + "]";

            String jsonInputString = "{"
                    + "\"ids\":" + idJsonArray + ","
                    + "\"filter_type\":\"" + "transaction_id" + "\","
                    + "\"new_data\":{"
                    + "\"amount\":{"
                    + "\"type\":\"" + "NONE" + "\","
                    + "\"amt\":" + 25639.75 + ","
                    + "\"currency\":\"" + "VES" + "\","
                    + "\"min_allow_amt\":" + 0 + ","
                    + "\"max_allow_amt\":" + 0 + ","
                    + "\"use_day_rate\":" + false
                    + "},"
                    + "\"receiving_user\":{"
                    + "\"name\":\"" + "Nuevo Nombre" + "\","
                    + "\"document_info\":{"
                    + "\"type\":\"" + "V" + "\","
                    + "\"number\":\"" + "123456789" + "\""
                    + "},"
                    + "\"account\":{"
                    + "\"bank_code\":\"" + "0172" + "\","
                    + "\"type\":\"" + "CELE" + "\","
                    + "\"number\":\"" + "04140121877" + "\""
                    + "}"
                    + "},"
                    + "\"expiration\":" + 3600 + "," // Numérico
                    + "\"concept\":\"" + "Pago actualizado por corrección de datos - nuevo concepto" + "\","
                    + "\"notification_urls\":{"
                    + "\"sucessful_callback_url\":\"" + "https://new-success.net/success" + "\","
                    + "\"failed_callback_url\":\"" + "https://new-fail.com/failure" + "\","
                    + "\"return_front_end_url\":\"" + "https://new-frontend.org/return" + "\","
                    + "\"web_hook_endpoint\":\"" + "https://new-endpoint.io/notify" + "\""
                    + "}"
                    + "}"
                    + "}";


                os.writeBytes(jsonInputString);
                os.flush();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == 200){
            StringBuilder response  =new StringBuilder();
            try(BufferedReader reader =new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
            }
            connection.disconnect();
            return response.toString();
        }
        else{
            StringBuilder responseError =new StringBuilder();
            try(BufferedReader readerError =new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while((line = readerError.readLine()) != null){
                    responseError.append(line);
                }
            }
            connection.disconnect();
            return responseError.toString();
        }
    }


    //Update operation Blueprint
    public String updateBlue(String token) throws IOException {
        URL url =new URL(this.API_URL_BLUEPRINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");

        try(DataOutputStream os = new DataOutputStream(connection.getOutputStream())){
            String blueprint_id = "BCE45616FF00";
            String filter_type = "blueprint_id";

            String jsonString = "{"
                    + "\"ids\":\"" + blueprint_id + "\"," // Solución: Añadir las comillas dobles
                    + "\"filter_type\":\"" + "blueprint_id" + "\","
                    + "\"new_data\":{"
                    + "\"amount\":{"
                    + "\"type\":\"" + "NONE" + "\","
                    + "\"amt\":" + 25639.75 + ","
                    + "\"currency\":\"" + "VES" + "\","
                    + "\"min_allow_amt\":" + 0 + ","
                    + "\"max_allow_amt\":" + 0 + ","
                    + "\"use_day_rate\":" + false
                    + "},"
                    + "\"receiving_user\":{"
                    + "\"name\":\"" + "Nuevo Nombre" + "\","
                    + "\"document_info\":{"
                    + "\"type\":\"" + "V" + "\","
                    + "\"number\":\"" + "123456789" + "\""
                    + "},"
                    + "\"account\":{"
                    + "\"bank_code\":\"" + "0172" + "\","
                    + "\"type\":\"" + "CELE" + "\","
                    + "\"number\":\"" + "04140121877" + "\""
                    + "}"
                    + "},"
                    + "\"expiration\":" + 3600 + ","
                    + "\"concept\":\"" + "Pago actualizado por corrección de datos - nuevo concepto" + "\","
                    + "\"notification_urls\":{"
                    + "\"sucessful_callback_url\":\"" + "https://new-success.net/success" + "\","
                    + "\"failed_callback_url\":\"" + "https://new-fail.com/failure" + "\","
                    + "\"return_front_end_url\":\"" + "https://new-frontend.org/return" + "\","
                    + "\"web_hook_endpoint\":\"" + "https://new-endpoint.io/notify" + "\""
                    + "}"
                    + "}"
                    + "}";

            os.writeBytes(jsonString);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 200){
            StringBuilder response =new StringBuilder();
            try(BufferedReader reader =new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
                    response.append(line);
                }
                connection.disconnect();
                return response.toString();
            }
        }
        else {
            StringBuilder responseError =new StringBuilder();
            try(BufferedReader readerError =new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while ((line = readerError.readLine()) != null){
                    responseError.append(line);
                }
            } catch (Exception e){
                return e.getMessage();
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
        ArrayList operations_n =new ArrayList();
        for (int i=0; i <1; i++){
            String generate = generate_operation.postPaylink(token);
            operations_n.add(generate);
        }

        PostPaylink generate_bluePrint =new PostPaylink(internal_id, group_id);
        StringBuilder operation_b =new StringBuilder();

        String generate = generate_operation.postPaylink(token);
        UpdateTransactions update_operation = new UpdateTransactions(operations_n);
        System.out.println("---Response Server Endpoint Update---");
        System.out.println(update_operation.updateTransaction(token));

        //Response for update blueprint (Revisar mas tarde el fallo de envio)
    }

}
