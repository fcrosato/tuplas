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

/**
 * Clase que implementa el servidor RMI de TuplasD, junto con
 * su faceta como coordinador o nodo distribuido.
 * @author Fabiola Rosato
 * @author José Delgado
 */
public class TuplaD implements TuplaDInterfaz {
    public static HashMap<String, Coordinador> socket_servidor = new HashMap<String, Coordinador>();
    public static HashMap<String, Integer> carga = new HashMap<String, Integer>();

    public static String _myAddress;
    public static List<Servidor> _servidores;

    private static String _hostCoordinador = "";
    private static String _nombre          = "";
    public static Conjuntos _tuplas        = new Conjuntos();
    public static boolean _coordinador     = false;
    private static int _puerto;

    /**
     * Constructor por defecto de la clase.
     */
    public TuplaD() throws RemoteException {}


    /**
     * Método que crea una tupla nueva.
     * 
     * @param nombre Identificador del conjunto de tuplas
     * @param tipo  Indica si es segmentado, replicado o particionado.
     * @return true si se crea satisfactoriamente, false en caso contrario.
     */
    public boolean crear(String nombre, int tipo) {
        List<String> servidores = new ArrayList<String>();

        String tuplaServidores = "";
        for (Servidor s : _servidores) {
            servidores.add(s.ip);
            tuplaServidores += (s.ip + Data.SUBSPLIT); 
        }

        String msg = (nombre + Data.SUBSPLIT + tipo + Data.SUBSPLIT + tuplaServidores);

        for (Coordinador g : socket_servidor.values()) {
            String commit = g.getAction(Data.SUBJECT_CREAR + Data.SPLIT + msg);

        }

        Data.print("Creando conjunto " + nombre);
        _tuplas.addNew(nombre, 0, tipo, servidores);
        return true;
    }

    private void actualizarCarga(String servidor, int delta) {
        int cargaServidor = carga.get(servidor) + delta;
        carga.put(servidor, cargaServidor);
    }

    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public boolean eliminar (String nombre) {
        String msg = Data.SUBJECT_ELIMINAR + Data.SPLIT + nombre;
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        int eliminados = 0;
        for (String s : tuplaServidores) {
            if (!s.equals(_myAddress)) {
                Coordinador g = socket_servidor.get(s);
                eliminados = Integer.parseInt(g.getAction(msg));
                Data.print("Eliminando conjunto " + nombre); 
            } else {
                eliminados = _tuplas.clear(nombre);
                Data.print("Eliminando conjunto " + nombre); 
            }
            actualizarCarga(s, -eliminados);
        }
        return true;
    }

    /**
     * Método que busca el servidor menos cargado entre una lista de 
     * servidores.
     *
     * @param servidores Lista de servidores 
     * @return un String con la dirección del servidor menos cargado 
     */
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
     * Método que inserta una tupla cuando su conjunto es de tipo segmentado     
     *
     * @param tuplaServidores Lista de servidores en los que está segmentado
     *                        el conjunto de la tupla.
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public boolean insertarSegmentado(String nombre, List<String> ti, List<String> tuplaServidores) {
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
        String tupla = "";
        for (String s : tuplaServidores) {
            int cargaServidor = 0;
            tupla = (ti.get(0) + Data.SUBSPLIT);
            int tam = modConjuntos != 0 && i == (numeroServidores - 1) ? 
                tamConjuntos + modConjuntos : tamConjuntos;
            for (int j = 1; j < tam; j++) {
                tupla += (ti.get(tuplaIndex++) + Data.SUBSPLIT);
                cargaServidor++;
                if (tuplaIndex == ti.size()) {
                    break;
                }
            }
            i++;

            int insertados = 0;
            if (!s.equals(_myAddress)) {
                Coordinador g = socket_servidor.get(s);
                insertados = Integer.parseInt(g.getAction(msg + tupla));
            } else {
                String[] t = tupla.split(Data.SUBSPLIT);
                List<String> listaTupla = new ArrayList<String>();
                for (int j=0; j<t.length; j++) {
                    listaTupla.add(t[j]);
                }
                Data.print("Insertando conjunto " + nombre + "> " + listaTupla.toString()); 
                insertados = _tuplas.add(nombre, listaTupla); 
            }
            actualizarCarga(s, insertados);
        }
        return true;
    }


    /**
     * Método que inserta una tupla.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public String insertar (String nombre, List<String> ti) {
        boolean commit = true;
        int tipo = _tuplas.tipo(nombre);
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        String tupla = "";
        for (String t : ti) {
            tupla += t + Data.SUBSPLIT;
        }
        String msg = Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SPLIT + tupla;

        if (tipo == Data.REPLICADO) {
            int cargaServidor = ti.size() - 1;
            int insertados;
            for (String s: tuplaServidores) {
                if (!s.equals(_myAddress)) {
                    Coordinador g = socket_servidor.get(s);
                    try {
                        insertados = Integer.parseInt(g.getAction(msg));
                    } catch (NumberFormatException e) {
                        commit = false;
                        break;
                    }
                } else {
                    Data.print("Insertando conjunto " + nombre); 
                    insertados = _tuplas.add(nombre, ti); 
                }
                actualizarCarga(s, insertados);

            }
        } else if (tipo == Data.PARTICIONADO) {
            int insertados = 0;
            String servidor = servidorMenosCargado(tuplaServidores);
            if (!servidor.equals(_myAddress)) {
                Coordinador g = socket_servidor.get(servidor);
                insertados = Integer.parseInt(g.getAction(msg));
            } else {
                Data.print("Insertando conjunto " + nombre); 
                insertados = _tuplas.add(nombre, ti);
            }
            actualizarCarga(servidor, insertados);
        } else if (tipo == Data.SEGMENTADO) {
            insertarSegmentado(nombre, ti, tuplaServidores);

        }

        if (! commit ) {
            int eliminados = _tuplas.rollback(nombre, ti);
            actualizarCarga(_myAddress, -eliminados);
            rollback (tuplaServidores);
            return "Ocurrió alguna falla en nuestros servidores. Intente de nuevo más tarde.";
        }
        return "La tupla se agregó satisfactoriamente";
    }

    public void rollback(List<String> servidores) {
        for (String s : servidores) {
            try {
                Coordinador g = socket_servidor.get(s);
                int eliminados = Integer.parseInt(g.getAction(Data.SUBJECT_ROLLBACK));
                actualizarCarga(s, -eliminados);
            } catch (NumberFormatException e) {
                System.err.println("Falla en el servidor: " + s);
            }
        }
    }

    /**
     * Método que borra una tupla de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a borrar.
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public boolean borrar (String nombre, String clave) {
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        String msg = Data.SUBJECT_BORRAR + Data.SPLIT + nombre + Data.SUBSPLIT + clave;
        int borrados = 0;
        for (String s : tuplaServidores) {
            if (!s.equals(_myAddress)) {
                Coordinador g = socket_servidor.get(s);
                borrados = Integer.parseInt(g.getAction(msg));
                Data.print("Eliminando conjunto " + nombre); 
            } else {
                borrados = _tuplas.remove(nombre, clave);
                Data.print("Eliminando conjunto " + nombre); 
            }
            actualizarCarga(s, borrados);
        }
        return true;
    }

    /**
     * Método que busca una tupla dentro de un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a actualizar
     * @return El conjunto de valores de la tupla.
     */
    public List<String> buscar (String nombre, String clave) {
        int tipo = _tuplas.tipo(nombre);
        String tupla = "";
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        String msg = Data.SUBJECT_BUSCAR + Data.SPLIT + nombre + Data.SUBSPLIT + clave;
        for (String s : tuplaServidores) {
            if (!s.equals(_myAddress)) {
                Coordinador g = socket_servidor.get(s);
                tupla += g.getAction(msg);
            } else {
                List<String> tuplaLocal = _tuplas.getElements(nombre, clave);
                if (tuplaLocal != null) {
                    for (String t : tuplaLocal) {
                        tupla += t + Data.SUBSPLIT;
                    }
                }
            }
            if(!tupla.equals("") && tipo == Data.REPLICADO) {
                break;
            }
        }
        List<String> resultado = new ArrayList<String>();
        String[] elementos = tupla.split(Data.SUBSPLIT);
        for (int i = 0; i < elementos.length; i++) {
            resultado.add(elementos[i]);
        }
        return resultado;
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

        String msg = Data.SUBJECT_ACTUALIZAR + Data.SPLIT + nombre + Data.SUBSPLIT + clave + 
            Data.SUBSPLIT + posicion + Data.SUBSPLIT + valor;

        int tipo = _tuplas.tipo(nombre);
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        if (tipo == Data.REPLICADO || tipo == Data.PARTICIONADO) {
            for (String s : tuplaServidores) {
                if (!s.equals(_myAddress)) {
                    Coordinador g = socket_servidor.get(s);
                    g.getAction(msg);
                } else {
                    _tuplas.set(nombre, clave, posicion, valor);
                }
            }
        } else if (tipo == Data.SEGMENTADO) {
            int offset = 0;
            String getCantidad = Data.SUBJECT_CARDINALIDAD + Data.SPLIT + 
                nombre + Data.SUBSPLIT + clave;
            for (String s : tuplaServidores) {
                if (!s.equals(_myAddress)) {
                    Coordinador g = socket_servidor.get(s);
                    int cardinalidad = Integer.parseInt(g.getAction(getCantidad));
                    offset += cardinalidad;
                    if (posicion < offset) {
                        msg += Data.SUBSPLIT + (offset - cardinalidad);
                        g.getAction(msg);
                        break;
                    }
                } else {
                    int cardinalidad =  _tuplas.cardinalidad(nombre, clave);
                    offset += cardinalidad;

                    if (posicion < offset) {
                        _tuplas.set(nombre, clave, posicion - (offset - cardinalidad), valor);
                        break;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Método para consultar la configuración de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas.
     * @return Información de configuración del conjunto de tuplas.
     */
    public String configuracion (String nombre) {
        String conf = _tuplas.config(nombre);
        return conf;
    }

    /**
     * Método que imprime el uso correcto del programa
     */
    private static void uso() {
        String uso = "java TuplaD -s [nombre] -p [puerto] -c \n" +
            "java TuplaD -s [nombre] -p [puerto] -n [coordinador]";
        System.err.println(uso);
    }


    /**
     * Método que registra el servicio RMI.
     */
    public static TuplaDInterfaz registrarse() throws RemoteException {
        TuplaDInterfaz tuplad = new TuplaD();
        TuplaDInterfaz stub =
            (TuplaDInterfaz) UnicastRemoteObject.exportObject(tuplad, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(_nombre, stub);
        System.out.println("TuplaD registrado");
        return tuplad;
    }



    /** 
     * Método que chequea si los parámetros de entrada son 
     * correctos.
     * 
     * @param args Argumentos a revisar
     * @return true si los parámetros son correctos, false
     *         en caso contrario.
     */
    private static boolean argsOk(String args[]) {
        if (args.length <= 0) {
            return false;
        }
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-s")) {
                    _nombre = args[i+1];
                    i++;

                } else if (args[i].equals("-c")) {
                    if (args.length < 5) {
                        return false;
                    }
                    _coordinador = true;

                } else if (args[i].equals("-n")) {
                    if (args.length < 6) {
                        return false;
                    }
                    _hostCoordinador = args[i+1];
                    i++;

                } else if (args[i].equals("-p")) {
                    _puerto = Integer.parseInt(args[i+1]);
                    i++;
                }
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return false;
        }
        return true;
    }

    /** 
     * Main del programa.
     */
    public static void main(String args[]) {
        if (! argsOk(args) ) {
            uso();
            System.exit(-1);
        }

        try {
            _servidores = new ArrayList<Servidor>();
            byte[] localIp = InetAddress.getLocalHost().getAddress();
            _myAddress = InetAddress.getByAddress(localIp).getHostAddress();
            _servidores.add(new Servidor(_myAddress, 0));
            carga.put(_myAddress, 0);
            Data.print(_myAddress);

            if (_coordinador) {
                registrarse();
                while (true) {
                    try (ServerSocket serverSocket = new ServerSocket(_puerto)) { 
                        while (true) {
                            System.out.println("Esperando conexión");
                            Socket service = serverSocket.accept();
                            System.out.println("Conexión aceptada");
                            Thread c = new Thread(new Coordinador(service));
                            c.start();
                        }
                    } catch (IOException e) {
                        System.err.println("Error escuchando por el puerto " + _puerto);
                        System.exit(-1);
                    }                
                }
            } else {
                Socket kkSocket = new Socket(_hostCoordinador, _puerto);
                Nodo n = new Nodo(kkSocket);
                n.run();
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            uso();
        } catch (Exception e) {
            System.err.println("TuplaD exception:");
            e.printStackTrace();
        }
    }
}
