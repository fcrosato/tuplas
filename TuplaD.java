import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;

public class TuplaD implements TuplaDInterfaz {
    public static HashMap<String, Grupo> socket_servidor = new HashMap<String, Grupo>();
    public static HashMap<String, Integer> carga = new HashMap<String, Integer>();

    public static String _myAddress;
    public static boolean _coordinador;
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
    public boolean crear(String nombre, int dimension, int tipo) {
        List<String> servidores = new ArrayList<String>();

        String tuplaServidores = "";
        for (Servidor s : _servidores) {
            tuplaServidores += (s.ip + Data.SUBSPLIT); 
            servidores.add(s.ip);
        }

        String msg = (nombre + Data.SUBSPLIT + dimension + Data.SUBSPLIT +
                tipo + Data.SUBSPLIT + tuplaServidores);


        for (Grupo g : socket_servidor.values()) {
            g.getAction(Data.SUBJECT_CREAR + Data.SPLIT + msg); 
            //g.getAction();
        }

        print("Creando conjunto " + nombre); 
        _tuplas.addNew(nombre, dimension, tipo, servidores);
        Data.print(_tuplas);
        return true;    
    }


    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public boolean eliminar (String nombre) {
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        for (String s : tuplaServidores) {
            System.out.println("Servidor> " +s);
            if (!s.equals(_myAddress)) {
                Grupo g = socket_servidor.get(s);
                g.getAction(Data.SUBJECT_ELIMINAR + Data.SPLIT + nombre);
                print("Eliminando conjunto " + nombre); 
            } else {
                _tuplas.clear(nombre);
                print("Eliminando conjunto " + nombre); 
            }
        }
        Data.print(_tuplas);
        return true;
    }


    private String servidorMenosCargado(List<String> servidores) {
        String minServidor = servidores.get(0);
        int minCarga = carga.get(minServidor);
        for (String s : servidores) {
            if (carga.get(s) < minCarga) {
                minCarga = carga.get(s);
                minServidor = s;
            }
        }
        return minServidor;
    }


    /**
     * Método que inserta una tupla.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public boolean insertar (String nombre, List<String> ti) {
        int tipo = _tuplas.tipo(nombre);
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        String tupla = "";
        for (String t : ti) {
            tupla += t + Data.SUBSPLIT;
        }

        if (tipo == Data.REPLICADO) {
            int cargaServidor = ti.size() - 1;
            String msg = Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SPLIT + tupla;
            for (String s: tuplaServidores) {
                if (!s.equals(_myAddress)) {
                    Grupo g = socket_servidor.get(s);
                    g.getAction(msg);
                } else {
                    print("Insertando conjunto " + nombre); 
                    _tuplas.add(nombre, ti); 
                }
                carga.put(s, cargaServidor);

            }
        } else if (tipo == Data.PARTICIONADO) {
            int cargaServidor = ti.size() - 1;
            String msg = Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SPLIT + tupla;
            String servidor = servidorMenosCargado(tuplaServidores);
            if (!servidor.equals(_myAddress)) {
                Grupo g = socket_servidor.get(servidor);
                g.getAction(msg);
            } else {
                print("Insertando conjunto " + nombre); 
                _tuplas.add(nombre, ti);
            }
            carga.put(servidor, cargaServidor);
        } else if (tipo == Data.SEGMENTADO) {
            String msg = Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SPLIT;
            int elementos = ti.size() - 1;
            int numeroServidores = tuplaServidores.size();
            int tamConjuntos = elementos / numeroServidores + 1;
            int modConjuntos = elementos % numeroServidores;

            if (tamConjuntos == 1) {
                numeroServidores = 1;
            }


            int i = 0;
            int tuplaIndex = 1;
            tupla = "";
            for (String s : tuplaServidores) {
                int cargaServidor = 0;
                tupla = (ti.get(0) + Data.SUBSPLIT);
                int tam = modConjuntos != 0 && i == (numeroServidores - 1) ? 
                    tamConjuntos + modConjuntos : tamConjuntos;
                for (int j = 1; j < tam; j++) {
                    System.out.println(i * tamConjuntos + j);
                    tupla += (ti.get(tuplaIndex++) + Data.SUBSPLIT);
                    cargaServidor++;
                    if (tuplaIndex == ti.size()) {
                        break;
                    }
                }
                i++;

                if (!s.equals(_myAddress)) {
                    Grupo g = socket_servidor.get(s);
                    g.getAction(msg += tupla);
                } else {
                    String[] t = tupla.split(Data.SUBSPLIT);
                    List<String> listaTupla = new ArrayList<String>();
                    for (int j=0; j<t.length; j++) {
                        listaTupla.add(t[j]);
                    }
                    print("Insertando conjunto " + nombre + "> " + listaTupla.toString()); 
                    _tuplas.add(nombre, listaTupla); 
                }
                carga.put(s, cargaServidor);
            }

        }

        return true;
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
            print(_myAddress);

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
            
            Socket kkSocket = new Socket(args[1], portNumber);
            Nodo n = new Nodo(kkSocket);
            n.run();

        } catch (ArrayIndexOutOfBoundsException e) {
            uso();
        } catch (Exception e) {
            System.err.println("TuplaD exception:");
            e.printStackTrace();
        }
    }
}
