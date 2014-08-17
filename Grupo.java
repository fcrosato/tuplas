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

public class Grupo implements Runnable {
    private Socket socket = null;
    PrintWriter out;
    BufferedReader in;

    public Grupo(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }


/*

    public void join() throws IOException {
        String msg = SUBJECT_JOINING + SPLIT + myAddress;
        group = InetAddress.getByName(MULTICAST);
        socket = new MulticastSocket(PORT);
        socket.joinGroup(group);

        System.out.println(SUBJECT_JOINING);
        sendMsg(SUBJECT_JOINING);
        servers.add(myAddress);
    }

    public void leave() throws IOException {
        String msg = SUBJECT_LEAVING + SPLIT + myAddress;
        System.out.println(SUBJECT_LEAVING);
        sendMsg(SUBJECT_LEAVING);
        socket.leaveGroup(group);
    }

    public void sendMsg(String msg) throws IOException {
        DatagramPacket datagram = new DatagramPacket(msg.getBytes(), 
                msg.length(), group, 6789);
        socket.send(datagram);
    }

    public String receiveMsg() throws IOException {
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        socket.receive(recv);
        String recieved = new String(recv.getData());
        System.out.println("Recieved> " + recieved);
        return recieved;
    }

*/
    public String getAction(String msg) {
        try {
            String[] msg_split = msg.split(Data.SPLIT);
            String subject = msg_split[0];
            System.out.println("Subject> " + subject);
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
                System.out.println("Joining> Enviando " + all_servers.toString());
                out.println(all_servers.toString());
                TuplaD.carga.put(action, 0);
                for (String s : TuplaD.carga.keySet()) {
                    System.out.println("serv> " + s + " | " + TuplaD.carga.get(s));
                }

            } else if (subject.equals(Data.SUBJECT_CREAR) || 
                    subject.equals(Data.SUBJECT_ELIMINAR) ||
                    subject.equals(Data.SUBJECT_CARDINALIDAD) ||
                    subject.equals(Data.SUBJECT_INSERTAR)) {
                System.out.println("Enviando> " + msg);
                out.println(msg);
                String card = in.readLine();
                return card;
            } else if (subject.equals(Data.SUBJECT_BUSCAR)) {
                System.out.println("Enviando> " + msg);
                out.println(msg);
                System.out.println("=========Hello");
                String inputLine = in.readLine();
                System.out.println("===///////======" + inputLine);
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

            System.err.println("Starting socket server");
            /*
            while ((inputLine = in.readLine()) != null) {
                System.err.println("Client says: " + inputLine);
                getAction(inputLine); 
            }
            */
            inputLine = in.readLine();
            System.err.println("Client says: " + inputLine);
            getAction(inputLine); 
            while(true) {}
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
