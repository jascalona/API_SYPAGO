package CER_PAYLINK;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.TransportEnum;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.util.Base64;
import java.util.Random;

public class SignalRClient {

    // --------------------------------------------------------------------------------
    // --- CLASE INTERNA: CriptoUtil (Mantenida del código anterior, con ajustes) ---
    // --------------------------------------------------------------------------------
    private static class CriptoUtil {
        public static final int KEY_SIZE_RSA = 2048;
        public static final String ALGORITMO_RSA_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
        public static final String ALGORITMO_AES = "AES";
        public static final String ALGORITMO_AES_GCM = "AES/GCM/NoPadding";
        public static final int GCM_IV_LENGTH = 12; // 96 bits
        public static final int GCM_TAG_LENGTH = 16; // 128 bits

        public static KeyPair generarClavesRSA() throws NoSuchAlgorithmException {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE_RSA);
            return keyGen.generateKeyPair();
        }

        public static String codificarPublicKey(PublicKey publicKey) {
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }

        public static SecretKey descifrarClaveSimetricaRSA(PrivateKey privateKey, String encryptedKeyBase64) throws Exception {
            byte[] bytesCifrados = Base64.getDecoder().decode(encryptedKeyBase64);
            Cipher cipher = Cipher.getInstance(ALGORITMO_RSA_OAEP);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bytesClaveAES = cipher.doFinal(bytesCifrados);
            return new SecretKeySpec(bytesClaveAES, ALGORITMO_AES);
        }

        // Asume que el IV de 12 bytes está concatenado al inicio de los datos cifrados Base64
        public static String descifrarAES(SecretKey symmetricKey, String dataBase64) throws Exception {
            byte[] datosCompletos = Base64.getDecoder().decode(dataBase64);

            if (datosCompletos.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
                throw new GeneralSecurityException("Datos cifrados incompletos o mal formados.");
            }

            // 1. Extraer el Vector de Inicialización (IV)
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(datosCompletos, 0, iv, 0, GCM_IV_LENGTH);

            // 2. Extraer el mensaje cifrado (el resto de los bytes)
            byte[] mensajeCifrado = new byte[datosCompletos.length - GCM_IV_LENGTH];
            System.arraycopy(datosCompletos, GCM_IV_LENGTH, mensajeCifrado, 0, mensajeCifrado.length);

            // 3. Configurar el Cipher
            Cipher cipher = Cipher.getInstance(ALGORITMO_AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey, spec);

            // 4. Descifrar
            byte[] bytesDescifrados = cipher.doFinal(mensajeCifrado);

            return new String(bytesDescifrados, "UTF-8");
        }

        // Simulación de cifrado para generar una respuesta mock
        public static String simularCifradoAES(SecretKey key, String data, String sessionId) throws Exception {
            byte[] iv = new byte[CriptoUtil.GCM_IV_LENGTH];
            new Random().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CriptoUtil.ALGORITMO_AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(CriptoUtil.GCM_TAG_LENGTH * 8, iv));

            byte[] mensajeCifrado = cipher.doFinal(data.getBytes("UTF-8"));

            byte[] datosCompletos = new byte[iv.length + mensajeCifrado.length];
            System.arraycopy(iv, 0, datosCompletos, 0, iv.length);
            System.arraycopy(mensajeCifrado, 0, datosCompletos, iv.length, mensajeCifrado.length);

            return Base64.getEncoder().encodeToString(datosCompletos);
        }
    }
    // --------------------------------------------------------------------------------

    // URL base del Hub SignalR
    private static final String HUB_URL_BASE = "https://pruebas.app.sypago.net:8086/hub";
    private KeyPair rsaKeyPair;
    private SecretKey symmetricKeyAES;
    private HubConnection connection;
    private final String sessionId;

    public SignalRClient(String sessionId) throws NoSuchAlgorithmException {
        this.sessionId = sessionId;
        // Paso 1: Generar las claves RSA del cliente al iniciar
        this.rsaKeyPair = CriptoUtil.generarClavesRSA();
        System.out.println("Claves RSA generadas con éxito.");
    }

    /**
     * Orquesta el flujo completo de conexión, intercambio de claves y solicitud de datos.
     */
    public void iniciarFlujoSeguro() {
        try {
            // ===================================================================
            // 4.2. Paso 2: Establecimiento de Conexión WebSocket
            // ===================================================================
            System.out.println("\n--- 4.2. Estableciendo Conexión SignalR ---");

            String connectionUrl = HUB_URL_BASE + "?sessionId=" + sessionId;
            System.out.println(connectionUrl);

            // Configuración de la conexión SignalR
            this.connection = HubConnectionBuilder.create(connectionUrl)
                    // Configurar el Header de Sesión
                    .withHeader("X-Checkout-Session-Id", sessionId)
                    // Configuración: skipNegotiation: true, transport: WebSockets
                    .withTransport(TransportEnum.WEBSOCKETS)
                    .build();

            // Iniciar la conexión (bloqueante para esperar el resultado)
            connection.start().blockingAwait();
            System.out.println("Conexión SignalR establecida con éxito.");

            // ===================================================================
            // 4.3. Paso 3: Intercambio Seguro de Claves (RSA & AES)
            // ===================================================================
            System.out.println("\n--- 4.3. Intercambio Seguro de Claves ---");

            // ➔ Cliente → Servidor: Enviar la publicKey RSA
            String publicKeyBase64 = CriptoUtil.codificarPublicKey(this.rsaKeyPair.getPublic());
            System.out.println("-> Enviando clave pública RSA...");

            // Usamos invoke para esperar la respuesta del servidor (la clave AES cifrada)
            // La respuesta debe ser una cadena (String)
            String encryptedSymmetricKeyBase64 = connection.invoke(String.class, "GetSymetricKey", publicKeyBase64).blockingGet();

            System.out.println("<- Clave Simétrica Cifrada (Base64) recibida.");

            // ➔ Cliente: Descifrar la clave simétrica usando la privateKey RSA
            PrivateKey privateKey = this.rsaKeyPair.getPrivate();
            this.symmetricKeyAES = CriptoUtil.descifrarClaveSimetricaRSA(privateKey, encryptedSymmetricKeyBase64);

            System.out.println("ÉXITO: Clave AES-256 descifrada y almacenada para uso futuro.");

            // ===================================================================
            // 4.4. Paso 4: Obtención de Datos de Transacción (Cifrado con AES)
            // ===================================================================
            System.out.println("\n--- 4.4. Solicitando y Descifrando Datos de Transacción ---");

            // Acción: Invocar GetTransaction()
            System.out.println("-> Invocar GetTransaction()");
            // La respuesta es un String (Base64) que contiene el IV y el Ciphertext
            String encryptedTransactionData = connection.invoke(String.class, "GetTransaction").blockingGet();

            System.out.println("<- Respuesta cifrada (AES) recibida: " + encryptedTransactionData.substring(0, 50) + "...");

            // Cliente: Descifrar la response.value usando la symmetricKey.
            String decryptedResponse = CriptoUtil.descifrarAES(this.symmetricKeyAES, encryptedTransactionData);

            System.out.println("\n=== Resultado Final ===");
            System.out.println("Datos de Transacción Descifrados:\n" + decryptedResponse);

        } catch (Exception e) {
            System.err.println("Error durante la conexión/criptografía: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null && !connection.getConnectionState().name().equals("DISCONNECTED")) {
                try {
                    connection.stop().blockingAwait();
                    System.out.println("\nConexión SignalR cerrada.");
                } catch (Exception e) {
                    System.err.println("Error al detener la conexión: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        // Asumiendo que el sessionId ya fue obtenido a través de PostPaylink
        String mockSessionId = "dd49ea01-c48d-49fc-a566-9469fb99ec91";

        try {
            SignalRClient cliente = new SignalRClient(mockSessionId);
            cliente.iniciarFlujoSeguro();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: El algoritmo criptográfico RSA no está disponible.");
        }
    }
}
