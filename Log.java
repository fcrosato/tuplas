import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;


public class Log {
    private static RandomAccessFile log;
    private static BufferedWriter logWriter;

    Log() throws FileNotFoundException {
        try {
        logWriter = new BufferedWriter(new FileWriter(new File(Data.LOG_PATH), true));
        log = new RandomAccessFile(Data.LOG_PATH, "rw");
        } catch (Exception e) {
        }
    }

    public void writeLog(String logEntry) throws IOException {
        /*
        long length = log.length();
        log.seek(length);
        log.writeUTF(logEntry);
        */
        logWriter.write(logEntry);
        logWriter.newLine();
    }

    public void redoLog() {

    }

    public long nextLogEntryPosition() {
        try {

            long length = log.length();

            if (length == 0) {
                return 0;
            }

            long i = 1;
            log.seek(length - 2);
            String entrada = log.readLine();

            while (!entrada.isEmpty() && i != length) {
                System.out.println(i);
                log.seek(log.length() - (++i));
                entrada = log.readLine();
            }
            
            long result = i == length ? 0 : length - i + 1;
            return result;

        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problemas leyendo el log.");
        }
        return -1;
    }

    public String readEntry() {
        try {
            long offset = nextLogEntryPosition();
            log.seek(offset);
            String entry = log.readLine();
            System.out.println(entry);
            return entry;
        } catch(IOException e) {
        }
        return "IOException";
    }

    public void removeEntry(long offset) throws IOException {
        long position = offset <= 0 ? 0 : offset;
        log.setLength(position);
    }

    /*
    public static void main(String args[]) {
        try {
            _fileName = "test.txt";
            log = new RandomAccessFile(_fileName, "rw");

            System.out.println(log.length());

            writeLog("Hola\n");
            writeLog("Hola\n");

            long offset = nextLogEntryPosition();
            String entry = readEntry(offset);

            removeEntry(offset);

            offset = nextLogEntryPosition();
            entry = readEntry(offset);

            removeEntry(offset);

            offset = nextLogEntryPosition();
            entry = readEntry(offset);
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problemas leyendo el log.");
        }

    }
    */

}
