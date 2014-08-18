import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.ConnectException;
import java.rmi.NotBoundException;

public class TuplaAdmin {
    List<String> tuplas;

    private static final String MSG_CREAR         = "Para crear un conjunto de tuplas, ingrese:";
    private static final String MSG_CONFIGURACION = "Para obtener la configuración de un conjunto, ingrese:";
    private static final String MSG_INSERTAR      = "Para insertar una tupla en un conjunto, ingrese:";
    private static final String MSG_ACTUALIZAR    = "Para actualizar una tupla de un conjunto, ingrese:";
    private static final String MSG_BORRAR        = "Para borrar una tupla de un conjunto, ingrese:";
    private static final String MSG_BUSCAR        = "Para buscar una tupla en un conjunto, ingrese:";
    private static final String MSG_ELIMINAR      = "Para eliminar un conjunto de tuplas, ingrese:";
    private static final String MSG_NOMBRE        = "Nombre del conjunto de tuplas:";
    private static final String MSG_CLAVE         = "Clave de la tupla:";
    private static final String MSG_POSICION      = "Posición a actualizar:";
    private static final String MSG_VALOR         = "Valor nuevo:";
    private static final String MSG_DIMENSION     = "Número de elementos de una tupla (> = 2):";
    private static final String ERR_DIMENSION     = "El número de elementos de una tupla debe ser mayor o igual a 2.";
    private static final String MSG_TIPO          = "Ingrese 1 si es segmentado, 2 si es replicado o 3 si es particionado.";
    private static final String ERR_TIPO          = "Tipo inválido. Opciones -\n\t1 - segmentado\n\t2 - replicado\n\t3 - particionado";
    private static final String ERR_INTENTOS      = "Demasiados intentos fallidos. Intente de nuevo";
    private static final String MSG_SERVIDOR      = "La lista de servidores en las que se ubicará la tupla (escriba \"done\" para terminar)";
    private static final String MSG_ELEMENTO      = "La lista de elementos de la tupla (escriba \"done\" para terminar)";
    private static final String MSG_INPUT         = "";
    private static final String MSG_INICIO        = "Iniciando TuplaAdmin";
    private static final int CREAR      = 1;
    private static final int ELIMINAR   = 2;
    private static final int INSERTAR   = 3;
    private static final int BORRAR     = 4;
    private static final int BUSCAR     = 5;
    private static final int ACTUALIZAR = 6;
    private static final int CONFIG     = 7;
    private static final int SALIR      = 0;
    private static final String MSG_MENU = "Ingrese el número de la acción a realizar:\n" +
        "\t1 - Crear un conjunto\n" +
        "\t2 - Eliminar un conjunto\n" + 
        "\t3 - Insertar una tupla en un conjunto\n" + 
        "\t4 - Borrar una tupla de un conjunto\n" + 
        "\t5 - Buscar una tupla en un conjunto\n" +
        "\t6 - Actualizar una tupla de un conjunto\n" +
        "\t7 - Obtener la configuración de un conjunto\n\n" +
        "\t0 - Salir";

    private static void println(String msg) {
        System.out.println("cliente> " + msg);
    }

    private static void print(String msg) {
        System.out.print("cliente> " + msg);
    }

    private static void printErr(String msg) {
        System.err.println("cliente> Error: " + msg);
    }

    private static String getString(Scanner in, String msg) {
        println(msg);
        print(MSG_INPUT);
        String nombre = in.next();
        return nombre;
    }

    private static String getNombre(Scanner in) {
        return getString(in, MSG_NOMBRE);
    }
    
    private static String getClave(Scanner in) {
        return getString(in, MSG_CLAVE);
    }

    private static String getValor(Scanner in) {
        return getString(in, MSG_VALOR);
    }
    
    private static int getDimension(Scanner in) {
        println(MSG_DIMENSION);
        print(MSG_INPUT);
        int intentos = 0;
        int dimension = in.nextInt();
        while (dimension < 2 && intentos++ < 3) {
            printErr(ERR_DIMENSION);
            print(MSG_INPUT);
            dimension = in.nextInt();
        }
        if (intentos == 3) {
            printErr(ERR_INTENTOS);
            return -1;
        }
        return dimension;
    }

    private static int getTipo(Scanner in) {
        println(MSG_TIPO);
        print(MSG_INPUT);
        int intentos = 0;
        int tipo = in.nextInt();
        while ((1 > tipo || tipo > 3) && intentos++ < 3) {
            printErr(ERR_TIPO);
            print(MSG_INPUT);
            tipo = in.nextInt();
        }
        if (intentos == 3) {
            printErr(ERR_INTENTOS);
            return -1;
        }
        return tipo;
    }

    private static int getPosicion(Scanner in) {
        println(MSG_POSICION);
        print(MSG_INPUT);
        int posicion = in.nextInt();
        return posicion;
    }

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

    private static List<String> getServidores(Scanner in) {
        return getLista(in, MSG_SERVIDOR);
    }

    private static List<String> getElementos(Scanner in) {
        return getLista(in, MSG_ELEMENTO);
    }

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
    public static boolean crear(TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(MSG_CREAR);
        String nombre = getNombre(in);
        int dimension = getDimension(in);
        if (dimension == -1) {
            return false;
        }
        int tipo = getTipo(in);
        if (tipo == -1) {
            return false;
        }
        return tuplad.crear(nombre, dimension, tipo);
    }

    /**
     * Método que elimina un conjunto de tuplas.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @return true si se elimina la tupla, false en caso de que no exista.
     */
    public static boolean eliminar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(MSG_ELIMINAR);
        String nombre = getNombre(in);
        return tuplad.eliminar(nombre);
    } 


    /**
     * Método que inserta una tupla.
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param ti Tupla a insertar
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public static boolean insertar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(MSG_INSERTAR);
        String nombre = getNombre(in);
        List<String> tupla = getLista(in, MSG_ELEMENTO); 
        return tuplad.insertar(nombre, tupla); 
    }


    /**
     * Método que borra una tupla de un conjunto de tuplas
     *
     * @param nombre Identificador del conjunto de tuplas
     * @param clave Clave de la tupla a borrar.
     * @return true si se agrega la tupla, false en caso de fallas.
     */
    public static boolean borrar(TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(MSG_BORRAR);
        String nombre = getNombre(in);
        String clave = getClave(in);
        return tuplad.borrar(nombre, clave);
    }

    /**
      * Método que busca una tupla dentro de un conjunto de tuplas.
      *
      * @param nombre Identificador del conjunto de tuplas
      * @param clave Clave de la tupla a actualizar
      * @return El conjunto de valores de la tupla.
      */
    public static List<String> buscar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(MSG_BUSCAR);
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
      * @param nombre Identificador del conjunto de tuplas
      * @param clave Clave de la tupla a actualizar
      * @param posicion Posición del valor a actualizar
      * @param valor Valor nuevo del elemento de la tupla.
      * @return true si el valor se actualizó satisfactoriamente, 
                false en caso contrario.
      */
    public static boolean actualizar (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(MSG_ACTUALIZAR);
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
      * @param nombre Identificador del conjunto de tuplas.
      * @return UInformación de configuración del conjunto de tuplas.
      */
    public static String configuracion (TuplaDInterfaz tuplad, Scanner in) throws RemoteException {
        println(MSG_CONFIGURACION);
        String nombre = getNombre(in);
        String result = tuplad.configuracion(nombre);
        println(result);
        return result;
    }


    private static void menu(TuplaDInterfaz tuplad) throws RemoteException {
        try (Scanner in = new Scanner(System.in)){
            boolean exit = false;
            int option = -1;
            while (!exit) {
                println(MSG_MENU);
                print(MSG_INPUT);
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


    public static void main(String args[]) {
        /*
           if (System.getSecurityManager() == null) {
           System.setSecurityManager(new SecurityManager());
           }
           */
        try {
            println(MSG_INICIO);
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
