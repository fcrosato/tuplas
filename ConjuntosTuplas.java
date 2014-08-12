import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class ConjuntosTuplas {
    private static HashMap<String, List<Tupla>> _conjuntos
        = new HashMap<String, List<Tupla>>();

    public ConjuntosTuplas() {

    }
    
    public synchronized void addNew(String nombre) {
        List<Tupla> ti = new ArrayList<Tupla>();
        _conjuntos.put(nombre, ti);
    }

    public synchronized void clear() {
        _conjuntos.clear();
    }

    public synchronized void add(String nombre, Tupla ti) {
        List<Tupla> tuplas = _conjuntos.get(nombre);
        tuplas.add(ti);
    }

    public synchronized void remove(String nombre, String clave) {
        List<Tupla> tuplas = _conjuntos.get(nombre);
        for (Tupla t : tuplas) {
            if (!t.elementos().isEmpty() && t.elementos().get(0).equals(clave)) {
                tuplas.remove(t); 
                break;
            }
        }
    }

    public synchronized boolean exists(String nombre) {
        return _conjuntos.containsKey(nombre);
    }
    
    public synchronized List<String> getElements(String nombre, String clave) {
        List<Tupla> tuplas = _conjuntos.get(nombre);
        for (Tupla t : tuplas) {
            if (!t.elementos().isEmpty() && t.elementos().get(0).equals(clave)) {
                return t.elementos(); 
            }
        }
        return (new ArrayList<String>());
    }

    public synchronized void set(String nombre, String clave, int posicion, String valor) {
        List<Tupla> tuplas = _conjuntos.get(nombre);
        for (Tupla t : tuplas) {
            if (!t.elementos().isEmpty() && t.elementos().get(0).equals(clave)) {
                t.elementos().set(posicion, valor); 
            }
        }
    }

    public synchronized String config(String nombre) {
        String config = "";
        List<Tupla> tuplas = _conjuntos.get(nombre);
        if (tuplas == null) {
            return "";
        }
        for (Tupla t : tuplas) { 
            config += t.toString() + "\n";
        }
        return config;
    }

}
