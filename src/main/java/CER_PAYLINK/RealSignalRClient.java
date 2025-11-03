package CER_PAYLINK;

import Utiliti.LabelTransacionID;

import java.io.IOException;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CLASE: RealSignalRClient
 * Implementación basada en WebSocket puro (java.net.http) para la conexión con skipNegotiation: true
 * al Hub: /CheckoutHub.
 * Flujo: Conexión -> Handshake (RSA-OAEP) -> GetTransaction (AES-GCM)
 */
public class RealSignalRClient implements Runnable {

    // URL y Configuración CRÍTICA
    private static final String WSS_URL_BASE = "wss://pruebas.app.sypago.net:8086/CheckoutHub";
    private static final String ALGORITHM_RSA_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private final String sessionId;
    private KeyPair rsaKeyPair;
    private SecretKey symmetricKey;
    private RealWebSocketClient wsClient;

    public RealSignalRClient(String sessionId) {
        this.sessionId = sessionId;
    }

    // --------------------------------------------------------------------------------------------------
    //                                  FLUJO PRINCIPAL
    // --------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        System.out.println("--- Cliente REAL Iniciado para Sesión: " + sessionId + " ---");
        try {
            // 4.1. Paso 1: Generación de Llaves Criptográficas (RSA-OAEP 2048)
            step1_generateRsaKeys();

            // 4.2. Paso 2: Establecimiento de Conexión WebSocket (Pura a /CheckoutHub)
            wsClient = step2_establishConnection();

            // 4.3. Paso 3: Intercambio Seguro de Claves (RSA & AES)
            step3_secureKeyExchange();

            // 4.4. Paso 4: Obtención de Datos de Transacción (GetTransaction)
            step4_getTransactionData();

        } catch (Exception e) {
            System.err.println("Error FATAL en cliente " + sessionId + ": " + e.getMessage());
            // El printStackTrace es clave para ver la causa real, especialmente si es ExecutionException
            e.printStackTrace();
        } finally {
            if (wsClient != null) {
                // Cierre de la conexión WebSocket
                wsClient.close();
            }
            System.out.println("--- Cliente REAL Finalizado para Sesión: " + sessionId + " ---");
        }
    }

    // --------------------------------------------------------------------------------------------------
    //                         PASOS DEL FLUJO
    // --------------------------------------------------------------------------------------------------

    private void step1_generateRsaKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.rsaKeyPair = keyGen.generateKeyPair();
            System.out.println("Llaves RSA-OAEP 2048 bits generadas.");
        } catch (Exception e) {
            throw new RuntimeException("Error al generar llaves RSA.", e);
        }
    }

    private String getPublicKeyPem() {
        PublicKey pubKey = rsaKeyPair.getPublic();
        String base64 = Base64.getEncoder().encodeToString(pubKey.getEncoded());

        // Empaquetar en formato PEM (SPKI) requerido
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64.substring(i, Math.min(i + 64, base64.length())));
            pem.append("\n");
        }
        pem.append("-----END PUBLIC KEY-----\n");
        return pem.toString();
    }

    private RealWebSocketClient step2_establishConnection() throws Exception {
        String wssUrl = WSS_URL_BASE + "?sessionId=" + sessionId;

        System.out.println("\n--- Cliente " + sessionId.substring(0, 8) + ": Paso 2: Conexión WebSocket Pura ---");
        System.out.println("URL Hub (skipNegotiation): " + wssUrl);

        RealWebSocketClient client = new RealWebSocketClient(sessionId);
        System.out.println("Iniciando conexión WSS y Handshake...");
        client.connect(wssUrl);

        if (client.isHandshakeComplete()) {
            System.out.println("Conexión SignalR pura establecida y Handshake completado.");
            return client;
        } else {
            // Esta línea ya no debería ser alcanzable si connect lanza la excepción correctamente
            throw new RuntimeException("Fallo en la conexión WSS o Handshake.");
        }
    }

    private void step3_secureKeyExchange() throws Exception {
        String publicKeyPem = getPublicKeyPem();

        System.out.println("\n--- Cliente " + sessionId.substring(0, 8) + ": Paso 3: Intercambio de Claves ---");
        System.out.println("Enviando PublicKey RSA (PEM) al servidor...");

        // 1. Invocar GetSymetricKey
        // El invoke se encarga de manejar el Base64 de la respuesta
        byte[] encryptedSymmetricKey = wsClient.invoke("GetSymetricKey", publicKeyPem);

        System.out.println("Clave simétrica cifrada recibida del servidor.");

        // 2. Descifrar la clave simétrica con la llave privada RSA-OAEP
        this.symmetricKey = decryptSymmetricKeyRsa(rsaKeyPair.getPrivate(), encryptedSymmetricKey);
        System.out.println("Clave simétrica AES descifrada y almacenada correctamente.");
    }

    private void step4_getTransactionData() throws Exception {
        System.out.println("\n--- Cliente " + sessionId.substring(0, 8) + ": Paso 4: Obtención de Datos ---");

        // 1. Invocar GetTransaction()
        // El invoke se encarga de manejar el Base64 de la respuesta
        byte[] encryptedResponse = wsClient.invoke("GetTransaction");

        System.out.println("Respuesta cifrada de GetTransaction recibida. Tamaño: " + encryptedResponse.length);

        // 2. Descifrar con la clave simétrica local (AES-256-GCM)
        String transactionDataJson = decryptAesData(this.symmetricKey, encryptedResponse);

        System.out.println("Datos de Transacción descifrados con éxito (AES-256-GCM).");
        System.out.println("\n=======================================================");
        System.out.println(" CLIENTE: " + sessionId.substring(0, 8));
        System.out.println("            RESPONSE.VALUE DECODIFICADO");
        System.out.println("=======================================================");
        // Mostrar datos descifrados (limitando la salida)
        System.out.println(transactionDataJson.substring(0, Math.min(transactionDataJson.length(), 500)) +
                (transactionDataJson.length() > 500 ? "..." : ""));
        System.out.println("=======================================================\n");
    }

    // --------------------------------------------------------------------------------------------------
    //                         MÉTODOS CRIPTOGRÁFICOS
    // --------------------------------------------------------------------------------------------------

    private SecretKey decryptSymmetricKeyRsa(PrivateKey privateKey, byte[] encryptedSymmetricKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA_OAEP);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = cipher.doFinal(encryptedSymmetricKey);

        if (aesKeyBytes.length != 32) {
            throw new GeneralSecurityException("Clave AES descifrada tiene tamaño inesperado: " + aesKeyBytes.length + " bytes.");
        }
        return new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
    }

    private String decryptAesData(SecretKey symmetricKey, byte[] combinedEncryptedData) throws Exception {
        final int GCM_IV_LENGTH = 12; // IV/Nonce de 12 bytes
        final int GCM_TAG_LENGTH = 16; // Tag de 16 bytes (128 bits)

        if (combinedEncryptedData.length < GCM_IV_LENGTH) {
            throw new GeneralSecurityException("Datos cifrados incompletos.");
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combinedEncryptedData, 0, iv, 0, GCM_IV_LENGTH);

        int cipherTextWithTagLength = combinedEncryptedData.length - GCM_IV_LENGTH;
        byte[] cipherTextWithTag = new byte[cipherTextWithTagLength];
        System.arraycopy(combinedEncryptedData, GCM_IV_LENGTH, cipherTextWithTag, 0, cipherTextWithTagLength);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, symmetricKey, gcmSpec);

        byte[] decryptedText = cipher.doFinal(cipherTextWithTag);
        return new String(decryptedText, StandardCharsets.UTF_8);
    }

    // --------------------------------------------------------------------------------------------------
    //                                  MAIN
    // --------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        System.out.println("Iniciando cliente REAL con WebSocket Puro al /CheckoutHub.");

        // ** ASUMIENDO QUE ESTE CODIGO ES CORRECTO Y ESTÁ FUNCIONANDO **
        String internal_id = LabelTransacionID.UIDD(12);
        String group_id = LabelTransacionID.UIDD(12);

        String token = AutenticationToken.mapperToken();
        PostPaylink datosConstructor = new PostPaylink(internal_id, group_id);
        String sesionURL = "https://pruebas.app.sypago.net:8086/api/v1/transaction/checkout?id="+datosConstructor.postPaylink(token)+"&blueprint=false";

        // Obtención del sessionId
        String activeSessionId = "a713dda2-1a19-45bd-aac1-1436dfed5af8";

        // Verificación de que el sessionId no esté vacío antes de continuar
        if (activeSessionId == null || activeSessionId.isEmpty()) {
            System.err.println("Error: El SessionId obtenido es nulo o vacío. No se puede iniciar el cliente SignalR.");
            return;
        }

        Runnable clientTask = new RealSignalRClient(activeSessionId);
        executor.execute(clientTask);

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // Se aumentó el timeout del Executor por seguridad
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    // =========================================================================
    //                   CLASE INTERNA: RealWebSocketClient (CON AJUSTES)
    // =========================================================================

    private abstract static class SignalRConnection {
        protected final String sessionId;
        public SignalRConnection(String sessionId) { this.sessionId = sessionId; }
        public abstract byte[] invoke(String method, Object... args) throws Exception;
    }

    /**
     * Implementación usando java.net.http.WebSocket (skipNegotiation: true)
     */
    private class RealWebSocketClient extends SignalRConnection implements WebSocket.Listener {

        private final Map<String, CompletableFuture<byte[]>> pendingInvocations = new ConcurrentHashMap<>();
        private final AtomicLong invocationCounter = new AtomicLong(0);
        private WebSocket webSocket;
        private final CompletableFuture<Void> handshakeFuture = new CompletableFuture<>();
        private volatile boolean handshakeComplete = false;

        // REGEX para parsear {"type":3,"invocationId":"X","result":"<BASE64>"}
        private static final Pattern RESULT_PATTERN = Pattern.compile("\"invocationId\":\"(.*?)\".*?\"result\":\"(.*?)\"");

        public RealWebSocketClient(String sessionId) { super(sessionId); }

        /**
         * Establece la conexión WSS y espera la confirmación del Handshake.
         * Se agregó manejo explícito de TimeoutException, ExecutionException e InterruptedException.
         */
        public void connect(String wssUrl) throws Exception {
            HttpClient httpClient = HttpClient.newHttpClient();

            System.out.println("  [DEBUG] Session ID usado en Header/Query: " + sessionId);
            webSocket = httpClient.newWebSocketBuilder()
                    .header("X-Checkout-Session-Id", sessionId) // Se usa el mismo
                    .buildAsync(URI.create(wssUrl), this) // URL ya contiene ?sessionId=...
                    .join();

            // AJUSTE 1: Manejo robusto de excepciones y logging para el Handshake
            try {
                System.out.println(" [WSS Event] Esperando la confirmación del Handshake (Máx. 50s)...");
                // LÍNEA CRÍTICA (antes 271): Espera con timeout
                handshakeFuture.get(50, TimeUnit.SECONDS);
                this.handshakeComplete = true;
            } catch (TimeoutException e) {
                // Si la espera expira (lo que lleva a InterruptedException si se omite)
                System.err.println("❌ Handshake Fallido: Timeout de 50s al esperar la respuesta del servidor.");
                throw new RuntimeException("Timeout de 50s al esperar la confirmación del Handshake de SignalR.", e);
            } catch (ExecutionException e) {
                // Si la conexión falló (ej. onError fue llamado o una excepción en onText/onClose)
                System.err.println("❌ Handshake Fallido: Excepción de Ejecución. Causa: " + e.getCause().getMessage());
                throw new RuntimeException("Error durante la conexión WSS o Handshake. (Causa: " + e.getCause().getMessage() + ")", e);
            } catch (InterruptedException e) {
                // Si el thread se interrumpe
                Thread.currentThread().interrupt();
                throw new RuntimeException("Conexión interrumpida durante el Handshake.", e);
            }
        }

        public boolean isHandshakeComplete() { return handshakeComplete; }

        public void close() {
            if (webSocket != null) {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Cliente terminado").join();
            }
        }

        @Override
        public byte[] invoke(String method, Object... args) throws Exception {
            if (!handshakeComplete) {
                throw new IllegalStateException("El Handshake de SignalR no se ha completado.");
            }

            String invocationId = String.valueOf(invocationCounter.incrementAndGet());
            CompletableFuture<byte[]> futureResponse = new CompletableFuture<>();
            pendingInvocations.put(invocationId, futureResponse);

            String jsonArgs = "[]";
            if (args.length > 0) {
                // Se asume que solo se pasa un argumento (publicKeyPem) que debe ir entre comillas
                // Si se invocara GetTransaction(), args sería vacío y jsonArgs sería "[]"
                jsonArgs = String.format("[\"%s\"]", args[0]);
            }

            // Payload de Invocación (type 1), con separador \u001e
            String invocationPayload = String.format(
                    "{\"type\":1,\"target\":\"%s\",\"arguments\":%s,\"invocationId\":\"%s\"}\u001e",
                    method, jsonArgs, invocationId
            );

            System.out.println("  [WSS Send] Invoking: " + method + " (" + invocationId + ")");
            webSocket.sendText(invocationPayload, true);

            // Esperar la respuesta (bloqueante) con timeout de 10 segundos
            return futureResponse.get(10, TimeUnit.SECONDS);
        }

        // --- Eventos WSS (Manejo de Mensajes) ---


        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println(" [WSS Event] Conexión WebSocket abierta. Iniciando Handshake...");

            String handshakeMessage = "{\"protocol\":\"json\",\"version\":1}\u001e";
            webSocket.sendText(handshakeMessage, true);
            System.out.println("  [WSS Send] Enviando Handshake de SignalR. Payload: " + handshakeMessage.trim().replace("\u001e", ""));
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            // Limpiamos el separador de registro \u001e
            String message = data.toString().trim().replace("\u001e", "");

            // 1. Manejar la confirmación del Handshake (respuesta {})
            if (message.equals("{}")) {
                if (!handshakeComplete && !handshakeFuture.isDone()) {
                    System.out.println("  [WSS Receive] ✅ Handshake de SignalR confirmado exitosamente.");
                    handshakeFuture.complete(null);
                }
            }

            // 2. Manejar PINGs de SignalR (type 6)
            else if (message.equals("{\"type\":6}")) {
                System.out.println("  [WSS Receive] Server PING (type 6) recibido. Ignorando.");
            }

            // 3. Procesar respuesta de Invocation (type 3)
            else {
                // Re-incluimos el separador solo para que el REGEX sea más robusto al buscar "result"
                Matcher matcher = RESULT_PATTERN.matcher(data.toString());

                if (matcher.find() && data.toString().contains("\"type\":3")) {
                    String invocationId = matcher.group(1);
                    String resultBase64 = matcher.group(2);

                    CompletableFuture<byte[]> future = pendingInvocations.remove(invocationId);
                    if (future != null) {
                        try {
                            byte[] resultBytes = Base64.getDecoder().decode(resultBase64);
                            future.complete(resultBytes);
                            System.out.println("  [WSS Receive] Response for " + invocationId + " processed.");
                        } catch (IllegalArgumentException e) {
                            future.completeExceptionally(new RuntimeException("Error al decodificar Base64 en la respuesta: " + e.getMessage()));
                        }
                    }
                } else {
                    // AJUSTE 2: Log de mensajes no procesados (incluye notificaciones type 1/2)
                    System.out.println("  [WSS Receive] ⚠️ Mensaje NO procesado (Puede ser NOTIFICACIÓN o ERROR): " + message.substring(0, Math.min(message.length(), 100)) + "...");
                }
            }
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            // AJUSTE 3: Impresión del stack trace y finalización excepcional
            System.err.println("  [WSS Event] ❌ Error en la conexión: " + error.getMessage());
            error.printStackTrace();
            handshakeFuture.completeExceptionally(error);
            pendingInvocations.values().forEach(f -> f.completeExceptionally(error));
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("  [WSS Event] 🛑 Conexión cerrada. Código: " + statusCode + ", Razón: " + reason);
            if (!handshakeFuture.isDone()) {
                // Propaga la excepción si se cierra antes de completar el Handshake
                handshakeFuture.completeExceptionally(new RuntimeException("Conexión cerrada antes del Handshake (Code: " + statusCode + "). Razón: " + reason));
            }
            if (!pendingInvocations.isEmpty()) {
                pendingInvocations.values().forEach(f -> f.completeExceptionally(new RuntimeException("Conexión cerrada. Fallo al obtener respuesta.")));
            }
            return null;
        }
    }
}