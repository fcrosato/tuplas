import java.util.List;
import java.util.ArrayList;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.*;
import java.net.*;

public class Grupo implements Runnable {
    private static final String MULTICAST = "235.1.1.1";
    private static final int PORT = 6789;
    public static final String SPLIT = "-";
    public static final String SUBJECT_LEAVING = "Leaving";
    public static final String SUBJECT_JOINING = "Joining";
    public static final String SUBJECT_SET     = "Joining";
    //private MulticastSocket socket;
    private InetAddress group;
    private List<String> servers = new ArrayList<String>();
    String myAddress;
    String _msg;
    private Socket socket = null;

    public Grupo(Socket socket) {
        this.socket = socket;
    }

    public Grupo(String msg) {
        _msg = msg;
    }

    private void print(Object msg) {
        System.out.println(msg.toString());
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

    private int getAction(String msg) {
        String[] msg_split = msg.split(SPLIT);
        String subject = msg_split[0];
        String action = msg_split[1];
        if (subject.equals(SUBJECT_LEAVING)) {
            if (!action.equals(myAddress)) {
                TuplaD._servidores.remove(action);
            }
        } else if (subject.equals(SUBJECT_JOINING)) {
            if (!action.equals(myAddress)) {
                TuplaD._servidores.add(action);
            }
        } else if (subject.equals(SUBJECT_SET)) {
            if (!action.equals(myAddress)) {
                TuplaD._servidores.add(action);
            }
        }
        return 0;    
    }
*/

/*
    @Override
    public void run() {
        try {
            byte[] localIp = InetAddress.getLocalHost().getAddress();
            myAddress = InetAddress.getByAddress(localIp).getHostName();

            getAction(_msg);
            System.out.println(_msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
    @Override
    public void run() {

        try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()));
            ) {
            String inputLine, outputLine;

            System.out.println("Starting socket server");
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client says: " + inputLine);
                out.println(inputLine);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
