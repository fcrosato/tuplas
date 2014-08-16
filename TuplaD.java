import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.Console;

public class TuplaD implements TuplaDInterfaz {
    public static final int SEGMENTADO   = 1;
    public static final int REPLICADO    = 2;
    public static final int PARTICIONADO = 3;

    public static HashMap<String, Grupo> socket_servidor = new HashMap<String, Grupo>();
    public static HashMap<String, Integer> carga = new HashMap<String, Integer>();

    private static final String MULTICAST = "235.1.1.1";
    private static final int PORT = 6789;

    private static MulticastSocket _socket;
    private static InetAddress _group;
    public static String _myAddress;

    public static boolean _coordinador;
//    public static List<String> _servidores;
    public static List<Servidor> _servidores;

    private static String _nombre = "";
    public static Conjuntos _tuplas = new Conjuntos(); 

    public TuplaD() throws RemoteException {}

    public static void print(Object msg) {
        System.out.println(msg.toString());
    }



    //Las operaciones sobre las tuplas pueden manejar un esquema de locking centralizado para proveer la exclusión mutua.

    /**
     * Método que crea una tupla nueva.
     * 
     * @param nombre Identificador del conjunto de tuplas
     * @param dimension Número de elementos de una tupla (debe ser mayor que 2)
     * @param tipo  Indica si es segmentado, replicado o particionado.
     * @param servidores Nombre de las máquinas donde se desea que resida el conjunto de tuplas.  
     * @return true si se crea satisfactoriamente, false en caso contrario.
     */
    public boolean crear(String nombre, int dimension, int tipo, List<String> servidores) {
        String tupla_servidores = "";
        for (Servidor s : _servidores) {
            tupla_servidores += (s.ip + Data.SUBSPLIT); 
        }

        String msg = (nombre + Data.SUBSPLIT + dimension + Data.SUBSPLIT +
                tipo + Data.SUBSPLIT + tupla_servidores);


        for (Grupo g : socket_servidor.values()) {
            g.getAction(msg); 
            //g.getAction();
        }

        print("Creando conjunto " + nombre); 
        _tuplas.addNew(nombre, dimension, tipo, servidores);
        return true;    
    }


    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public boolean eliminar (String nombre) {
        if (_tuplas.exists(nombre)) {
            print("Eliminando conjunto " + nombre); 
            _tuplas.clear();
            return true;
        }
        return false;
    }


    /**
     * Método que inserta una tupla.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public boolean insertar (String nombre, List<String> ti) {
        
        if (_tuplas.exists(nombre)) {
            print("Insertando tupla en el conjunto " + nombre);
            print(ti);
            _tuplas.add(nombre, ti); 
            return true;
        }
        return false;
    }


    /**
     * Método que borra una tupla de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a borrar.
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public boolean borrar (String nombre, String clave) {
        if (_tuplas.exists(nombre)) {
            print("Eliminando tupla "+clave+" en el conjunto "+nombre);
            _tuplas.remove(nombre, clave);
            return true;
        }
        return false;
    }

    /**
      * Método que busca una tupla dentro de un conjunto de tuplas.
      *
      * @param nombre Identificador del conjunto de tuplas
      * @param clave Clave de la tupla a actualizar
      * @return El conjunto de valores de la tupla.
      */
    public List<String> buscar (String nombre, String clave) {
        List<String> result = new ArrayList<String>();
        if (_tuplas.exists(nombre)) {
            print("Buscando elementos de la tupla "+clave+" en el conjunto "+nombre);
            result = _tuplas.getElements(nombre, clave);
        }
        return result;
    }

    /**
      * Método que actualiza un valor de una tupla.
      *
      * @param nombre Identificador del conjunto de tuplas
      * @param clave Clave de la tupla a actualizar
      * @param posicion Posición del valor a actualizar
      * @param valor Valor nuevo del elemento de la tupla.
      * @return true si el valor se actualizó satisfactoriamente, 
                false en caso contrario.
      */
    public boolean actualizar (String nombre, String clave, int posicion, String valor) {
        if (_tuplas.exists(nombre)) {
            print("Actualizando: "+
                "\tconjunto "+nombre +
                "\tclave: "+clave +
                "\tposicion: "+posicion +
                "\tvalor: "+valor);
            _tuplas.set(nombre, clave, posicion, valor);
        }
        return true;
    }

    /**
      * Método para consultar la configuración de un conjunto de tuplas
      *
      * @param nombre Identificador del conjunto de tuplas.
      * @return UInformación de configuración del conjunto de tuplas.
      */
    public String configuracion (String nombre) {
        String conf = _tuplas.config(nombre);
        //= Información de configuración del conjunto de tuplas
        return conf;
    }

    private static void uso() {
        String uso = "./tuplad [nombre]";
        System.err.println(uso);
    }


    public static TuplaDInterfaz registrarse() throws RemoteException {
        TuplaDInterfaz tuplad = new TuplaD();
        TuplaDInterfaz stub =
            (TuplaDInterfaz) UnicastRemoteObject.exportObject(tuplad, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(_nombre, stub);
        System.out.println("TuplaD registrado");
        return tuplad;
    }

    public static void sendMsg(String msg) throws IOException {
        DatagramPacket datagram = new DatagramPacket(msg.getBytes(), 
                msg.length(), _group, 6789);
        _socket.send(datagram);
    }

    public static String receiveMsg() throws IOException {
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        _socket.receive(recv);
        String recieved = new String(recv.getData());
        System.out.println("Recieved> " + recieved);
        return recieved;
    }


    public static void main(String args[]) {
        int portNumber = 10764;
        try {
            _nombre = args[0];
            if (args[1].equals("-c")) {
                _coordinador = true;
                System.out.println("COORDINADOR");
            }
//            _servidores = new ArrayList<String>();
            _servidores = new ArrayList<Servidor>();
            byte[] localIp = InetAddress.getLocalHost().getAddress();
            _myAddress = InetAddress.getByAddress(localIp).getHostAddress();
            _servidores.add(new Servidor(_myAddress, 0));

            if (_coordinador) {
                registrarse();
                while (true) {
                    try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
                        while (true) {
                            Socket service = serverSocket.accept();
                            new Grupo(service).run();
                        }
                    } catch (IOException e) {
                        System.err.println("Could not listen on port " + portNumber);
                        System.exit(-1);
                    }                
                }
            }

            
            print(_myAddress);

            Socket kkSocket = new Socket(args[1], portNumber);
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(kkSocket.getInputStream()));

            String fromServer;
            String fromUser;

            out.println(Grupo.SUBJECT_JOINING + Grupo.SPLIT + _myAddress);
            fromServer = in.readLine();
            String[] all_servers = fromServer.split(Grupo.SPLIT);
            for (int i = 0; i < all_servers.length; i+=2) {
                _servidores.add(new Servidor(all_servers[i], 
                            Integer.parseInt(all_servers[i+1])));
            }


            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));


            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;

                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }



            Nodo n = new Nodo(args[1], portNumber);
            n.run();
            /*
                    print("Hello.");
                    String msg = receiveMsg();
                    Runnable g = new Grupo(msg);
                    g.run();
                }
            }
            Console console = System.console();
            while (true) {
                sendMsg("Testing");
                String input = console.readLine("Enter input:");
            }
*/

        } catch (ArrayIndexOutOfBoundsException e) {
            uso();
        } catch (Exception e) {
            System.err.println("TuplaD exception:");
            e.printStackTrace();
        }
    }
}
