import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Test {
    public static void main(String args[]) {

        try {
            RandomAccessFile log = new RandomAccessFile("test.txt", "rw");
            log.seek(log.length() - 1);
            String entrada = log.readLine();
            System.out.println(entrada);
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo.");
        } catch (IOException e) {
            System.out.println("Problemas leyendo el log.");
        }
    }

}
