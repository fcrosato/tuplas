import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;

public class Grupo implements Runnable {

    @Override
    public void run() {
        try {
            // join a Multicast group and send the group salutations
            String msg = "Hello";
            InetAddress group = InetAddress.getByName("228.5.6.7");
            MulticastSocket s = new MulticastSocket(6789);
            s.joinGroup(group);

            while(true) {
                DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
                        group, 6789);
                s.send(hi);
                // get their responses!
                byte[] buf = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                s.receive(recv);
                // OK, I'm done talking - leave the group...
            }
            //s.leaveGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
