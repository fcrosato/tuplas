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
import java.io.RandomAccessFile;
/**
 * Clase que implementa el hilo del servidor nodo para atender peticiones
 * de distribución de carga.
 * @author Fabiola Rosato
 * @author José Delgado
 */
public class Nodo implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String lastMsg = Data.SUBJECT_INICIO + Data.SPLIT;

    /**
     * Constructor de la clase, especificando el socket de la conexión 
     * con el servidor coordinador.
     */
    public Nodo(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
    }


    /**
     * Método que crea una tupla nueva.
     * 
     * @param nombre Identificador del conjunto de tuplas
     * @param dimension Número de elementos de una tupla (debe ser mayor que 2)
     * @param tipo  Indica si es segmentado, replicado o particionado.
     * @param servidores Nombre de las máquinas donde se desea que resida el conjunto de tuplas.  
     * @return true si se crea satisfactoriamente, false en caso contrario.
     */
    public ConjuntoTupla crear(String nombre, int dimension, int tipo, List<String> servidores) {
        Data.print("Creando conjunto " + nombre); 
        ConjuntoTupla cjto = TuplaD._tuplas.addNew(nombre, dimension, tipo, servidores);
        Data.print(TuplaD._tuplas);
        return cjto;    
    }


    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public int eliminar (String nombre) {
        if (TuplaD._tuplas.exists(nombre)) {
            Data.print("Eliminando conjunto " + nombre); 
            return TuplaD._tuplas.clear(nombre);
        }
        return 0;
    }


    /**
     * Método que inserta una tupla.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public int insertar (String nombre, List<String> ti) {

        if (TuplaD._tuplas.exists(nombre)) {
            Data.print("Insertando tupla en el conjunto " + nombre);
            Data.print(ti);
            return TuplaD._tuplas.add(nombre, ti); 
        }
        return 0;
    }


    /**
     * Método que borra una tupla de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a borrar.
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public int borrar (String nombre, String clave) {
        if (TuplaD._tuplas.exists(nombre)) {
            Data.print("Eliminando tupla "+clave+" en el conjunto "+nombre);
            int borrados = TuplaD._tuplas.remove(nombre, clave);
            return borrados;
        }
        return 0;
    }

    /**
     * Método que busca una tupla dentro de un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a buscar 
     * @return El conjunto de valores de la tupla.
     */
    public List<String> buscar (String nombre, String clave) {
        List<String> result = new ArrayList<String>();
        if (TuplaD._tuplas.exists(nombre)) {
            Data.print("Buscando elementos de la tupla "+clave+" en el conjunto "+nombre);
            result = TuplaD._tuplas.getElements(nombre, clave);
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
    public String actualizar (String nombre, String clave, int posicion, String valor) {
        if (TuplaD._tuplas.exists(nombre)) {
            Data.print("Actualizando: "+
                    "\tconjunto "+nombre +
                    "\tclave: "+clave +
                    "\tposicion: "+posicion +
                    "\tvalor: "+valor);
            String anterior = TuplaD._tuplas.set(nombre, clave, posicion, valor);
            out.println(anterior);
            return anterior;
        }
        out.println("");
        return "";
    }

    /**
     * Método para consultar la configuración de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas.
     * @return UInformación de configuración del conjunto de tuplas.
     */
    public String configuracion (String nombre) {
        String conf = TuplaD._tuplas.config(nombre);
        //= Información de configuración del conjunto de tuplas
        return conf;
    }

    /**
     * Método que obtiene la cardinalidad de una tupla
     * 
     * @param nombre Nombre del conjunto donde reside la tupla
     * @param clave Clave de la tupla
     * @return la cardinalidad de la tupla
     */
    public int cardinalidad (String nombre, String clave) {
        if (!TuplaD._tuplas.exists(nombre)) {
            return 0;
        }
        return TuplaD._tuplas.cardinalidad(nombre, clave);
    }


    /**
     * Método para realizar el protocolo de ingreso al sistema distribuido.
     */
    public void join() throws IOException {
        out.println(Data.SUBJECT_JOINING + Data.SPLIT + TuplaD._myAddress);
        String fromServer = in.readLine();
        Data.print("Joining: " + fromServer);

        String[] all_servers = fromServer.split(Data.SPLIT);

        for (int i = 0; i < all_servers.length; i+=2) {
            String ip = all_servers[i];
            int carga = Integer.parseInt(all_servers[i+1]);
            TuplaD._servidores.add(new Servidor(ip, carga));
        } 

    }

    /**
     * Método que dado un mensaje, realiza una acción
     *
     * @param msg Mensaje a ser procesado.
     * @return retorna la respuesta del servidor al que se está conectado,
     *         si aplica.
     */
    public int getAction(String msg, BufferedReader in, PrintWriter out) {
        lastMsg = msg;
        String[] msg_split = msg.split(Data.SPLIT);
        String subject = msg_split[0];
        Data.print("Subject: " + subject);
        String action = msg_split[1];

        if (subject.equals(Data.SUBJECT_LEAVING)) {
            TuplaD._servidores.remove(action);

        } else if (subject.equals(Data.SUBJECT_CARDINALIDAD)) {
            String[] cardinalidad = action.split(Data.SUBSPLIT);
            String nombre = cardinalidad[0];
            String clave = cardinalidad[1];
            int card = cardinalidad(nombre, clave);
            Data.print("Enviando cardinalidad de " + nombre + ": " + card);
            out.println(card);
        } else if (subject.equals(Data.SUBJECT_JOINING)) {
            // TuplaD.socket_servidor.put(action, this);
            StringBuilder all_servers = new StringBuilder();
            for (Servidor s : TuplaD._servidores) {
                all_servers.append(s.ip).append(Data.SPLIT).append(s.carga).append(Data.SPLIT);
            }
            TuplaD._servidores.add(new Servidor(action, 0));
            out.println(all_servers.toString());

        } else if (subject.equals(Data.SUBJECT_CREAR)) {
            String[] crear = action.split(Data.SUBSPLIT);
            String nombre = crear[0];
            int tipo      = Integer.parseInt(crear[1]);

            ConjuntoTupla cjto = crear(nombre, 0, tipo, new ArrayList<String>());
            TuplaD.writeLog(Data.SUBJECT_CREAR + Data.SPLIT + cjto.log());
            out.println(Data.ACK);

        } else if (subject.equals(Data.SUBJECT_ELIMINAR)) {
            int eliminados = 0;
            if (!TuplaD._tuplas.exists(action)) { 
                out.println(eliminados);
                return 0;
            }
            ConjuntoTupla cjto = TuplaD._tuplas.get(action);
            eliminados = eliminar(action);
            out.println(eliminados);
            TuplaD.writeLog(Data.SUBJECT_ELIMINAR + Data.SPLIT + cjto.log());

        } else if (subject.equals(Data.SUBJECT_INSERTAR)) {
            String nombre = action;
            String[] elementos = msg_split[2].split(Data.SUBSPLIT);
            List<String> tupla = new ArrayList<String>();
            for (int i=0; i < elementos.length; i++) {
                tupla.add(elementos[i]);
            }
            int insertados = insertar(nombre, tupla);
            out.println(insertados);

            TuplaD.writeLog(Data.SUBJECT_INSERTAR + Data.SPLIT + msg_split[2]);
        } else if (subject.equals(Data.SUBJECT_BORRAR)) {
            String borrar[] = action.split(Data.SUBSPLIT);
            String nombre = borrar[0];
            String clave = borrar[1];

            int borrados = 0;
            if (!TuplaD._tuplas.existsTuple(nombre, clave)) {
                out.println(borrados);
                return 0;
            }

            List<String> elementosAnteriores = TuplaD._tuplas.getElements(nombre, clave);
            String elementosBorrados = "";
            for (int i = 0; i < elementosAnteriores.size(); i++) 
                elementosBorrados += elementosAnteriores.get(i) + Data.SUBSPLIT;

            borrados = borrar(nombre, clave);
            out.println(borrados);
            TuplaD.writeLog(Data.SUBJECT_BORRAR + Data.SPLIT + elementosBorrados);
        } else if (subject.equals(Data.SUBJECT_BUSCAR)) {
            String buscar[] = action.split(Data.SUBSPLIT);
            String nombre = buscar[0];
            String clave = buscar[1];
            List<String> respuesta = buscar(nombre, clave);
            if (respuesta == null) {
                out.println("");
                return 0;
            }
            Data.print("Enviando respuesta: " + respuesta);
            String tupla = "";
            for (int i=0; i<respuesta.size(); i++) {
                tupla += respuesta.get(i) + Data.SUBSPLIT;
            }
            out.println(tupla);
        } else if (subject.equals(Data.SUBJECT_ACTUALIZAR)) {

            String[] actualizar = action.split(Data.SUBSPLIT);
            String nombre = actualizar[0];
            String clave = actualizar[1];
            int posicion = Integer.parseInt(actualizar[2]);
            String valor = actualizar[3];
            int offset = 0;
            if (actualizar.length > 4) {
                offset = Integer.parseInt(actualizar[4]);
            }
            Data.print("Empezando a actualizar...");
            System.out.println("HELLOOOOOO");
            String valorAnterior = actualizar(nombre, clave, posicion - offset, valor);
            Data.print("Tupla actualizada");
            TuplaD.writeLog(msg + Data.SPLIT + valorAnterior);
            Data.print("Despues del log");
            return 0;
        } else if (subject.equals(Data.SUBJECT_INICIO)) {
            // No hacer nada.
        } else if (subject.equals(Data.SUBJECT_ROLLBACK)) {
            return rollback(msg);
        }

        return 0;    
    }

    public int rollback(String msg) {
        lastMsg = msg;
        String[] msg_split = msg.split(Data.SPLIT);
        String subject = msg_split[1];
        Data.print("Subject: " + subject);
        String action = msg_split[2];

        if (subject.equals(Data.SUBJECT_INSERTAR)) {
            String nombre = action;
            String[] elementos = msg_split[2].split(Data.SUBSPLIT);
            List<String> tupla = new ArrayList<String>();
            for (int i=0; i < elementos.length; i++) {
                tupla.add(elementos[i]);
            }
            int eliminados = TuplaD._tuplas.rollback(nombre, tupla);
            out.println(eliminados);

        } else if (subject.equals(Data.SUBJECT_BORRAR)) {
            // buscar en el log el valor de la tupla;
            // devolver el tamaño de la tupla;
            String rollbackBorrar[] = action.split(Data.SUBSPLIT);
            String nombre = rollbackBorrar[0];
            String clave = rollbackBorrar[1];
            int borrados = 0; // buscar en el log aquí.
            out.println(borrados);

        } else if (subject.equals(Data.SUBJECT_ACTUALIZAR)) {
            String[] actualizar = action.split(Data.SUBSPLIT);
            String nombre = actualizar[0];
            String clave = actualizar[1];
            int posicion = Integer.parseInt(actualizar[2]);
            String valor = actualizar[3];
            int offset = 0;
            if (actualizar.length > 4) {
                offset = Integer.parseInt(actualizar[4]);
            }
            // buscar posicion - valor anterior en el log
            actualizar(nombre, clave, posicion - offset, valor);
            return 0;
        } else if (subject.equals(Data.SUBJECT_INICIO)) {
            // 
        } else if (subject.equals(Data.SUBJECT_ROLLBACK)) {
            //
        }
        out.println("0");
        return 0;
    }


    @Override
    public void run() {
        try {
            String fromServer;
            String fromUser;

            join();

            while ((fromServer = in.readLine()) != null) {
                Data.print("Server: " + fromServer);
                getAction(fromServer, in, out);
            }
        } catch (Exception e) {
            Data.printErr("TuplaD exception:");
            e.printStackTrace();
        }
    }
}
