package Bancrecer.GatewayV2;
import Labels.TransactionIdGV2; // Se asume que esta clase existe y funciona correctamente
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class GV2 {
    // Bloque estático que se ejecuta una sola vez al cargar la clase
    static {
        try {
            //Crear un TrustManager que no valide los certificados.
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // No hace nada, acepta todos los clientes
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // No hace nada, acepta todos los servidores
                        }
                    }
            };

            //Inicializar el SSLContext con el TrustManager que ignora la validación.
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            //  Aplicar la configuración de fábrica de sockets SSL por defecto.
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            //Desactivar la verificación del nombre de host.
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Siempre devuelve true, no verifica el nombre de host
                }
            });
        } catch (Exception e) {
            // Manejar la excepción si algo sale mal durante la inicialización
            System.err.println("Error al inicializar la configuración SSL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía una solicitud GET a la URL de la API especificada para recuperar informes de estado
     * de transacciones. Este metodo está diseñado para realizar una solicitud GET y espera
     * una respuesta JSON. Maneja la omisión del certificado SSL y la verificación del nombre de host
     * para entornos de desarrollo/pruebas.
     *
     * @param username El nombre de usuario para la autenticación básica.
     * @param password La contraseña para la autenticación básica.
     * @param baseUrl La URL base del endpoint de la API (por ejemplo, "https://10.0.62.20:8088/api/v1/transaction/").
     * @param transactionId El ID de la transacción que se incluirá en los parámetros de la URL.
     * @return Un String que contiene la respuesta JSON del servidor si la solicitud es exitosa (HTTP 200 OK).
     * @throws IOException Si ocurre un error de E/S durante la solicitud HTTP.
     * @throws RuntimeException Si la solicitud HTTP falla (código de respuesta no 200).
     */

    public static String getStatusReports(String username, String password, String baseUrl, String transactionId) throws IOException{
        String  auth = username + ";" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());


        //Construir la URL completa con los parametros  de consulta para una solicitud GET
        //Ejmplo /api/v1/transaction
        String fullUrl = baseUrl + "?is_send_transaction=true&transaction_id=" + transactionId;

        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        // Para solicitudes GET, setDoOutput debe ser false o no establecerse,
        // ya que no hay un cuerpo de solicitud.
        connection.setDoOutput(false); // Establecer explícitamente en false para una solicitud GET limpia

        connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
        connection.setRequestProperty("Content-Type", "application/json"); // Buena práctica para indicar el tipo de contenido aceptado

        // No hay cuerpo de solicitud para una solicitud GET, por lo tanto, se elimina el bloque DataOutputStream.
        // El código original incluía un DataOutputStream, lo cual es incorrecto para GET.

        int responseCode = connection.getResponseCode();
        System.out.println("Código de Respuesta: " + responseCode);

        //Para una solicirud get Exitosa, Codigo 200
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } finally {
                //Cerrar la conexion
                connection.disconnect();
            }
            return response.toString();
        } else{
            StringBuilder errorResponse =new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                String line;
                while ((line = errorReader.readLine()) != null){
                    errorResponse.append(line);
                }
            }catch (Exception e){
                //Ignorar errores al leer el stran de error si ya es un estado de error
                return e.getMessage();
            }
            finally {
                //Cerrar la conexion
                connection.disconnect();
            }
            return errorResponse.toString();
        }

        }

        //Validar la respuesta
        public static void main(String[] args) {
            String username = "sygateway_user";
            String password = "sB5I2lRO5Jxh1ia47S7KvnyLR";
            String transactionId = "5C8E0A0098FE";

            //URL Base para el Endpoint
            String baseUrl = "https://pruebas.app.sypago.net:9095/sygateway/bancrecer/api/v1/transaction";
            //La URL  se construye dentro del meotodo getStatusReports


            try{
                System.out.println("Solicitud de Estado a GV2");
                //LLamar al metodo modificado
                String response = getStatusReports(username, password, baseUrl, transactionId);
                System.out.println("Solicitud completada");
                System.out.println("Respuesta del servidor: " + response);

            } catch (IOException e) {
                System.err.println("Se produjo un error de E/S: " + e.getMessage());
                e.printStackTrace();
            } catch (RuntimeException e) {
                System.err.println("Se produjo un error durante la solicitud HTTP: " + e.getMessage());
            }

        }


    }



