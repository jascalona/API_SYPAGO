package CER_PAYLINK;

import Utiliti.LabelTransacionID;

import javax.management.RuntimeMBeanException;
import java.io.IOException;

public class SetPalyink {
    public static void main(String[] args) throws IOException {

        String internal_id = LabelTransacionID.UIDD(12);
        String group_id = LabelTransacionID.UIDD(12);

        String token = AutenticationToken.mapperToken();
        PostPaylink datosConstructor = new PostPaylink(internal_id, group_id);

        try {
            System.out.println("Iniciando PayLink...");
            String response = datosConstructor.postPaylink(token);
            System.out.println("Request Completed" );
            System.out.println("Respuesta del Servidor: " + response);
        }
        catch (IOException e){
            System.out.println("Se ha producido un error I/O: "+ e.getMessage());
            e.printStackTrace();
        }catch (RuntimeException e){
            System.out.println("Se produjo un error durante la solicitud HTTP: " + e.getMessage());
        }

        System.out.println("---------------------------------------------------------------------------");
        //Solicitar el estado del PayLink
        String requestURL = "https://pruebas.sypago.net:8086/api/v1/transaction/" + datosConstructor.getInternal_id();
        try {
            System.out.println("Solicitando el estado de la transaccion");
            String responde = datosConstructor.getPaylink(token, requestURL);
            System.out.println("Respuesta del PayLink: " + responde);
        }catch (Exception e){
            System.out.println("Se ha producido un error I/O: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
