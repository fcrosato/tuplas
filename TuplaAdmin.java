import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.ConnectException;
import java.rmi.NotBoundException;

/**
 * Clase que implementa el cliente RMI de TuplasD
 * @author Fabiola Rosato
 * @author José Delgado
 */
public class TuplaAdmin {
    List<String> tuplas;

    private static final int CREAR      = 1;
    private static final int ELIMINAR   = 2;
    private static final int INSERTAR   = 3;
    private static final int BORRAR     = 4;
    private static final int BUSCAR     = 5;
    private static final int ACTUALIZAR = 6;
    private static final int CONFIG     = 7;
    private static final int SALIR      = 0;


    /**
      * Método de impresión del cliente, finalizando con salto de línea.
      *
      * @param msg Mensaje a imprimir
      */
    private static void println(String msg) {
        System.out.println("cliente> " + msg);
    }

    /**
      * Método de impresión del cliente
      *
      * @param msg Mensaje a imprimir
      */
    private static void print(String msg) {
        System.out.print("cliente> " + msg);
    }

    /**
      * Método de impresión de errores del cliente
      *
      * @param msg Mensaje a imprimir
      */
    private static void printErr(String msg) {
        System.err.println("cliente> Error: " + msg);
    }

    /**
      * Método que devuelve el valor de la entrada de un usuario
      * por entrada estándar.
      *
      * @param in Scanner para obtener la entrada del usuario
      * @param msg Mensaje de preámbulo para la entrada
      * @return el valor de la entrada del usuario
      */
    private static String getString(Scanner in, String msg) {
        println(msg);
        print(Data.MSG_INPUT);
        String nombre = in.next();
        return nombre;
    }

    /**
      * Método para obtener el nombre de un conjunto por entrada estándar
      * 
      * @param in Scanner para obtener la entrada del usuario
      * @return el nombre del conjunto
      */
    private static String getNombre(Scanner in) {
        return getString(in, Data.MSG_NOMBRE);
    }

    /**
      * Método para obtener la clave de una tupla por entrada estándar 
      * 
      * @param in Scanner para obtener la entrada del usuario
      * @return la clave de una tupla
      */
    private static String getClave(Scanner in) {
        return getString(in, Data.MSG_CLAVE);
    }

    /**
      * Método para obtener el valor a actualizar de un elemento de una tupla 
      * 
      * @param in Scanner para obtener la entrada del usuario
      * @return el valor de un elemento de una tupla 
      */
    private static String getValor(Scanner in) {
        return getString(in, Data.MSG_VALOR);
    }
    
    /**
      * Método para obtener la dimensión de una tupla 
      * 
      * @param in Scanner para obtener la entrada del usuario
      * @return la dimensión de una tupla 
      */
    private static int getDimension(Scanner in) {
        println(Data.MSG_DIMENSION);
        print(Data.MSG_INPUT);
        int intentos = 0;
        int dimension = in.nextInt();
        while (dimension < 2 && intentos++ < 3) {
            printErr(Data.ERR_DIMENSION);
            print(Data.MSG_INPUT);
            dimension = in.nextInt();
        }
        if (intentos == 3) {
            printErr(Data.ERR_INTENTOS);
            return -1;
        }
        return dimension;
    }
    /**
      * Método para obtener el tipo de una tupla 
      * 
      * @param in Scanner para obtener la entrada del usuario
      * @return el tipo de una tupla 
      */
    private static int getTipo(Scanner in) {
        println(Data.MSG_TIPO);
        print(Data.MSG_INPUT);
        int intentos = 0;
        int tipo = in.nextInt();
        while ((1 > tipo || tipo > 3) && intentos++ < 3) {
            printErr(Data.ERR_TIPO);
            print(Data.MSG_INPUT);
            tipo = in.nextInt();
        }
        if (intentos == 3) {
            printErr(Data.ERR_INTENTOS);
            return -1;
        }
        return tipo;
    }

    /**
      * Método para obtener la posición de un elemento de una tupla por entrada
      * estándar.
      *
      * @param in Scanner para obtener la entrada del usuario
      * @return posición del elemento de una tupla 
      */
    private static int getPosicion(Scanner in) {
        println(Data.MSG_POSICION);
        print(Data.MSG_INPUT);
        int posicion = in.nextInt();
        return posicion;
    }

    /**
      * Método para obtener una lista de elementos por entrada 
      * estándar.
      *
      * @param in Scanner para obtener la entrada del usuario
      * @param msg Mensaje preámbulo para obtener la lista
      * @return la lista ingresada por el usuario 
      */
    private static List<String> getLista(Scanner in, String msg) {
        println(msg);
        List<String> servidores = new ArrayList<String>();
        int contador = 1;
        print(String.valueOf(contador++) + ") ");
        String dns = in.next();
        while (!dns.equals("done")) {
            servidores.add(dns); 
            print(String.valueOf(contador++) + ") ");
            dns = in.next();
        }
        return servidores;
    }

    /**
      * Método para obtener una lista de servidores por entrada 
      * estándar.
      *
      * @param in Scanner para obtener la entrada del usuario
      * @return la lista de servidores ingresada por el usuario 
      */
    private static List<String> getServidores(Scanner in) {
        return getLista(in, Data.MSG_SERVIDOR);
    }

    /**
      * Método para obtener una lista de elementos por entrada 
      * estándar.
      *
      * @param in Scanner para obtener la entrada del usuario
      * @return la lista de elementos ingresada por el usuario 
      */
    private static List<String> getElementos(Scanner in) {
        return getLista(in, Data.MSG_ELEMENTO);
    }

    /**
     * Método que crea una tupla nueva.
     * 
     * @param tuplad Instancia de la interfaz RMI para obtener el servicio
     * @param in Scanner para obtener la entrada del usuario
     * @return true si se crea satisfactoriamente, false en caso contrario.
     */
    public static String crear(TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(Data.MSG_CREAR);
        String nombre = getNombre(in);
        int tipo = getTipo(in);
        if (tipo == -1) {
            return "";
        }
        return tuplad.crear(nombre, tipo);
    }

    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param tuplad Instancia de la interfaz RMI para obtener el servicio
     * @param in Scanner para obtener la entrada del usuario
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public static String eliminar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(Data.MSG_ELIMINAR);
        String nombre = getNombre(in);
        String msg = tuplad.eliminar(nombre);
        println(msg);
        return msg;
    } 


    /**
     * Método que inserta una tupla.
     *
     * @param tuplad Instancia de la interfaz RMI para obtener el servicio
     * @param in Scanner para obtener la entrada del usuario
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public static String insertar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(Data.MSG_INSERTAR);
        String nombre = getNombre(in);
        List<String> tupla = getLista(in, Data.MSG_ELEMENTO); 
        String msg = tuplad.insertar(nombre, tupla); 
        println(msg);
        return msg;
    }


    /**
     * Método que borra una tupla de un conjunto de tuplas
     *
     * @param tuplad Instancia de la interfaz RMI para obtener el servicio
     * @param in Scanner para obtener la entrada del usuario
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public static boolean borrar(TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(Data.MSG_BORRAR);
        String nombre = getNombre(in);
        String clave = getClave(in);
        println(tuplad.borrar(nombre, clave));
        return true;
    }

    /**
      * Método que busca una tupla dentro de un conjunto de tuplas.
      *
      * @param tuplad Instancia de la interfaz RMI para obtener el servicio
      * @param in Scanner para obtener la entrada del usuario
      * @return El conjunto de valores de la tupla.
      */
    public static List<String> buscar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(Data.MSG_BUSCAR);
        String nombre = getNombre(in);
        String clave = getClave(in);
        List<String> elem = tuplad.buscar(nombre, clave);
        if (elem != null) {
            println(elem.toString());
        } else {
            println("No se encontraron sus datos. Pudo haber ocurrido algún problema con algún servidor");
        }

        return elem;
    }

    /**
      * Método que actualiza un valor de una tupla.
      *
      * @param tuplad Instancia de la interfaz RMI para obtener el servicio
      * @param in Scanner para obtener la entrada del usuario
      * @return true si el valor se actualizó satisfactoriamente, 
                false en caso contrario.
      */
    public static boolean actualizar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(Data.MSG_ACTUALIZAR);
        String nombre = getNombre(in);
        String clave = getClave(in);
        int posicion = getPosicion(in);
        String valor = getValor(in);
        System.out.println("Actualizando");
        return tuplad.actualizar(nombre, clave, posicion, valor);
    }

    /**
      * Método para consultar la configuración de un conjunto de tuplas
      *
      * @param tuplad Instancia de la interfaz RMI para obtener el servicio
      * @param in Scanner para obtener la entrada del usuario
      * @return UInformación de configuración del conjunto de tuplas.
      */
    public static String configuracion (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(Data.MSG_CONFIGURACION);
        String nombre = getNombre(in);
        String result = tuplad.configuracion(nombre);
        println(result);
        return result;
    }


    /**
      * Método que despliega el menú del cliente
      */
    private static void menu(TuplaDInterfaz tuplad) throws RemoteException {
        try (Scanner in = new Scanner(System.in)){
            boolean exit = false;
            int option = -1;
            while (!exit) {
                println(Data.MSG_MENU);
                print(Data.MSG_INPUT);
                option = in.nextInt();
                if (option == SALIR) break;
                else if (option == CREAR) crear(tuplad, in);
                else if (option == ELIMINAR) eliminar(tuplad, in);
                else if (option == INSERTAR) insertar(tuplad, in);
                else if (option == BORRAR) borrar(tuplad, in);
                else if (option == BUSCAR) buscar(tuplad, in);
                else if (option == ACTUALIZAR) actualizar(tuplad, in);
                else if (option == CONFIG) configuracion(tuplad, in);
                else printErr("Opción inválida"); 
            }

        } catch(java.util.InputMismatchException e) {
            printErr("Input no válido");
        }
    }


    /**
      * Método main del programa
      */
    public static void main(String args[]) {
        try {
            println(Data.MSG_INICIO);
            String name = "TuplaD";
            Registry registry = LocateRegistry.getRegistry(args[0]);
            TuplaDInterfaz tuplad = (TuplaDInterfaz) registry.lookup(name);

            menu(tuplad);

        } catch (RemoteException e) {
            printErr("Remote exception");
        } catch (NotBoundException e) {
            printErr("Not bound");
        }
    }    
}
