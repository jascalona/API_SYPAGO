package Labels;
import java.util.Random;


public class TransactionIdGV2 {
    public static String Transaction(int longitud){


        Random idRandom = new Random();
        StringBuilder transaction = new StringBuilder();

        //Generar id de digitos, hexadecimal (0-9)
        for(int i= 0; i < longitud; i++){
            long id = idRandom.nextInt(10);
            transaction.append(id);
        }

        int pos1 = 0;
        char pos1stc = 'E';
        transaction.insert(pos1,pos1stc);

        int pos2 = 3;
        char pos2stc = 'F';
        transaction.insert(pos2,pos2stc);

        int pos3 = 7;
        String pos3stc = "BD";
        transaction.insert(pos3,pos3stc);

        System.out.println("Transaction: " + transaction);
        System.out.println("Longitud: " + transaction.length());

        return String.valueOf(transaction);
    }

    public static void main(String [] args){
        TransactionIdGV2.Transaction(8);

    }

}
