package Labels;
import java.util.Random;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class GeneratorNum {
    public static String AMT(int longitud) {

        double min = (long) Math.pow(10, longitud - 1);
        double max = (long) Math.pow(10, longitud) - 1;

        if (longitud == 1) {
            min = 1;
            max = 9;
        }
        Random amtRandom = new Random();

        double numAMT = min + (max - min) * amtRandom.nextDouble();

        //Configurar simbolo delimitador
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');

        //Formatear salida a a dos decimales
        DecimalFormat df = new DecimalFormat("0.00", symbol);
        return df.format(numAMT);
    }

    public static void main(String[] args) {
        int longitud = 2;
        String randomAMT = AMT(longitud);
        System.out.println(randomAMT);

    }
}
