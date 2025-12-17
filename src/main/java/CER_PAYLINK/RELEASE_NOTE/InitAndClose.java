package CER_PAYLINK.RELEASE_NOTE;

import CER_PAYLINK.AutenticationToken;
import CER_PAYLINK.PostPaylink;
import Utiliti.LabelTransacionID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class InitAndClose {

    // Nota: THREAD_SIZE = 2 y TOTAL_TRANSACTIONS = 2
    private static final int THREAD_SIZE = 10; // Hilos de procesamiento en simultaneo

    // --- Configuración del Lote ---
    private static final int BATCH_SIZE = 2;
    private static final int TOTAL_TRANSACTIONS = 1000;
    private static final long TRANSACTION_TIMEOUT_SECONDS = 60000;


    public static void main(String[] args) throws IOException, InterruptedException {
        // Inicializa el pool de 2 hilos
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_SIZE);

        long startTime = System.currentTimeMillis();
        int transactionCounter = 0;

        System.out.println("--Iniciando operaciones--");
        System.out.println("Thread de procesamientos: " + THREAD_SIZE );
        System.out.println("Tiempo de pausa: " + TRANSACTION_TIMEOUT_SECONDS);

        String token = AutenticationToken.mapperToken();

        // list ids
        ArrayList<String> IDs = new ArrayList<>();

        while (transactionCounter < TOTAL_TRANSACTIONS){

            int transactionsInBatch = Math.min(BATCH_SIZE, TOTAL_TRANSACTIONS - transactionCounter);

            if (transactionsInBatch == 0){
                break;
            }

            // init operation: Somete las tareas al pool
            List<Future<String>> postFutures = new ArrayList<>();
            for (int i = 0; i < transactionsInBatch; i++) {
                PostPaylink init_operation = new PostPaylink(
                        LabelTransacionID.UIDD(12),
                        LabelTransacionID.UIDD(12)
                );
                // Somete la tarea al pool.
                Future<String> future = executor.submit(new PostTask(token, init_operation));
                postFutures.add(future);
            }

            // Recolección de resultados (bloquea el hilo main hasta que todas las tareas terminen)
            for (Future<String> future : postFutures) {
                try {
                    // .get() asegura que el hilo principal espera el resultado.
                    IDs.add(future.get());

                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Una tarea de PostPaylink falló: " + e.getMessage());
                }
            }

            // actualizacion del contador para salir del bucle
            // Esta línea ahora funcionará correctamente para detener el while.
            transactionCounter += transactionsInBatch;
        }

        executor.shutdown();

        // Espera de forma segura a que todos los hilos terminen antes de continuar
        try {
            System.out.println("Esperando finalización de hilos...");
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                // Si el tiempo expira, intenta detener los hilos que aún corren
                System.err.println("El pool de hilos no terminó a tiempo. Forzando detención...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Si el hilo principal es interrumpido durante la espera
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try{
            System.out.println("--Inicio de pausa--");
            Thread.sleep(TRANSACTION_TIMEOUT_SECONDS);

            // init close
            CloseTransactions closeTransactions = new CloseTransactions(IDs);
            System.out.println("---Iniciando proceso de cancelacion---");
            System.out.println(closeTransactions.closeTransaction(token));


        }catch (Exception e){
            System.out.println("Error: "+ e.getMessage());
        }

    }

}