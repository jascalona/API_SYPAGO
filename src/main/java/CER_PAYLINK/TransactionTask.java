package CER_PAYLINK;

import Utiliti.LabelTransacionID;

import java.io.IOException;
import java.util.concurrent.Callable;

public class TransactionTask implements Callable<String> {
    private final int transaccionIndex;

    public TransactionTask(int transaccionIndex) {
        this.transaccionIndex = transaccionIndex;
    }

    public static String generate_session_id() throws IOException {
        //Logica para generar los links y los session_id
        String internal_id = LabelTransacionID.UIDD(12);
        String group_id = LabelTransacionID.UIDD(12);
        String token = AutenticationToken.mapperToken();
        PostPaylink datosConstructor = new PostPaylink(internal_id, group_id);
        String transaction_id = datosConstructor.postPaylink(token);
        String url_sessionId = "https://pruebas.app.sypago.net:8086/api/v1/transaction/checkout?id=" + transaction_id + "&blueprint=false";
        return datosConstructor.obtain_sesionId(token, url_sessionId);
    }

    @Override
    public String call() throws Exception {
        // ... (generate_session_id se mantiene igual)
        String session_id = TransactionTask.generate_session_id();

        if (session_id == null || session_id.isEmpty()) {
            // Fallo en la generación del ID se sigue reportando como ERROR
            throw new RuntimeException("El SessionId obtenido es nulo o vacío.");
        }

        // Iniciar el cliente SignalR
        // CAMBIO CRÍTICO: Ahora SignalRClient es un Callable, no Runnable.
        // Llamamos a su .call() y si devuelve true, es éxito.
        Callable<Boolean> clientTask = new SignalRClient(session_id);

        // Si el cliente lanza una excepción, esta será capturada por Future.get() en main.
        // Si no lanza una excepción y devuelve true, es éxito.
        clientTask.call();

        // Si la llamada no lanzó excepción, es éxito.
        return "[COMPLETADA] Transaccion " + transaccionIndex + " con Session ID: " + session_id;
    }
}