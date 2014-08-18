import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
/**
 * Clase que implementa el monitor de los conjuntos de tuplas para un
 * servidor TuplaD.
 * @author Fabiola Rosato
 * @author José Delgado
 */
public class Conjuntos {
    private static HashMap<String, ConjuntoTupla> _conjuntos
        = new HashMap<String, ConjuntoTupla>();

    /**
      * Constructor por defecto de la clase.
      */
    public Conjuntos() {

    }

    /**
      * Método que devuelve el tipo de un conjunto.
      *
      * @param nombre Nombre del conjunto
      * @return el tipo del conjunto
      */
    public synchronized int tipo(String nombre) {
        return _conjuntos.get(nombre).tipo();
    }
    
    /**
      * Método que agrega un conjunto de tuplas. 
      *
      * @param nombre Nombre del conjunto
      * @param dimension Dimension del conjunto
      * @param tipo Tipo del conjunto 
      * @param servidores Servidores del conjunto
      */
    public synchronized void addNew(String nombre, int dimension, int tipo, List<String> servidores) {
       ConjuntoTupla cjto = new ConjuntoTupla(nombre, dimension, tipo, servidores); 
        _conjuntos.put(nombre, cjto);
    }

    /**
      * Método que elimina un conjunto 
      *
      * @param nombre Nombre del conjunto
      */
    public synchronized void clear(String nombre) {
        _conjuntos.remove(nombre);
    }

    /**
      * Método que agrega una tupla a un conjunto de tuplas
      *
      * @param nombre Nombre del conjunto   
      * @param ti Tupla a agregar
      */
    public synchronized void add(String nombre, List<String> ti) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        String clave = ti.remove(0);
        cjto.add(clave, ti);
    }

    /**
      * Método que elimina una tupla de un conjunto 
      *
      * @param nombre Nombre del conjunto
      * @param clave Clave de la tupla a eliminar
      */
    public synchronized void remove(String nombre, String clave) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        cjto.remove(clave);
    }

    /**
      * Método que determina si un conjunto de tuplas existe
      *
      * @param nombre Nombre del conjunto
      * @return true si la tupla existe, false en caso contrario
      */
    public synchronized boolean exists(String nombre) {
        return _conjuntos.containsKey(nombre);
    }

      /**
      * Método que devuelve los elementos de una tupla de un conjunto
      *
      * @param nombre Nombre del conjunto
      * @param clave Clave de la tupla
      * @return una lista con los elementos de la tupla
      */  
    public synchronized List<String> getElements(String nombre, String clave) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.get(clave);
    }

      /**
      * Método que modifica un elemento de una tupla de un conjunto 
      *
      * @param nombre Nombre del conjunto
      * @param clave Clave de la tupla
      * @param posicion Posicion de la tupla a actualizar
      * @param valor Valor nuevo del elemento
      */  
    public synchronized void set(String nombre, String clave, int posicion, String valor) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        if (cjto.exists(clave)) {
            cjto.set(clave, posicion, valor);
        }
    }

      /**
      * Método que devuelve la cardinalidad de una tupla de un conjunto
      *
      * @param nombre Nombre del conjunto
      * @param clave Clave de la tupla
      * @return la cardinalidad de la tupla 
      */  
    public synchronized int cardinalidad(String nombre, String clave) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.cardinalidad(clave);
    }

      /**
      * Método que devuelve la cardinalidad de una tupla de un conjunto
      *
      * @param nombre Nombre del conjunto
      * @return un string con la configuración de un conjunto de tuplas
      */  
    public synchronized String config(String nombre) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.toString();
    }

      /**
      * Método que devuelve la lista de servidores de un conjunto de tuplas 
      *
      * @param nombre Nombre del conjunto
      * @return lista de servidores del conjunto de tuplas 
      */  
    public synchronized List<String> servidores(String nombre) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.servidores();
    }

    @Override
    public synchronized String toString() {
        String cjto = "" ;
        for (String s : _conjuntos.keySet()) {
            cjto += _conjuntos.get(s).toString() + "\n";
        }
        return cjto;    
    }

}
