import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;
/**
 * Clase que implementa un conjunto de tuplas. 
 * @author Fabiola Rosato
 * @author José Delgado
 */
public class ConjuntoTupla implements Serializable {
    private String _nombre;
    private int _dimension;
    private int _tipo;
    private HashMap<String, List<String>> _tuplas = new HashMap<String, List<String>>();
    private List<String> _servidores = new ArrayList<String>();

    /**
      * Constructor por defecto de la clase.
      */
    public ConjuntoTupla() {}

    /**
      * Constructor de la clase, especificando el nombre, la dimension,
      * el tipo del conjunto de tuplas y los servidores en los que estará 
      * alojado.
      */
    public ConjuntoTupla (String nombre, int dimension, int tipo, List<String> servidores) { 
        _nombre = nombre;
        _dimension = dimension;
        _tipo = tipo;
        _servidores = servidores;
    }

    public String getValue(String clave, int posicion) {
        if (! exists(clave) )
            return "";

        List<String> elementos = _tuplas.get(clave);
        return elementos.get(posicion);
    }

    public void delServer(String s) {
        _servidores.remove(s);
    }

    public String nombre() {
        return _nombre;
    }

    public int dimension() {
        return _dimension;
    }

    /** 
      * Método que devuelve el conjunto de elementos de una tupla del
      * conjunto.
      *
      * @param clave Clave de la tupla a buscar
      * @return Lista de elementos de una tupla del conjunto.
      */
    public List<String> get(String clave) {
        return _tuplas.get(clave);
    }

    /**
      * Método que devuelve el tipo del conjunto.
      *
      * @return el tipo del conjunto de tuplas.
      */
    public int tipo() {
        return _tipo;
    }
    /**
      * Método que devuelve la cardinalidad de una tupla del conjunto.
      *
      * @param clave Clave de la tupla
      * @return cardinalidad de la tupla.
      */
    public int cardinalidad(String clave) {
        List<String> elementos = _tuplas.get(clave);
        return elementos.size();
    }
   
    /**
      * Método que devuelve el nombre del tipo de particionamiento de conjuntos.
      *
      * @return Un string con el nombre del tipo.
      */
    public String nombreTipo(int tipo) {
        if (tipo == Data.SEGMENTADO) {
            return "Segmentado";
        } else if (tipo == Data.REPLICADO) {
            return "Replicado";
        } else if (tipo == Data.PARTICIONADO) {  
            return "Particionado";
        }
        return "Tipo inválido";
    }

    /**
      * Método que lista los elementos de una lista.
      *
      * @param lista Lista sobre la que se listarán sus elementos
      * @return Un string con los elementos de la lista
      */
    private String listar(List<String> lista) {
        String resultado = "|";
        for (String l : lista) {
            resultado += l + " | ";
        }
        return resultado;
    }

    /**
      * Método que retorna si una clave existe dentro del conjunto de tuplas
      * 
      * @param key clave de la tupla
      * @return true si la tupla existe, false en caso contrario
      */
    public boolean exists(String key) {
        return _tuplas.containsKey(key);
    }



    /**
      * Método que agrega una tupla al conjunto.
      * 
      * @param clave Clave de la tupla a agregar
      * @param elementos Elementos de la tupla a agregar
      */
    public int add(String clave, List<String> elementos) {
        _dimension += elementos.size();
        List<String> old = _tuplas.get(clave);
        if (old != null) {
            elementos.addAll(old);
        }
        _tuplas.put(clave, elementos);
        return elementos.size();
    }   

    public int del(List<String> elementos) {
        if (elementos.isEmpty()) 
            return 0;

        String clave = elementos.get(0);
        if (!exists(clave)) {
            return 0;
        }

        List<String> elementos_tupla = _tuplas.get(clave);
        for (int i = 1; i < elementos.size(); i++) {
            elementos_tupla.remove(elementos.get(i));
        }   
        return (elementos.size() - 1);
    }

    /**
      * Método que elimina una tupla de un conjunto.
      *
      * @param clave Clave de la tupla a eliminar 
      */
    public int remove(String clave) {
        int removed =  _tuplas.get(clave).size();
        _dimension -= removed;
        _tuplas.remove(clave);
        return removed;
    }

    /**
      * Método que lista los elementos del conjunto
      *
      * @return Un string con los elementos del conjunto.
      */
    public String listarElementos() {
        String result = "";
        for (String key : _tuplas.keySet()) {
            result += (key + ">" + listar(_tuplas.get(key)) + "\n");
        }
        return result;
    }

    /**
      * Método que lista los servidores de un conjunto
      *
      * @return un string con los servidores del conjunto.
      */
    public String listarServidores() {
        return listar(_servidores);
    }

    /**
      * Método que modifica una tupla del conjunto
      *
      * @param clave Clave de la tupla a modificar
      * @param posicion Posición de la tupla a modificar
      * @param valor El nuevo valor del elemento
      */
    public String set(String clave, int posicion, String valor) {
        List<String> elementos = _tuplas.get(clave);
        System.out.println("\nELEMENTOS " + elementos + " // posicion " + posicion);
        if (0 <= posicion && posicion < elementos.size()) {
            String e = elementos.set(posicion, valor);
            System.out.println("\tPrevious: " + e + " | now: " + valor);
            return e;
        }
        return "";
    }

    /**
      * Método que devuelve los servidores del conjunto
      *
      * @return una lista de los servidores del conjunto
      */
    public List<String> servidores() {
        return _servidores;
    }
    
    public String log() {
        String tupla = "" + 
            _dimension + Data.SUBSPLIT + _tipo + Data.SUBSPLIT + listarElementos() +
            Data.SUBSPLIT + listarServidores();
        return tupla;
    }

    @Override
    public String toString() {
        String tupla = "" +
            "Dimensión:  " + _dimension + "\n" +
            "Tipo:       " + nombreTipo(_tipo) + "\n" +
          //  "Elementos:  " + listarElementos() + "\n" +
            "Servidores: " + listarServidores();

        return tupla;
    }
}
