import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TuplaDInterfaz extends Remote
{
    /**
     * Método que crea una tupla nueva.
     * 
     * @param nombre Identificador del conjunto de tuplas
     * @param dimension Número de elementos de una tupla (debe ser mayor que 2)
     * @param tipo  Indica si es segmentado, replicado o particionado.
     * @param servidores Nombre de las máquinas donde se desea que resida el 
     *                   conjunto de tuplas.  
     * @return true si se crea satisfactoriamente, false en caso contrario.
     */
    public String crear(String nombre, int tipo)
             throws RemoteException; 

    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public String eliminar (String nombre) throws RemoteException;


    /**
     * Método que inserta una tupla.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public String insertar (String nombre, List<String> ti) 
        throws RemoteException;


    /**
     * Método que borra una tupla de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a borrar.
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public boolean borrar (String nombre, String clave) 
        throws RemoteException; 

    /**
      * Método que busca una tupla dentro de un conjunto de tuplas.
      *
      * @param nombre Identificador del conjunto de tuplas
      * @param clave Clave de la tupla a actualizar
      * @return El conjunto de valores de la tupla.
      */
    public List<String> buscar (String nombre, String clave)
        throws RemoteException;

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
    public boolean actualizar (String nombre, String clave, int posicion, 
            String valor) throws RemoteException;

    /**
      * Método para consultar la configuración de un conjunto de tuplas
      *
      * @param nombre Identificador del conjunto de tuplas.
      * @return UInformación de configuración del conjunto de tuplas.
      */
    public String configuracion (String nombre) throws RemoteException;
}
