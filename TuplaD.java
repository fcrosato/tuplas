import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.ConnectException;

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
    public static Log log;

    public static String _myAddress;
    public static String _myName;
    public static List<Servidor> _servidores;

    private static String _hostCoordinador = "";
    private static String _nombre          = "TuplaD";
    public static Conjuntos _tuplas        = new Conjuntos();
    public static boolean _coordinador     = false;
    private static int _puerto;

    public static void writeLog(String accion) {
        try {
            log.writeLog(accion);
        } catch(IOException e) {
            Data.printErr(_myName, "Error escribiendo en el log.");
        }
    }
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
    public String crear(String nombre, int tipo) {
        List<String> servidores = new ArrayList<String>();

        String msg = Data.SUBJECT_CREAR + Data.SPLIT + nombre + Data.SUBSPLIT + tipo; 

        for (String s : socket_servidor.keySet()) {
            Coordinador c = null;
            try {
                c = socket_servidor.get(s);
                int exito = Integer.parseInt(c.getAction(msg));
                servidores.add(s);
            } catch (NumberFormatException | NullPointerException e) {
                Data.printErr(_myName, Data.ERR_CREAR);
                Data.printErr(_myName, Data.ERR_SERVIDOR + s);
            }
        }

        servidores.add(_myName);
        ConjuntoTupla cjto = _tuplas.addNew(nombre, 0, tipo, servidores);
        writeLog(Data.SUBJECT_CREAR + Data.SPLIT + nombre + Data.SUBSPLIT + cjto.log());
        Data.print(_myName, "Creando conjunto " + nombre); 
        Data.print(_myName, Data.EXITO_CREAR);
        return Data.EXITO_CREAR; 
    }

    private void actualizarCarga(String servidor, int delta) {
        try {
            int cargaServidor = carga.get(servidor) + delta;
            carga.put(servidor, cargaServidor);
        } catch (NullPointerException e) {
            carga.remove(servidor);
        }
    }


    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public String eliminar (String nombre) {
        if (! _tuplas.exists(nombre)) {
            return Data.ERR_EXISTE + nombre;
        }
        ConjuntoTupla cjto = _tuplas.get(nombre);
        List<String> tuplaServidores = cjto.servidores(); 
        int tipo = cjto.tipo();
        boolean commit = true;

        String msg = Data.SUBJECT_ELIMINAR + Data.SPLIT + nombre;

        int eliminados = 0;
        for (String s : tuplaServidores) {
            Coordinador c = null;
            if (!s.equals(_myName)) {
                try {
                    c = socket_servidor.get(s);
                    eliminados = Integer.parseInt(c.getAction(msg));
                } catch (NumberFormatException | NullPointerException e) {
                    Data.printErr(_myName, Data.ERR_SERVIDOR + s);
                    socket_servidor.remove(s);
                }
            } else {
                eliminados = _tuplas.clear(nombre);
                writeLog(Data.SUBJECT_ELIMINAR + Data.SPLIT + nombre + Data.SUBSPLIT + cjto.log());
            }
            actualizarCarga(s, -eliminados);
        }

        Data.print(TuplaD._myName, "Eliminando conjunto " + nombre); 
        Data.print(_myName, Data.EXITO_ELIMINAR);
        return Data.EXITO_ELIMINAR; 
    }

    /**
     * Método que busca el servidor menos cargado entre una lista de 
     * servidores.
     *
     * @param servidores Lista de servidores 
     * @return un String con la dirección del servidor menos cargado 
     */
    private String servidorMenosCargado(List<String> servidores) {
        try {
            String minServidor = servidores.get(0);
            int minCarga = carga.get(minServidor);
            for (String s : servidores) {
                if (carga.get(s) < minCarga) {
                    minCarga = carga.get(s);
                    minServidor = s;
                }
            }
            return minServidor;
        }catch(NullPointerException e) {
            return "";
        }
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
        if (tuplaServidores.isEmpty()) {
            return false;
        }
        String msg = Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SPLIT;

        int i = 0, tuplaIndex = 1, elementos = ti.size() - 1;
        int numeroServidores = tuplaServidores.size();
        int tamConjuntos = elementos / numeroServidores + 1;
        int modConjuntos = elementos % numeroServidores;
        if (tamConjuntos == 1) numeroServidores = 1; 
        boolean commit = true;

        List<String> servidores = new ArrayList<String>();

        String tupla = "";
        for (String s : tuplaServidores) {
            tupla = (ti.get(0) + Data.SUBSPLIT);
            int tam = modConjuntos != 0 && i == (numeroServidores - 1) ? 
                tamConjuntos + modConjuntos : tamConjuntos;
            for (int j = 1; j < tam; j++) {
                tupla += (ti.get(tuplaIndex++) + Data.SUBSPLIT);
                if (tuplaIndex == ti.size()) {
                    break;
                }
            }
            i++;
            System.out.println(tupla);

            int insertados = 0;
            if (!s.equals(_myName)) {
                Coordinador g = null;
                try {
                    g = socket_servidor.get(s);
                    insertados = Integer.parseInt(g.getAction(msg + tupla));
                    servidores.add(s);
                } catch (NumberFormatException | NullPointerException e) {
                    commit = false;
                    socket_servidor.remove(s);
                    carga.remove(s);
                    Data.printErr(_myName, Data.ERR_SERVIDOR + s);
                    break;
                }
            } else {
                Data.print(TuplaD._myName, "Insertando " + tupla + " en el conjunto " + nombre);
                writeLog(msg + tupla);
                String[] elementosTi = tupla.split(Data.SUBSPLIT);
                List<String> tiLocal = new ArrayList<String>(); 
                for (int j = 0; j < elementosTi.length; j++) {
                    tiLocal.add(elementosTi[j]);
                }
                insertados = _tuplas.add(nombre, tiLocal); 
                servidores.add(s);
            }
            actualizarCarga(s, insertados);
        }
        if (! commit) {
            int eliminados = _tuplas.rollback(nombre, ti);
            actualizarCarga(_myName, -eliminados);
            rollback(servidores, Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SUBSPLIT + tupla);
        }
        writeLog(msg);
        return commit;
    }


    /** 
      * Método que concatena una lista de servidores en un String
      * 
      * @param ti Tupla en forma de lista
      * @return la tupla en un String
      */
    private String concatenarTupla(List<String> ti) {
        String tupla = "";
        for (String t : ti) {
            tupla += t + Data.SUBSPLIT;
        }
        return tupla;
    }
    /**
     * Método que inserta una tupla cuando su conjunto es de tipo replicado 
     *
     * @param tuplaServidores Lista de servidores en los que está segmentado
     *                        el conjunto de la tupla.
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    private boolean insertarReplicado(String nombre, List<String> ti, List<String> servidores, String msg) {
        int insertados = 0;
        List<String> servidoresExitosos = new ArrayList<String>();

        for (String s: servidores) {
            try {
                if (!s.equals(_myName)) {
                    Coordinador g = socket_servidor.get(s);
                    insertados = Integer.parseInt(g.getAction(msg));
                } else {
                    writeLog(msg);
                    Data.print(TuplaD._myName, "Insertando " + ti.toString() + " en el conjunto " + nombre);
                    insertados = _tuplas.add(nombre, ti); 
                }
                servidoresExitosos.add(s);
                actualizarCarga(s, insertados);
            } catch(NumberFormatException | NullPointerException e) {
                carga.remove(s);
                socket_servidor.remove(s);

                int eliminados = _tuplas.rollback(nombre, ti);
                actualizarCarga(_myName, -eliminados);
                String tupla = "";
                for (String elemento : ti)
                    tupla += elemento + Data.SUBSPLIT;
                rollback (servidoresExitosos, Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SUBSPLIT + tupla);
                return false; 
            }
        }
        writeLog(msg);
        return true;
    }

    /**
     * Método que inserta una tupla cuando su conjunto es de tipo particionado 
     *
     * @param tuplaServidores Lista de servidores en los que está segmentado
     *                        el conjunto de la tupla.
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    private boolean insertarParticionado(String nombre, List<String> ti, List<String> servidores, String msg) {
        int insertados = 0;
        List<String> servidorExitoso = new ArrayList<String>();

        String servidor = servidorMenosCargado(servidores);
        if (servidor.equals("")) {
            return false;
        }

        try {
            if (!servidor.equals(_myName)) {
                Coordinador g = socket_servidor.get(servidor);
                insertados = Integer.parseInt(g.getAction(msg));
            } else {
                Data.print(TuplaD._myName, "Insertando " + ti.toString() + " en el conjunto " + nombre);
                writeLog(msg);
                insertados = _tuplas.add(nombre, ti);
            }
            servidorExitoso.add(servidor);
            actualizarCarga(servidor, insertados);
        } catch (NumberFormatException | NullPointerException e) {
            carga.remove(servidor);
            socket_servidor.remove(servidor);

            if (servidor.equals(_myName)) {
                int eliminados = _tuplas.rollback(nombre, ti);
                actualizarCarga(_myName, -eliminados);
            } else {
                String tupla = "";
                for (String t : ti) 
                    tupla += t + Data.SUBSPLIT;
                rollback (servidorExitoso, Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SUBSPLIT + tupla);
            }
            return false; 
        }
        writeLog(msg);
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
        if (! _tuplas.exists(nombre)) {
            return Data.ERR_EXISTE + nombre;
        }
        boolean commit = true;
        int tipo = _tuplas.tipo(nombre);
        String tupla = concatenarTupla(ti);
        String msg = Data.SUBJECT_INSERTAR + Data.SPLIT + nombre + Data.SPLIT + tupla;

        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        List<String> servidoresExitosos = new ArrayList<String>();

        int insertados = 0;
        if (tipo == Data.REPLICADO) {
            commit = insertarReplicado(nombre, ti, tuplaServidores, msg);
        } else if (tipo == Data.PARTICIONADO) {
            commit = insertarParticionado(nombre, ti, tuplaServidores, msg);
        } else if (tipo == Data.SEGMENTADO) {
            commit = insertarSegmentado(nombre, ti, tuplaServidores);
        }

        if (! commit ) 
            return Data.ERR_INSERTAR;
        Data.print(_myName, Data.EXITO_INSERTAR + _tuplas.getElements(nombre, ti.get(0))); 
        Data.print(TuplaD._myName, _tuplas.get(nombre).toString());
        return Data.EXITO_INSERTAR; 
    }

    /**
      * Método que hace rollback de una transacción.
      *
      * @param servidores Servidores que deben hacer rollback
      * @param msg Contenido que debe hacerse rollback
      */
    public void rollback(List<String> servidores, String msg) {
        for (String s : servidores) {
            try {
                Coordinador g = socket_servidor.get(s);

                int eliminados = Integer.parseInt(
                        g.getAction(Data.SUBJECT_ROLLBACK + Data.SPLIT + msg));

                actualizarCarga(s, -eliminados);
            } catch (NumberFormatException | NullPointerException e) {
                Data.printErr(_myName, "Falla en el servidor: " + s);
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
    public String borrar (String nombre, String clave) {
        if (! _tuplas.exists(nombre)) {
            return Data.ERR_EXISTE + nombre;
        }
        List<String> tuplaAnterior = new ArrayList<String>();
        List<String> elementos = _tuplas.getElements(nombre, clave);
        for (String a : elementos) {
            tuplaAnterior.add(new String(a));
        }
        System.out.println("La tupla anterior es: " + tuplaAnterior);

        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        String msg = Data.SUBJECT_BORRAR + Data.SPLIT + nombre + Data.SUBSPLIT + clave;
        int borrados = 0;
        boolean commit = true;
        for (String s : tuplaServidores) {
            if (!s.equals(_myName)) {
                try {
                    Coordinador g = socket_servidor.get(s);
                    borrados = Integer.parseInt(g.getAction(msg));
                } catch(NumberFormatException | NullPointerException e) {
                    carga.remove(s);
                    socket_servidor.remove(s);
                    commit = false;
                }
            } else {
                Data.print(TuplaD._myName, "Eliminando tupla "+clave+" en el conjunto "+nombre);
                writeLog(msg + Data.SPLIT + _tuplas.getElements(nombre, clave));
                borrados = _tuplas.remove(nombre, clave);
            }
            actualizarCarga(s, borrados);
        }

        if (! commit) {
            String lastEntry = log.readEntry();
            Data.print(_myName, lastEntry);
            String[] msgAnterior = lastEntry.split(Data.SPLIT);
            if (msgAnterior[0].equals(Data.SUBJECT_BORRAR)) {
                Data.print(TuplaD._myName, "Rollback: Agregando " + tuplaAnterior + " en el conjunto " + nombre);
                _tuplas.add(clave, tuplaAnterior);
            }
            rollback(tuplaServidores, Data.MSG_BORRAR + Data.SPLIT + nombre + Data.SUBSPLIT + clave);
        }
        Data.print(_myName, Data.EXITO_BORRAR); 
        Data.print(TuplaD._myName, _tuplas.get(nombre).toString());
        return Data.EXITO_BORRAR;
    }

    /**
     * Método que busca una tupla dentro de un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a actualizar
     * @return El conjunto de valores de la tupla.
     */
    public List<String> buscar (String nombre, String clave) {
        if (! _tuplas.exists(nombre)) {
            Data.print(_myName, Data.ERR_BUSCAR);
            return new ArrayList<String>();
        }
        int tipo = _tuplas.tipo(nombre);
        String tupla = "";
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        String msg = Data.SUBJECT_BUSCAR + Data.SPLIT + nombre + Data.SUBSPLIT + clave;
        for (String s : tuplaServidores) {
            if (!s.equals(_myName)) {
                Coordinador g = socket_servidor.get(s);
                try {
                    tupla += g.getAction(msg);
                } catch (NullPointerException e) {
                    return new ArrayList<String>();
                }
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
        Data.print(_myName, Data.EXITO_BUSCAR);
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
    public String actualizar (String nombre, String clave, int posicion, String valor) {
        if (! _tuplas.exists(nombre)) {
            return Data.ERR_EXISTE + nombre; 
        }
        String valorAnterior = "";
        List<String> servidoresExitosos = new ArrayList<String>();
        int tipo = _tuplas.tipo(nombre);
        boolean commit = true;

        String msg = Data.SUBJECT_ACTUALIZAR + Data.SPLIT + nombre + Data.SUBSPLIT + clave + 
            Data.SUBSPLIT + posicion + Data.SUBSPLIT + valor;

        int miPosicion = -1;
        List<String> tuplaServidores = _tuplas.servidores(nombre); 
        if (tipo == Data.REPLICADO || tipo == Data.PARTICIONADO) {
            for (String s : tuplaServidores) {
                if (!s.equals(_myName)) {
                    try {
                        Coordinador g = socket_servidor.get(s);
                        int ack = Integer.parseInt(g.getAction(msg));
                        servidoresExitosos.add(s);
                    } catch(NumberFormatException | NullPointerException e) {
                        carga.remove(s);
                        socket_servidor.remove(s);
                        commit = false;
                    }
                } else {
                    miPosicion = posicion;
                    _tuplas.set(nombre, clave, posicion, valor);
                    servidoresExitosos.add(s);
                }
            }
        } else if (tipo == Data.SEGMENTADO) {
            int offset = 0;
            String getCantidad = Data.SUBJECT_CARDINALIDAD + Data.SPLIT + 
                nombre + Data.SUBSPLIT + clave;
            for (String s : tuplaServidores) {
                if (!s.equals(_myName)) {
                    try {
                        Coordinador g = socket_servidor.get(s);
                        int cardinalidad = Integer.parseInt(g.getAction(getCantidad));
                        offset += cardinalidad;
                        if (posicion < offset) {
                            System.out.println("Enviando a " + s + " | p:" + posicion + ", o:" + offset);
                            msg += Data.SUBSPLIT + (offset - cardinalidad);
                            int ack = Integer.parseInt(g.getAction(msg));
                            servidoresExitosos.add(s);
                            break;
                        }
                    } catch(NumberFormatException | NullPointerException e) {
                        Data.printErr(_myName, Data.ERR_SERVIDOR + s);
                        carga.remove(s);
                        socket_servidor.remove(s);
                        commit = false;
                        break;
                    }
                } else {
                    int cardinalidad =  _tuplas.cardinalidad(nombre, clave);
                    offset += cardinalidad;

                    if (posicion < offset) {
                        System.out.println("Enviando a " + s + " | p:" + posicion + ", o:" + offset);
                        miPosicion = posicion - (offset - cardinalidad);
                        valorAnterior = _tuplas.getValue(nombre, clave, miPosicion);
                        writeLog(msg + Data.SUBSPLIT + valorAnterior);
                        _tuplas.set(nombre, clave, miPosicion, valor);
                        servidoresExitosos.add(s);
                        break;
                    }
                }
            }
        }

        if (! commit ) {
            if (miPosicion != -1) 
                _tuplas.set(nombre, clave, miPosicion, valorAnterior);
            rollback(servidoresExitosos, (msg + Data.SUBSPLIT + valorAnterior));
            Data.print(_myName, Data.ERR_ACTUALIZAR);
            return Data.ERR_ACTUALIZAR;
        }
        Data.print(_myName, Data.EXITO_ACTUALIZAR);
        Data.print(TuplaD._myName, _tuplas.get(nombre).toString());
        return Data.EXITO_ACTUALIZAR;
    }

    /**
     * Método para consultar la configuración de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas.
     * @return Información de configuración del conjunto de tuplas.
     */
    public String configuracion (String nombre) {
        if (! _tuplas.exists(nombre)) {
            return Data.ERR_EXISTE + nombre;
        }
        String conf = _tuplas.config(nombre);
        return conf;
    }

    /**
     * Método que imprime el uso correcto del programa
     */
    private static void uso() {
        String uso = "java TuplaD -s [nombre] -p [puerto] -c \n" +
            "java TuplaD -s [nombre] -p [puerto] -n [coordinador]";
        Data.printErr(_myName, uso);
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
        Data.print(_myName, "TuplaD registrado");
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
                    _myName = args[i+1];
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
            log = new Log();
            _servidores = new ArrayList<Servidor>();
            byte[] localIp = InetAddress.getLocalHost().getAddress();
            _myAddress = InetAddress.getByAddress(localIp).getHostAddress();
            _servidores.add(new Servidor(_myName, 0));
            carga.put(_myName, 0);

            InetAddress addr = InetAddress.getByName(_myAddress);
            //_myName = addr.getHostName();

            Data.print(_myName, Data.MSG_STARTUP);


            if (_coordinador) {
                registrarse();
                while (true) {
                    try (ServerSocket serverSocket = new ServerSocket(_puerto)) { 
                        while (true) {
                            Data.print(_myName, "Esperando conexión");
                            Socket service = serverSocket.accept();
                            Data.print(_myName, "Conexión aceptada");
                            Thread c = new Thread(new Coordinador(service));
                            c.start();
                        }
                    } catch (IOException e) {
                        Data.printErr(_myName, "Error escuchando por el puerto " + _puerto);
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
        } catch (ConnectException e) {
            Data.printErr(_myName, "Problema creando la conexión RMI");
        } catch (UnknownHostException e) {
            Data.printErr(_myName, "Host desconocido");
        } catch (IOException e) {
            Data.printErr(_myName, "Excepción de entrada/salida");
        }
    }
}
