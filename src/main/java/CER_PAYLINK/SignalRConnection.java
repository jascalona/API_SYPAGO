package CER_PAYLINK;

/**
 * Clase base abstracta para la conexión SignalR.
 * Define el contrato para el metodo 'invoke', que es crucial para los
 * pasos de intercambio de claves y obtención de datos de transacción.
 * */
public abstract class SignalRConnection {

    protected final String sessionId;

    /**
     * Constructor. Requiere un ID de sesión.
     */
    public SignalRConnection(String sessionId) {
        this.sessionId = "66eb0ab6-0a7d-4081-8a89-53a656806e55";
    }

    /**
     * Realiza la invocación a un metodo del hub SignalR (RPC) y espera la respuesta.
     * En la implementación real del WebSocket, esto enviaría un mensaje de 'Invocation'.
     * @param method El nombre del metodo del hub a invocar.
     * @param args Los argumentos a pasar al metodo del hub.
     * @return Los datos de respuesta cifrados del servidor (como un array de bytes).
     */
    public abstract byte[] invoke(String method, Object... args) throws Exception;
}
