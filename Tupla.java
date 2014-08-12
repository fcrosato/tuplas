import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;

public class Tupla implements Serializable{
    private String _nombre;
    private int _dimension;
    private int _tipo;
    private List<String> _elementos = new ArrayList<String>();
    private List<String> _servidores = new ArrayList<String>();

    public Tupla() {}

    public Tupla (int dimension, int tipo, List<String> elementos, List<String> servidores) { 
        _dimension = dimension;
        _tipo = tipo;
        _elementos = elementos;
        _servidores = servidores;
    }

    public List<String> elementos() {
        return _elementos;
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

    public String listarElementos() {
        return listar(_elementos);
    }

    public String listarServidores() {
        return listar(_servidores);
    }

    public void set(int posicion, String valor) {
        if (0 <= posicion && posicion < _elementos.size()) {
            String e = _elementos.set(posicion, valor);
        }
    }
    
    public void set(String valor) {
        _elementos.add(valor);
    } 

    @Override
    public String toString() {
        String tupla = "" +
            "Dimensión:  " + _dimension + "\n" +
            "Tipo:       " + nombreTipo(_tipo) + "\n" +
            "Elementos:  " + listarElementos() + "\n" +
            "Servidores: " + listarServidores();

        return tupla;
    }
}
