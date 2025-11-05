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
        // *********** LOGICA CONCURRENTE ***********
        String session_id = TransactionTask.generate_session_id();

        if (session_id == null || session_id.isEmpty()) {
            return "[ERROR] Tarea " + transaccionIndex + ": El SessionId obtenido es nulo o vacío.";
        }
        // Iniciar el cliente SignalR
        Runnable clientTask = new SignalRClient(session_id);
        clientTask.run();
        return "[COMPLETADA] Transaccion " + transaccionIndex + " con Session ID: " + session_id;
    }
}
