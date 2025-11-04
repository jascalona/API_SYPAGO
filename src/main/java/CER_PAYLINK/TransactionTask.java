package CER_PAYLINK;

import Utiliti.LabelTransacionID;

import java.util.concurrent.Callable;

public class TransactionTask implements Callable<String> {
    private final int transaccionIndex;

    public TransactionTask(int transaccionIndex) {
        this.transaccionIndex = transaccionIndex;
    }

    @Override
    public String call() throws Exception {
        // *********** LÓGICA CONCURRENTE ***********
        String internal_id = LabelTransacionID.UIDD(12);
        String group_id = LabelTransacionID.UIDD(12);

        String token = AutenticationToken.mapperToken();
        PostPaylink datosConstructor = new PostPaylink(internal_id, group_id);

        String transaction_id = datosConstructor.postPaylink(token);

        String url_sessionId = "https://pruebas.app.sypago.net:8086/api/v1/transaction/checkout?id=" + transaction_id + "&blueprint=false";

        String session_id = datosConstructor.obtain_sesionId(token, url_sessionId);

        if (session_id == null || session_id.isEmpty()) {
            return "[ERROR] Tarea " + transaccionIndex + ": El SessionId obtenido es nulo o vacío.";
        }

        // Iniciar el cliente SignalR
        Runnable clientTask = new RealSignalRClient(session_id);
        clientTask.run();

        return "[COMPLETADA] Transaccion " + transaccionIndex + " con Session ID: " + session_id;
    }
}
