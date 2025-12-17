package CER_PAYLINK;

import CER_PAYLINK.RELEASE_NOTE.CloseTransactions;
import Utiliti.LabelTransacionID;

import javax.management.RuntimeMBeanException;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SetPalyink {

    public static void main(String[] args) throws IOException {

        //Generar la Iniciacion y solicitud de la transaccion
        int count = 1;
        for (int i=0; i < count; i++){
            String internal_id = LabelTransacionID.UIDD(12);
            String group_id = "51CD7AD27777";

            String token = AutenticationToken.mapperToken();
            PostPaylink datosConstructor = new PostPaylink(internal_id, group_id);

            //Retornar transaction_ids
            //System.out.println(datosConstructor.postPaylink(token));

            //Generar la transaccion y el SesionId
            String sesionURL = "https://pruebas.app.sypago.net:8086/api/v1/transaction/checkout?id="+datosConstructor.postPaylink(token)+"&blueprint=false";
            // System.out.println(datosConstructor.obtain_sesionId(token, sesionURL));

            for (int h = 0; h < 2; h++){
                String urlweb = "https://pruebas.sypago.net:8086/api/v1/transaction/" + datosConstructor.postPaylink(token);
                String groupids =datosConstructor.getPaylink(token, urlweb);
            }
        }
    }
}
