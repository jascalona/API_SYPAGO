package CER_PAYLINK;

import java.security.*;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clase de utilidad para manejar las operaciones criptográficas requeridas:
 * Generación de RSA, descifrado RSA (para la clave AES) y operaciones AES-GCM.
 */
public class CriptoUtil {

    // Constantes públicas para ser usadas por otras clases
    public static final int KEY_SIZE_RSA = 2048;
    public static final String ALGORITMO_RSA_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final String ALGORITMO_AES = "AES";
    public static final String ALGORITMO_AES_GCM = "AES/GCM/NoPadding";
    public static final int GCM_IV_LENGTH = 12; // 96 bits
    public static final int GCM_TAG_LENGTH = 16; // 128 bits

    /**
     * Genera un par de claves RSA de 2048 bits.
     * @return El par de claves generado.
     */
    public static KeyPair generarClavesRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(KEY_SIZE_RSA);
        return keyGen.generateKeyPair();
    }

    /**
     * Convierte la clave pública a Base64 (X.509) para enviarla al servidor.
     * @param publicKey La clave pública a codificar.
     * @return La clave pública en formato Base64.
     */
    public static String codificarPublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Paso 3 (Servidor -> Cliente): Descifra la clave simétrica (AES) cifrada con RSA.
     * @param privateKey La clave privada RSA del cliente.
     * @param encryptedKeyBase64 La clave AES cifrada, codificada en Base64.
     * @return La clave simétrica AES original.
     */
    public static SecretKey descifrarClaveSimetricaRSA(PrivateKey privateKey, String encryptedKeyBase64) throws Exception {
        byte[] bytesCifrados = Base64.getDecoder().decode(encryptedKeyBase64);

        // Inicializar el Cipher para descifrado RSA-OAEP
        Cipher cipher = Cipher.getInstance(ALGORITMO_RSA_OAEP);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // Descifrar para obtener los bytes de la clave AES
        byte[] bytesClaveAES = cipher.doFinal(bytesCifrados);

        // Reconstruir la clave simétrica AES
        return new SecretKeySpec(bytesClaveAES, ALGORITMO_AES);
    }

    // --- Funciones para el Paso 4 (Uso de Clave AES-256-GCM) ---

    /**
     * Descifra datos usando la clave simétrica AES-256-GCM y asume que el IV
     * está concatenado al inicio de los datos cifrados.
     * NOTA: El servidor debe enviar la respuesta cifrada y el IV.
     * @param symmetricKey La clave AES (SecretKey).
     * @param dataBase64 Los datos cifrados (IV + Ciphertext), codificados en Base64.
     * @return El mensaje descifrado.
     */
    public static String descifrarAES(SecretKey symmetricKey, String dataBase64) throws Exception {
        byte[] datosCompletos = Base64.getDecoder().decode(dataBase64);

        // 1. Extraer el Vector de Inicialización (IV)
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(datosCompletos, 0, iv, 0, GCM_IV_LENGTH);

        // 2. Extraer el mensaje cifrado (sin el IV)
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
}
