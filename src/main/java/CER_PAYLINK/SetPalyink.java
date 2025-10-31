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

        //Generar la Iniciacion y solicitud de la transaccion
        String requestURL = "https://pruebas.sypago.net:8086/api/v1/transaction/" + datosConstructor.postPaylink(token);;
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
