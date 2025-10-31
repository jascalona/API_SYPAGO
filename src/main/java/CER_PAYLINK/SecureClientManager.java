package CER_PAYLINK;

import CER_PAYLINK.CriptoUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * Clase que simula el flujo de seguridad para el cliente virtual,
 * orquestando la generación de claves, el intercambio seguro y el
 * uso de la clave simétrica.
 */
public class SecureClientManager {

    // --- Variables de Sesión (deben ser guardadas en la instancia del cliente virtual) ---
    private KeyPair rsaKeyPair;
    private SecretKey symmetricKeyAES;
    private String sessionId;

    // Simulación de la Clave AES Cifrada que el Servidor enviaría.
    // Esta cadena debe ser la representación Base64 de la clave AES (32 bytes) cifrada con RSA-OAEP.
    private static final String MOCK_ENCRYPTED_AES_KEY_BASE64 =
            "U+YjR5LpQyN0wK3v8zXm/7qZ9V6jB1sH3d4O2iA7x0C9lF5tE2oP4rS8uWvX1YcZqXgN0iA3j9K6lH7oM8pQ2rT4vU6wZ1x3y4a5b6c7d8e9f0gA1b2c3d4e5f6g7h8i9j0kL1m2n3o4p5q6r7s8t9u0v1w2x3y4z5";

    public SecureClientManager(String initialSessionId) {
        this.sessionId = initialSessionId;
        System.out.println("Cliente virtual inicializado con sessionId: " + this.sessionId);
    }

    // --- Procedimiento del Flujo Seguro ---

    public void iniciarFlujoSeguro() {
        try {
            // 4.1. Paso 1: Generación de Llaves Criptográficas (RSA)
            System.out.println("\n--- 4.1. Generando Par de Claves RSA (" + CriptoUtil.KEY_SIZE_RSA + " bits) ---");
            this.rsaKeyPair = CriptoUtil.generarClavesRSA();
            PublicKey publicKey = this.rsaKeyPair.getPublic();
            PrivateKey privateKey = this.rsaKeyPair.getPrivate(); // <--- GUARDADA DE FORMA SEGURA

            String publicKeyBase64 = CriptoUtil.codificarPublicKey(publicKey);
            System.out.println("Clave Pública (Base64) generada, lista para ser enviada.");
            //System.out.println("Clave Pública: " + publicKeyBase64.substring(0, 50) + "..."); // Solo para debug

            // 4.2. Paso 2: Establecimiento de Conexión WebSocket (Simulado)
            System.out.println("\n--- 4.2. Conexión WebSocket (SignalR) ---");
            System.out.println("INFO: Usando sessionId en URL y Header para iniciar conexión WSS...");
            // ************ Aquí iría la lógica real de conexión SignalR ************
            TimeUnit.SECONDS.sleep(1);

            // 4.3. Paso 3: Intercambio Seguro de Claves (RSA & AES)
            System.out.println("\n--- 4.3. Intercambio de Claves ---");

            // ➔ Cliente -> Servidor: Enviar la publicKey RSA
            System.out.println("-> Cliente Invoca: connection.invoke('GetSymetricKey', publicKeyBase64)");

            // ➔ Servidor -> Cliente: Recibir clave AES cifrada
            System.out.println("<- Servidor Responde: Clave AES cifrada con RSA recibida...");
            // ¡LÍNEA CORREGIDA! Se usa la constante con el sufijo "64"
            String encryptedSymmetricKeyBase64 = MOCK_ENCRYPTED_AES_KEY_BASE64;

            // ➔ Cliente: Descifrar la clave simétrica con la privateKey RSA
            this.symmetricKeyAES = CriptoUtil.descifrarClaveSimetricaRSA(privateKey, encryptedSymmetricKeyBase64);

            // ➔ Almacenamiento: Guardar la symmetricKey (AES-256-GCM)
            System.out.println("ÉXITO: Clave simétrica AES descifrada y almacenada en memoria.");
            System.out.println("Clave AES (Algoritmo): " + this.symmetricKeyAES.getAlgorithm() + " | Longitud: " + (this.symmetricKeyAES.getEncoded().length * 8) + " bits.");


            // 4.4. Paso 4: Obtención de Datos de Transacción
            System.out.println("\n--- 4.4. Obtención de Datos de Transacción (Cifrado con AES) ---");

            if (this.symmetricKeyAES != null) {
                // Simulación de respuesta cifrada del servidor
                String mockEncryptedResponse = simularRespuestaServidorCifrada(this.symmetricKeyAES);
                System.out.println("<- Respuesta Cifrada (AES): " + mockEncryptedResponse.substring(0, 50) + "...");

                // Cliente: Descifrar la respuesta usando la symmetricKey
                String decryptedResponse = CriptoUtil.descifrarAES(this.symmetricKeyAES, mockEncryptedResponse);
                System.out.println("-> Cliente Descifra: OK. Datos de Transacción: [" + decryptedResponse + "]");
            }

        } catch (Exception e) {
            System.err.println("Error durante el flujo seguro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * SIMULA el proceso inverso (Servidor): Cifra un mensaje con la clave AES,
     * incluyendo un IV al inicio (requerido para AES-GCM).
     */
    private String simularRespuestaServidorCifrada(SecretKey key) throws Exception {
        String data = "transaction_id: " + sessionId + ", status: 'PENDIENTE', monto: 100.00";

        // Generar un IV (Vector de Inicialización) aleatorio
        byte[] iv = new byte[CriptoUtil.GCM_IV_LENGTH];
        new Random().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CriptoUtil.ALGORITMO_AES_GCM);
        // La etiqueta de autenticación (Auth Tag) se configura en el GCMParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(CriptoUtil.GCM_TAG_LENGTH * 8, iv));

        byte[] mensajeCifrado = cipher.doFinal(data.getBytes("UTF-8"));

        // Concatenar IV y mensaje cifrado, luego codificar en Base64
        // El servidor envía (IV + Texto Cifrado + Etiqueta de Autenticación)
        byte[] datosCompletos = new byte[iv.length + mensajeCifrado.length];
        System.arraycopy(iv, 0, datosCompletos, 0, iv.length);
        System.arraycopy(mensajeCifrado, 0, datosCompletos, iv.length, mensajeCifrado.length);

        return Base64.getEncoder().encodeToString(datosCompletos);
    }

    public static void main(String[] args) {
        // Usar un Session ID mock para la demostración
        String mockSessionId = "dcc5aea8-91b2-400e-ae6e-397878cc8e00";

        SecureClientManager cliente = new SecureClientManager(mockSessionId);
        cliente.iniciarFlujoSeguro();
    }
}
