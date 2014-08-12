import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class TuplaD implements TuplaDInterfaz {

    private Servidor _servidor;
    private ConjuntosTuplas _tuplas = new ConjuntosTuplas(); 

    public TuplaD() throws RemoteException {}

    public TuplaD(Servidor servidor) throws RemoteException {
        _servidor = servidor;
    }

    private static void print(Object msg) {
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
    public boolean crear(String nombre) {
        print("Creando conjunto " + nombre); 
        _tuplas.addNew(nombre);
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
    public boolean insertar (String nombre, Tupla ti) {
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

    public static void main(String args[]) {
        /*
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        */
        try {
            
            String nombre = args[0];
            TuplaDInterfaz tuplad = new TuplaD();
            TuplaDInterfaz stub =
                (TuplaDInterfaz) UnicastRemoteObject.exportObject(tuplad, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(nombre, stub);
            System.out.println("TuplaD registrado");

            Runnable g = new Grupo();
            g.run();
        } catch (ArrayIndexOutOfBoundsException e) {
            uso();
        } catch (Exception e) {
            System.err.println("TuplaD exception:");
            e.printStackTrace();
        }
    }
}
