package Bancaribe.API;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
public class Autenticacion {

    /**
     * Sends an HTTP POST request to the SYPAGO gateway API.
     *
     * @param username userName
     * @param password password for Basic Authentication.
     * @param apiUrl The URL of the API endpoint.
     * @param TransactionId The JSON string to send in the request body.
     * @return The server's response body as a String if the request is
     * successful (HTTP 202 Accepted).
     * @throws IOException If an I/O error occurs during the connection or data
     * transfer.
     * @throws RuntimeException If the HTTP response code indicates an error
     * (not 202 Accepted).
     */
    public static String geyStsusReports(String username, String password, String apiUrl, String TransactionId) throws IOException {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
        connection.setRequestProperty("Content-Type", "application/json");

        // Envio de datos JSON

        try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
            os.writeBytes("{\"PmtStsReq\": {\"TransactionId\": \"" + TransactionId + "\",\"LclInstrm\": \"01\",\"Purp\": \"string\",\"SendingBankCode\": \"\"}}");
            os.flush();
        }

        int responseCode = connection.getResponseCode();  //Obtener el codigo de estado HTTP
        System.out.println("Response Code : " + responseCode); // para depurar

        if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            connection.disconnect();
            return response.toString();
        } else {
            // Read error stream if available for more details on the error
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
            } catch (Exception e) {
                // Ignore if error stream is not available
            }
            connection.disconnect();
            throw new RuntimeException("HTTP request failed with code " + responseCode + ". Error details: " + errorResponse.toString());
        }
    }

    public static void main(String[] args) {
        String user = "sycom_gateway_user";
        String password = "3b17-0e38206Pae-88_@fb8476dff92f271A7C";
        String apiUrl = "http://10.0.62.20:8086/sypago/gateway/api/v1/solest";
        String TransactionId = "11111111111";

        try {
            System.out.println("Initiating SYPAGO request...");
            String response = geyStsusReports(user, password, apiUrl, TransactionId);
            System.out.println("Request completed.");
            System.out.println("Server Response: " + response);
        } catch (IOException e) {
            System.err.println("An I/O error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.err.println("An error occurred during the HTTP request: " + e.getMessage());
        }
    }

}

