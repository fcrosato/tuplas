import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class Conjuntos {
    private static HashMap<String, ConjuntoTupla> _conjuntos
        = new HashMap<String, ConjuntoTupla>();

    public Conjuntos() {

    }
    
    public synchronized void addNew(String nombre, int dimension, int tipo, List<String> servidores) {
       ConjuntoTupla cjto = new ConjuntoTupla(nombre, dimension, tipo, servidores); 
        _conjuntos.put(nombre, cjto);
    }

    public synchronized void clear() {
        _conjuntos.clear();
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
        cjto.set(clave, posicion, valor);
    }

    public synchronized String config(String nombre) {
        ConjuntoTupla cjto = _conjuntos.get(nombre);
        return cjto.toString();
    }

}
