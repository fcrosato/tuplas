import java.util.List;
import java.util.ArrayList;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.lang.StringBuilder;
/**
 * Clase que implementa el hilo que se encargará de la conexión con algún
 * servidor a través de sockets para realizar trabajos de distribución de
 * cargas.
 * @author Fabiola Rosato
 * @author José Delgado
 */
class Coordinador implements Runnable {
    private Socket socket = null;
    PrintWriter out;
    BufferedReader in;

    /** 
      * Constructor de la clase indicando el socket de la conexión
      */
    public Coordinador(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void leave() {
        System.exit(0);
    }

    /**
     * Método que dado un mensaje, realiza una acción
     *
     * @param msg Mensaje a ser procesado.
     * @return retorna la respuesta del servidor al que se está conectado,
     *         si aplica.
     */
    public String getAction(String msg) {
        try {
            String[] msg_split = msg.split(Data.SPLIT);
            String subject = msg_split[0];
            Data.print(TuplaD._myName, "Subject: " + subject);

            if (subject.equals(Data.SUBJECT_ROLLBACK)) {
                out.println(msg);
                return "";    
            }

            String action = msg_split[1];

            if (subject.equals(Data.SUBJECT_LEAVING)) {
                TuplaD._servidores.remove(action);

            } else if (subject.equals(Data.SUBJECT_JOINING)) {
                TuplaD.socket_servidor.put(action, this);

                StringBuilder all_servers = new StringBuilder();
                for (Servidor s : TuplaD._servidores) {
                    all_servers.append(s.ip).append(Data.SPLIT).append(s.carga).append(Data.SPLIT);
                }
                TuplaD._servidores.add(new Servidor(action, 0));
                Data.print(TuplaD._myName, "Enviando " + all_servers.toString());
                out.println(all_servers.toString());
                TuplaD.carga.put(action, 0);
                for (String s : TuplaD.carga.keySet()) {
                    Data.print(TuplaD._myName, "servidor: " + s + " | carga: " + TuplaD.carga.get(s));
                }

            } else if (subject.equals(Data.SUBJECT_CREAR) || 
                    subject.equals(Data.SUBJECT_ACTUALIZAR)) { 
                Data.print(TuplaD._myName, "Enviando: " + msg);
                out.println(msg);
                String inputLine = in.readLine();
                return inputLine;
            } else if (subject.equals(Data.SUBJECT_ELIMINAR) ||
                    subject.equals(Data.SUBJECT_BORRAR) ||
                    subject.equals(Data.SUBJECT_INSERTAR)) {
                Data.print(TuplaD._myName, "Enviando: " + msg);
                out.println(msg);
                String modificados = in.readLine();
                return modificados;
            } else if (subject.equals(Data.SUBJECT_CARDINALIDAD)) {
                Data.print(TuplaD._myName, "Enviando> " + msg);
                out.println(msg);
                String card = in.readLine();
                return card;
            } else if (subject.equals(Data.SUBJECT_BUSCAR)) {
                Data.print(TuplaD._myName, "Enviando> " + msg);
                out.println(msg);
                String inputLine = in.readLine();
                return inputLine;
            } 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";    
    }

    @Override
    public void run() {

        try { 
            String inputLine, outputLine;

            Data.print(TuplaD._myName, "Starting socket server");
            inputLine = in.readLine();
            Data.print(TuplaD._myName, "Client says: " + inputLine);
            getAction(inputLine); 
            while(true) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
