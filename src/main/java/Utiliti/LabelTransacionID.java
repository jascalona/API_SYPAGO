package Utiliti;

import java.util.Random;

public class LabelTransacionID {

    public static String UIDD(int longitud){

        Random random =new Random();
        String charts= "ABCDEF0123456789";
        StringBuilder transactionId =new StringBuilder();

        //Bucle para trabajar la longitud del UI
        for (int i =0; i < longitud; i++){
            int indice = random.nextInt(charts.length());
            char iterador = charts.charAt(indice);
            transactionId.append(iterador);
        }
        return transactionId.toString();

    }

    public static void main(String[] args) {
        System.out.println(LabelTransacionID.UIDD(13));
    }

}
