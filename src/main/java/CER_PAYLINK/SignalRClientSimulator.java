package CER_PAYLINK;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;
import java.util.Map; // Necesario para la representación de headers

/**
 * CLASE: SignalRClientSimulator
 * * Implementación conceptual en Java del flujo de conexión seguro (RSA/AES)
 * a un hub SignalR.
 * */
public class SignalRClientSimulator implements Runnable {

    private final String sessionId;

    // --- Criptografía ---
    private KeyPair rsaKeyPair;
    private SecretKey symmetricKey; // Clave AES-256-GCM para comunicaciones futuras

    public SignalRClientSimulator(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void run() {
        System.out.println("--- Cliente Iniciado para Sesión: " + sessionId + " ---");
        try {
            // 4.1. Paso 1: Generación de Llaves Criptográficas (RSA)
            step1_generateRsaKeys();

            // 4.2. Paso 2: Establecimiento de Conexión WebSocket
            SignalRClientMock connection = step2_establishConnection();

            // 4.3. Paso 3: Intercambio Seguro de Claves (RSA & AES)
            step3_secureKeyExchange(connection);

            // 4.4. Paso 4: Obtención de Datos de Transacción
            step4_getTransactionData(connection);

        } catch (Exception e) {
            // Manejo de errores de conexión o criptografía.
            System.err.println("Error FATAL en cliente " + sessionId + ": " + e.getMessage());
        } finally {
            System.out.println("--- Cliente Finalizado para Sesión: " + sessionId + " ---");
        }
    }

    /**
     * 4.1. Paso 1: Generación de Llaves Criptográficas (RSA)
     * Acción: Generar RSA-OAEP de 2048 bits (pública y privada).
     */
    private void step1_generateRsaKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.rsaKeyPair = keyGen.generateKeyPair();

            System.out.println("Llaves RSA de 2048 bits generado.");
            // La clave privada se mantiene segura en la variable local 'rsaKeyPair'.
        } catch (Exception e) {
            throw new RuntimeException("Error al generar llaves RSA.", e);
        }
    }

    /**
     * 4.2. Paso 2: Establecimiento de Conexión WebSocket (Simulación de Conexión Real)
     * Endpoint: wss://pruebas.app.sypago.net:8086/hub?sessionId=${sessionId}
     * Header: "X-Checkout-Session-Id": sessionId
     */
    private SignalRClientMock step2_establishConnection() {
        String realUrl = "wss://pruebas.app.sypago.net:8086/hub?sessionId=" + sessionId;
        Map<String, String> headers = Map.of("X-Checkout-Session-Id", sessionId);

        System.out.println("Paso 2: Iniciando conexión a: " + realUrl);
        System.out.println("Paso 2: Headers requeridos: " + headers);

        // --- IMPLEMENTACIÓN REAL REQUERIDA ---
        // 1. Conectarse a 'realUrl'.
        // 2. Incluir el header 'X-Checkout-Session-Id'.
        // 3. Manejar la conexión y los mensajes del SignalR JSON Hub Protocol.
        // connection.connect(realUrl, headers); // Lógica real

        System.out.println("Conexión SignalR/WebSocket establecida.");
        return new SignalRClientMock(sessionId);
    }
    /**
     * 4.3. Paso 3: Intercambio Seguro de Claves (RSA & AES)
     * Cliente -> Servidor: Invoca GetSymetricKey con la publicKey RSA (sin cifrar).
     * Servidor -> Cliente: Recibe la clave AES cifrada con RSA (respuesta cifrada).
     * Cliente: Descifra la clave AES con la privateKey RSA.
     */
    private void step3_secureKeyExchange(SignalRClientMock connection) throws Exception {
        // 1. Cliente -> Servidor: Envía la clave pública codificada en Base64.
        String publicKeyBase64 = Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
        System.out.println("Paso 3a: Enviando PublicKey RSA (Base64) al servidor...");

        // --- IMPLEMENTACIÓN (Envío SignalR) ---
        // El cliente debe construir el mensaje JSON del Hub Protocol
        // {"type":1, "invocationId":"0", "target":"GetSymetricKey", "arguments":["<publicKeyBase64>"]}
        // Y enviarlo por el WebSocket.

        byte[] encryptedSymmetricKey = connection.invoke("GetSymetricKey", publicKeyBase64.getBytes(StandardCharsets.UTF_8));

        System.out.println("Clave simétrica cifrada recibida del servidor.");
        // 2. Cliente: Descifra la clave simétrica recibida usando la privateKey RSA
        this.symmetricKey = decryptSymmetricKeyRsa(rsaKeyPair.getPrivate(), encryptedSymmetricKey);
        System.out.println("Clave simétrica AES descifrada y almacenada correctamente.");
    }
    /**
     * 4.4. Paso 4: Obtención de Datos de Transacción
     * Acción: Invocar GetTransaction() o GetTransactionBlueprint().
     * La respuesta viene cifrada con la symmetricKey AES.
     */
    private void step4_getTransactionData(SignalRClientMock connection) throws Exception {
        // --- IMPLEMENTACIÓN REAL REQUERIDA (Envío SignalR) ---
        // El cliente debe construir el mensaje JSON del Hub Protocol, e.g.:
        // {"type":1, "invocationId":"1", "target":"GetTransaction", "arguments":[]}
        // Y enviarlo por el WebSocket.
        byte[] encryptedResponse = connection.invoke("GetTransaction", this.symmetricKey);
        System.out.println("Respuesta cifrada de GetTransaction recibida.");
        // Cliente: Descifrar la response.value usando la symmetricKey (AES-256-GCM)
        String transactionDataJson = decryptAesData(this.symmetricKey, encryptedResponse);
        System.out.println("Datos de Transacción descifrados con éxito (AES-256-GCM).");
        System.out.println("DATOS RECIBIDOS: " + transactionDataJson);
    }

    /**
     * Descifra la clave simétrica (AES) utilizando la clave privada RSA.
     */
    private SecretKey decryptSymmetricKeyRsa(PrivateKey privateKey, byte[] encryptedSymmetricKey) throws Exception {
        // Algoritmo: RSA/ECB/OAEPWithSHA-256AndMGF1Padding
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = cipher.doFinal(encryptedSymmetricKey);
        // Reconstruimos la clave simétrica como un objeto SecretKey
        return new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
    }

    /**
     * Descifra datos recibidos utilizando la clave AES (AES-256-GCM).
     */
    private String decryptAesData(SecretKey symmetricKey, byte[] encryptedData) throws Exception {
        final int GCM_IV_LENGTH = 12; // IV de 12 bytes (estándar GCM)
        final int GCM_TAG_LENGTH = 16; // TAG de 16 bytes (estándar GCM)

        // 1. Extraer IV (Initialization Vector)
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

        // 2. Extraer el texto cifrado + TAG
        int encryptedLength = encryptedData.length - GCM_IV_LENGTH;
        byte[] cipherTextWithTag = new byte[encryptedLength];
        System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherTextWithTag, 0, encryptedLength);

        // 3. Crear el objeto GCMParameterSpec (TAG length en bits)
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        // 4. Inicializar el descifrador
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, symmetricKey, gcmSpec);

        // 5. Descifrar
        byte[] decryptedText = cipher.doFinal(cipherTextWithTag);
        return new String(decryptedText, StandardCharsets.UTF_8);
    }

    // =========================================================================
    //                            CLASE MOCK/SIMULADA
    // =========================================================================

    /**
     * Mock para simular la interacción con el hub SignalR.
     * En una implementación real, esta clase sería reemplazada por la lógica
     * de un cliente WebSocket. Implementa la lógica de Cifrado del Servidor
     * para verificar la lógica de Descifrado del Cliente.
     */
    private class SignalRClientMock {
        private final String sessionId;

        public SignalRClientMock(String sessionId) {
            this.sessionId = sessionId;
        }
        /**
         * Simula la invocación a un metodo del hub y espera la respuesta cifrada.
         * En la vida real, esto enviaría JSON a través de WebSocket y esperaría un JSON de respuesta.
         */
        public byte[] invoke(String method, Object... args) throws Exception {
            if (method.equals("GetSymetricKey")) {
                // El cliente envía bytes de su Public Key codificada en Base64.
                byte[] clientPublicKeyBase64Bytes = (byte[]) args[0];
                return serverEncryptAesKey(clientPublicKeyBase64Bytes);

            } else if (method.equals("GetTransaction")) {
                // El cliente ha invocado GetTransaction. El Servidor responde con datos cifrados.
                SecretKey clientSymmetricKey = (SecretKey) args[0];
                return serverEncryptTransactionData(clientSymmetricKey);
            }
            throw new UnsupportedOperationException("Método SignalR mock no soportado.");
        }

        // --- SIMULACIÓN DE LÓGICA DEL SERVIDOR (SERVER-SIDE) ---

        /**
         * Simula la lógica del servidor: genera AES y la cifra con la Public Key del cliente.
         */
        private byte[] serverEncryptAesKey(byte[] clientPublicKeyBase64Bytes) throws Exception {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // AES-256
            SecretKey serverAesKey = keyGen.generateKey();

            // Decodificar Base64 para obtener los bytes de la clave pública
            String publicKeyBase64String = new String(clientPublicKeyBase64Bytes, StandardCharsets.UTF_8);
            byte[] rawPublicKeyBytes = Base64.getDecoder().decode(publicKeyBase64String);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey clientPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(rawPublicKeyBytes));

            // Cifrar la clave AES con RSA-OAEP
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
            return cipher.doFinal(serverAesKey.getEncoded());
        }

        /**
         * Simula la lógica del servidor: cifra los datos de transacción con la clave AES compartida.
         */
        private byte[] serverEncryptTransactionData(SecretKey sharedAesKey) throws Exception {
            String mockData = "{\"transactionId\":\"T-" + sessionId + "\",\"amount\":10000.00,\"currency\":\"USD\",\"details\":\"Datos de transacción seguros. Esto simula el objeto JSON completo recibido del servidor.\"}";

            // Generar IV para GCM
            final int GCM_IV_LENGTH = 12;
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(16 * 8, iv);

            // Inicializar y cifrar
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sharedAesKey, gcmSpec);

            byte[] cipherText = cipher.doFinal(mockData.getBytes(StandardCharsets.UTF_8));

            // Concatenar IV + Texto Cifrado (el TAG GCM va incluido al final del cipherText)
            byte[] encryptedPayload = new byte[GCM_IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, encryptedPayload, 0, GCM_IV_LENGTH);
            System.arraycopy(cipherText, 0, encryptedPayload, GCM_IV_LENGTH, cipherText.length);

            return encryptedPayload;
        }
    }


    // =========================================================================
    //                                  MAIN
    // =========================================================================

    public static void main(String[] args) {
        final String TARGET_SESSION_ID = "8189c0c5-6806-4732-8571-7f5b1a50a0e1";
        final int NUM_CLIENTS = 1;

        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);

        System.out.println("Iniciando simulación de " + NUM_CLIENTS + " clientes concurrentes.");

        for (int i = 0; i < NUM_CLIENTS; i++) {
            // Generar IDs únicos para la simulación de concurrencia
            String clientSessionId = TARGET_SESSION_ID + (i > 0 ? ("-" + (i+1)) : "");
            Runnable clientTask = new SignalRClientSimulator(clientSessionId);
            executor.execute(clientTask);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
