import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


public class Log {
    private static RandomAccessFile log;
    private static PrintWriter pw;

    Log() throws FileNotFoundException {
        try {
            pw = new PrintWriter(Data.LOG_PATH);
            log = new RandomAccessFile(Data.LOG_PATH, "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeLog(String logEntry) throws IOException {
        pw.println(logEntry);
        pw.flush();
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
                log.seek(log.length() - (++i));
                entrada = log.readLine();
            }
            
            long result = i == length ? 0 : length - i + 1;
            return result;

        } catch (FileNotFoundException e) {
            Data.printErr("Log", "No se encontró el archivo.");
        } catch (IOException e) {
            e.printStackTrace();
            Data.printErr("Log", "Problemas leyendo el log.");
        }
        return -1;
    }

    public String readEntry() {
        try {
            long offset = nextLogEntryPosition();
            log.seek(offset);
            String entry = log.readLine();
            Data.printErr("Log", entry);
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
