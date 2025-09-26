package Utiliti;

import java.util.Random;

public class LabelTransacionID {

    public static String Transacion(int longitud){

        Random numbers =new Random();
        StringBuilder transactionId =new StringBuilder();

        //Bucle para trabajar la longitud del UI
        for (int i =0;  i <longitud; i++){
            int contador = numbers.nextInt(10);
            transactionId.append(contador);
        }

        //5C8E0A4171FE

        int pos1 = 1;
        String char1 = "C";
        transactionId.insert(pos1, char1);

        int pos2 = 3;
        String char2 = "E";
        transactionId.insert(pos2, char2);

        int pos3 = 5;
        String char3 = "A";
        transactionId.insert(pos3, char3);

        int pos4 = 10;
        String char4 = "FE";
        transactionId.insert(pos4, char4);

        return transactionId.toString();

    }

    public static void main(String[] args) {
        String jk = LabelTransacionID.Transacion(7);
        //String j = "5C8E0A4171FE";
        System.out.println(jk);
    }

}
