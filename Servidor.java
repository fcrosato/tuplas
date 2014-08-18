/**
 * Clase que implementa la representación de un servidor 
 * @author Fabiola Rosato
 * @author José Delgado
 */
public class Servidor {
    public String ip;
    public int carga;

    /**
      * Constructor de la clase especificando la ip y la carga del
      * servidor.
      */
    Servidor (String _ip, int _carga) {
        ip = _ip;
        carga = _carga;
    }

    @Override
    public String toString() {
        return ("ip: " + ip + " | carga: " + carga);
    }
}
