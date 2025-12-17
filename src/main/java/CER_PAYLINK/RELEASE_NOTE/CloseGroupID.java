package CER_PAYLINK.RELEASE_NOTE;

import CER_PAYLINK.AutenticationToken;
import CER_PAYLINK.PostPaylink;
import Utiliti.LabelTransacionID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class CloseGroupID {

    // Nota: THREAD_SIZE = 2 y TOTAL_TRANSACTIONS = 2
    private static final int THREAD_SIZE = 50; // Hilos de procesamiento en simultaneo

    // --- Configuración del Lote ---
    private static final int BATCH_SIZE = 2;
    private static final int TOTAL_TRANSACTIONS = 5000;
    private static final long TRANSACTION_TIMEOUT_SECONDS = 20000;


    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_SIZE);
        String token = AutenticationToken.mapperToken();
        ArrayList<String> IDs = new ArrayList<>(); // Aquí guardaremos los group_ids

        int transactionCounter = 0;
        System.out.println("--Iniciando operaciones--");

        while (transactionCounter < TOTAL_TRANSACTIONS) {
            int transactionsInBatch = Math.min(BATCH_SIZE, TOTAL_TRANSACTIONS - transactionCounter);

            // Lista para almacenar los objetos Future del lote actual
            List<Future<String>> currentBatchFutures = new ArrayList<>();

            for (int i = 0; i < transactionsInBatch; i++) {
                PostPaylink init_operation = new PostPaylink(
                        LabelTransacionID.UIDD(12),
                        "MANTEQUILLA");

                // La tarea debe retornar el group_id. Si PostTask solo retorna transaction_id,
                // hay que obtener el group_id después.
                currentBatchFutures.add(executor.submit(new PostTask(token, init_operation)));
            }

            //  Procesamos los resultados del lote
            for (Future<String> future : currentBatchFutures) {
                try {
                    // Obtenemos el ID retornado por la tarea
                    String resultId = future.get();

                    // --- LÓGICA DE INTERCAMBIO ---

                    String urlweb = "https://pruebas.sypago.net:8086/api/v1/transaction/" + resultId;

                    PostPaylink data_groupid = new PostPaylink( LabelTransacionID.UIDD(12), "MANTEQUILLA");
                    String groupid = data_groupid.getPaylink(token, urlweb);

                    IDs.add(groupid);

                } catch (Exception e) {
                    System.err.println("Error procesando transacción: " + e.getMessage());
                }
            }
            transactionCounter += transactionsInBatch;
        }

        // Cierre del executor
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // 3. Proceso de cancelación por GROUP_ID
        try {
            System.out.println("--Inicio de pausa--");
            Thread.sleep(TRANSACTION_TIMEOUT_SECONDS);

            // Pasamos la lista de GROUP_IDs recolectados
            CloseTransactions closeTransactions = new CloseTransactions(IDs);
            System.out.println("---Iniciando proceso de cancelacion por Group ID---");
            System.out.println(closeTransactions.closeTransaction(token));

        } catch (Exception e) {
            System.out.println("Error en cancelación: " + e.getMessage());
        }
    }
}
