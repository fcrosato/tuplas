import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class Conjuntos {
    private static HashMap<String, ConjuntoTupla> _conjuntos
        = new HashMap<String, ConjuntoTupla>();

    public Conjuntos() {

    }

    public synchronized int tipo(String nombre) {
        return _conjuntos.get(nombre).tipo();
    }
    
    public synchronized void addNew(String nombre, int dimension, int tipo, List<String> servidores) {
       ConjuntoTupla cjto = new ConjuntoTupla(nombre, dimension, tipo, servidores); 
        _conjuntos.put(nombre, cjto);
    }

    public synchronized void clear(String nombre) {
        _conjuntos.remove(nombre);
    }

    public synchronized void add(String nombre, List<String> ti) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        String clave = ti.remove(0);
        cjto.add(clave, ti);
    }

    public synchronized void remove(String nombre, String clave) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        cjto.remove(clave);
    }

    public synchronized boolean exists(String nombre) {
        return _conjuntos.containsKey(nombre);
    }
    
    public synchronized List<String> getElements(String nombre, String clave) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.get(clave);
    }

    public synchronized void set(String nombre, String clave, int posicion, String valor) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        if (cjto.exists(clave)) {
            cjto.set(clave, posicion, valor);
        }
    }

    public synchronized int cardinalidad(String nombre, String clave) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.cardinalidad(clave);
    }

    public synchronized String config(String nombre) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.toString();
    }

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
