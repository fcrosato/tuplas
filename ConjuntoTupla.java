import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;

public class ConjuntoTupla implements Serializable {
    private String _nombre;
    private int _dimension;
    private int _tipo;
    private HashMap<String, List<String>> _tuplas = new HashMap<String, List<String>>();
    private List<String> _servidores = new ArrayList<String>();

    public ConjuntoTupla() {}

    public ConjuntoTupla (String nombre, int dimension, int tipo, List<String> servidores) { 
        _nombre = nombre;
        _dimension = dimension;
        _tipo = tipo;
        _servidores = servidores;
    }

    public List<String> get(String clave) {
        return _tuplas.get(clave);
    }

    public int tipo() {
        return _tipo;
    }

    public int cardinalidad(String clave) {
        List<String> elementos = _tuplas.get(clave);
        return elementos.size();
    }
    
    public String nombreTipo(int tipo) {
        if (tipo == 1) {
            return "Segmentado";
        } else if (tipo == 2) {
            return "Replicado";
        } else if (tipo == 3) {         
            return "Particionado";
        }
        return "Tipo inválido";
    }

    private String listar(List<String> lista) {
        String resultado = "|";
        for (String l : lista) {
            resultado += l + " | ";
        }
        return resultado;
    }

    public boolean exists(String key) {
        return _tuplas.containsKey(key);
    }

    public void add(String clave, List<String> elementos) {
        List<String> old = _tuplas.get(clave);
        if (old != null) {
            elementos.addAll(old);
        }
        _tuplas.put(clave, elementos);
    }   

    public void remove(String clave) {
        _tuplas.remove(clave);
    }

    public String listarElementos() {
        String result = "";
        for (String key : _tuplas.keySet()) {
            result += (key + ">" + listar(_tuplas.get(key)) + "\n");
        }
        return result;
    }

    public String listarServidores() {
        return listar(_servidores);
    }
    public void set(String clave, int posicion, String valor) {
        List<String> elementos = _tuplas.get(clave);
        if (0 <= posicion && posicion < elementos.size()) {
            String e = elementos.set(posicion, valor);
        }
    }

    public List<String> servidores() {
        return _servidores;
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
