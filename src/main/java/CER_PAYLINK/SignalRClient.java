package CER_PAYLINK;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.TransportEnum;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SignalRClient implements Callable<Boolean> {
    private static final String HUB_URL_BASE = "https://pruebas.app.sypago.net:8086/CheckoutHub";
    private static final String ALGORITHM_RSA = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
    private static final Gson GSON = new Gson();

    private final String sessionId;
    private KeyPair rsaKeyPair;
    private SecretKey symmetricKey;
    private HubConnection hubConnection;

    public SignalRClient(String sessionId) {
        this.sessionId = sessionId;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // --------------------------------------------------------------------------------------------------
    //                                  FLUJO PRINCIPAL
    // --------------------------------------------------------------------------------------------------
    @Override
    public Boolean call() throws Exception {
        System.out.println("--- Cliente virtual Iniciado para Sesión: " + sessionId + " ---");
        try {
            step1_generateRsaKeys();

            step2_establishConnection();
            if (hubConnection == null || hubConnection.getConnectionState() != com.microsoft.signalr.HubConnectionState.CONNECTED) {
                System.err.println("Descartando sesión " + sessionId + ": Conexión SignalR no establecida o fallida.");
                throw new RuntimeException("Fallo en la conexión SignalR.");
            }

            step3_secureKeyExchange();
            if (this.symmetricKey == null) {
                System.err.println("Descartando sesión " + sessionId + ": Intercambio de clave simétrica fallido.");
                throw new RuntimeException("Fallo en el intercambio de clave simétrica.");
            }

            step4_getTransactionData();

            return true;

        } catch (Exception e) {
            System.err.println("Error FATAL en cliente " + sessionId + ": " + e.getMessage());

            if (e.getCause() instanceof InterruptedException || e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;

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
        } catch (Exception e) {
            throw new RuntimeException("Error al generar llaves RSA.", e);
        }
    }

    private String getPublicKeyPem() {
        PublicKey pubKey = rsaKeyPair.getPublic();
        String base64Content = Base64.getEncoder().encodeToString(pubKey.getEncoded());
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\n");

        int chunkSize = 64;
        for (int i = 0; i < base64Content.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, base64Content.length());
            pem.append(base64Content.substring(i, end));
            pem.append('\n');
        }

        pem.append("-----END PUBLIC KEY-----\n");
        return pem.toString();
    }

    private void step2_establishConnection() throws Exception {
        String fullUrl = HUB_URL_BASE + "?sessionId=" + sessionId;

        this.hubConnection = HubConnectionBuilder.create(fullUrl)
                .withHeader("X-Checkout-Session-Id", sessionId)
                .withTransport(TransportEnum.WEBSOCKETS)
                .build();

        try {
            // Timeout de inicio de conexión
            hubConnection.start().blockingAwait(30, TimeUnit.SECONDS);
            System.out.println("Conexión SignalR y Handshake completados exitosamente.");
        } catch (Exception e) {
            System.err.println("Fallo al establecer la conexión SignalR para " + sessionId + " (Timeout/Error de conexión).");
            this.hubConnection = null;
            throw new RuntimeException("Fallo en la conexión SignalR (Timeout/Error de conexión).", e);
        }
    }

    private void step3_secureKeyExchange() throws Exception {
        if (hubConnection == null || hubConnection.getConnectionState() != com.microsoft.signalr.HubConnectionState.CONNECTED) {
            throw new IllegalStateException("La conexión SignalR no está activa.");
        }

        String publicKeyPem = getPublicKeyPem();

        try {
            String encryptedSymmetricKeyBase64 = hubConnection.invoke(
                            String.class,
                            "GetSymetricKey",
                            publicKeyPem
                    )

                    .blockingGet();

            // 2. Descifrar la clave simétrica con la llave privada RSA
            byte[] encryptedSymmetricKey = Base64.getDecoder().decode(encryptedSymmetricKeyBase64);

            if (encryptedSymmetricKey.length == 0) {
                throw new GeneralSecurityException(
                        "¡Error en el Protocolo! El servidor devolvió una clave cifrada de 0 bytes. " +
                                "Esto indica que el servidor RECHAZÓ la clave pública RSA (PEM) enviada."
                );
            }

            // 3. Descifrar con la clave privada RSA
            this.symmetricKey = decryptSymmetricKeyRsa(rsaKeyPair.getPrivate(), encryptedSymmetricKey);

        } catch (Exception e) {
            this.symmetricKey = null;
            throw e;
        }
    }

    private void step4_getTransactionData() throws Exception {
        if (hubConnection == null || hubConnection.getConnectionState() != com.microsoft.signalr.HubConnectionState.CONNECTED) {
            throw new IllegalStateException("La conexión SignalR no está activa.");
        }
        if (this.symmetricKey == null) {
            throw new IllegalStateException("La clave simétrica no se pudo descifrar en el paso anterior.");
        }

        // Invocar GetTransaction.
        Object serverResponseObject = hubConnection.invoke(Object.class, "GetTransaction")
                .blockingGet();

        // 1. Extraer la respuesta cifrada en Base64 del objeto
        String encryptedResponseBase64;
        if (serverResponseObject instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) serverResponseObject;

            Object resultValue = map.get("value");

            if (resultValue instanceof String) {
                encryptedResponseBase64 = (String) resultValue;
            } else {
                String serverResponseJson = GSON.toJson(serverResponseObject);
                throw new GeneralSecurityException("Respuesta SignalR: Objeto JSON devuelto pero el campo 'value' no es un string Base64. Contenido: " + serverResponseJson);
            }
        } else {
            String serverResponseJson = GSON.toJson(serverResponseObject);
            throw new GeneralSecurityException("Respuesta SignalR: Tipo de retorno inesperado. Esperado Map. Recibido: " + serverResponseObject.getClass().getName() + ". Contenido: " + serverResponseJson);
        }

        // 2. Decodificar Base64
        byte[] encryptedResponse = Base64.getDecoder().decode(encryptedResponseBase64);

        // 3. Descifrar con la clave simétrica local (AES-256-GCM)
        String transactionDataJson = decryptAesData(this.symmetricKey, encryptedResponse);

        System.out.println("\n=======================================================");
        System.out.println(" CLIENTE: " + sessionId + "\n" + transactionDataJson.substring(0, Math.min(transactionDataJson.length(), 500)) +
                (transactionDataJson.length() > 500 ? "..." : ""));
        System.out.println("=======================================================\n");
    }

    // --------------------------------------------------------------------------------------------------
    //                         MÉTODOS CRIPTOGRÁFICOS
    // --------------------------------------------------------------------------------------------------

    private SecretKey decryptSymmetricKeyRsa(PrivateKey privateKey, byte[] encryptedSymmetricKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM_RSA, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = cipher.doFinal(encryptedSymmetricKey);
        int keyLength = Math.min(aesKeyBytes.length, 32);
        return new SecretKeySpec(aesKeyBytes, 0, keyLength, "AES");
    }

    private String decryptAesData(SecretKey symmetricKey, byte[] combinedEncryptedData) throws Exception {
        final int GCM_IV_LENGTH = 12;
        final int GCM_TAG_LENGTH = 16;

        if (combinedEncryptedData.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
            throw new GeneralSecurityException("Datos cifrados incompletos o faltan IV/Tag.");
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

    // *** CORRECCIÓN: Se aumenta el THREAD_SIZE a 20 para evitar el encolamiento ***
    private static final int THREAD_SIZE = 10;

    // --- Configuración del Lote ---
    private static final int BATCH_SIZE = 5;
    private static final int TOTAL_TRANSACTIONS = 1000;
    private static final long TRANSACTION_TIMEOUT_SECONDS = 30; //Timeout para mayor tolerancia y consistencia

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_SIZE);
        List<Future<String>> allFutures = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        int transactionCounter = 0;

        System.out.println("--- Parametros de Configuracion ---");
        System.out.println("  - Hilos de ejecucion: " + THREAD_SIZE);
        System.out.println("  - Lote: " + BATCH_SIZE + " transacciones por lote.");
        System.out.println("  - Timeout por Transacción: " + TRANSACTION_TIMEOUT_SECONDS + " segundos.");
        System.out.println("  - Total de Transacciones a Ejecutar: " + TOTAL_TRANSACTIONS);
        System.out.println("---------------------------------------------------------------------");

        while (transactionCounter < TOTAL_TRANSACTIONS) {

            int transactionsInBatch = Math.min(BATCH_SIZE, TOTAL_TRANSACTIONS - transactionCounter);

            if (transactionsInBatch == 0) {
                break;
            }

            List<Future<String>> currentBatchFutures = new ArrayList<>();
            for (int i = 0; i < transactionsInBatch; i++) {
                // TransactionTask debe ser Callable<String> y en su .call() ejecuta clientTask.call()
                Callable<String> task = new TransactionTask(transactionCounter);
                Future<String> future = executor.submit(task);
                currentBatchFutures.add(future);
                allFutures.add(future);
                transactionCounter++;
            }

            System.out.println("Total de mensajes enviados: " + transactionCounter + " (Lote de " + transactionsInBatch + ")");

            int completedInBatch = 0;
            int failedInBatch = 0;

            for (Future<String> future : currentBatchFutures) {
                try {
                    // Si el Future completa sin lanzar excepción, es éxito.
                    future.get(TRANSACTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    completedInBatch++;
                } catch (TimeoutException e) {
                    future.cancel(true); // Cancelar si hay timeout
                    failedInBatch++;
                    System.err.printf("[TIMEOUT] Transacción no completada en %d segundos.\n", TRANSACTION_TIMEOUT_SECONDS);
                } catch (ExecutionException e) { // Capturar fallos internos
                    future.cancel(true);
                    failedInBatch++;
                    // La excepción original lanzada por SignalRClient está envuelta.
                    System.err.println("[FALLO INTERNO] Excepción en la tarea: " + e.getCause().getMessage());
                } catch (Exception e) {
                    failedInBatch++;
                    System.err.println("[FALLO GENERAL] Excepción al obtener resultado: " + e.getMessage());
                }
            }

            long timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
            System.out.printf("--- LOTE COMPLETADO (Transacciones: %d OK: %d Fallidas: %d Tiempo transcurrido: %d s) ---\n",
                    transactionsInBatch, completedInBatch, failedInBatch, timeElapsed);

            if (transactionCounter < TOTAL_TRANSACTIONS) {
                System.out.println("\n-- INICIANDO EL SIGUIENTE LOTE --");
            }
        }

        long finishTime = System.currentTimeMillis();

        System.out.println("\n---------------------------------------------------------------------");
        System.out.println("Se han enviado todas las transacciones (" + TOTAL_TRANSACTIONS + ").");
        System.out.println("Iniciando apagado ordenado de servicios...");

        executor.shutdown();
        try {
            if (!executor.awaitTermination(TRANSACTION_TIMEOUT_SECONDS * 3, TimeUnit.SECONDS)) {
                System.out.println("\nLas tareas no terminaron a tiempo, forzando apagado.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("\nEspera interrumpida.");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        int completed = 0;
        int failed = 0;

        for (Future<String> future : allFutures) {
            try {
                future.get(1, TimeUnit.MILLISECONDS);
                completed++;
            } catch (CancellationException | TimeoutException e) {
                failed++;
            } catch (ExecutionException e) {
                failed++;
            } catch (Exception e) {
                failed++;
            }
        }

        double totalRunTimeSeconds = (finishTime - startTime) / 1000.0;

        System.out.println("\n--- Resumen de Resultados ---");
        System.out.println("Transacciones Enviadas: " + transactionCounter);
        System.out.println("Transacciones Completadas (Aprox. OK): " + completed);
        System.out.println("Transacciones fallidas o con Timeout: " + failed);
        System.out.println("Duracion Total de las Pruebas: " + totalRunTimeSeconds + " segundos");
    }
}