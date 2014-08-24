/**
 * Clase que contiene todas las configuraciones de mensajes y generales
 * del resto de las clases.
 * @author Fabiola Rosato
 * @author José Delgado
 */
public class Data {
    public static final String SPLIT = "-";
    public static final String SUBSPLIT = ",";
    public static final String SUBJECT_ACTUALIZAR   = "Actualizar";
    public static final String SUBJECT_BORRAR       = "Borrar";
    public static final String SUBJECT_BUSCAR       = "Buscar";
    public static final String SUBJECT_CARDINALIDAD = "Cardinalidad";
    public static final String SUBJECT_CREAR        = "Crear";
    public static final String SUBJECT_ELIMINAR     = "Eliminar";
    public static final String SUBJECT_INICIO       = "Inicio";
    public static final String SUBJECT_INSERTAR     = "Insertar";
    public static final String SUBJECT_JOINING      = "Joining";
    public static final String SUBJECT_LEAVING      = "Leaving";
    public static final String SUBJECT_ROLLBACK     = "Rollback";
    public static final String SUBJECT_SET          = "Set";

    public static final String MSG_ACTUALIZAR    = "Para actualizar una tupla de un conjunto, ingrese:";
    public static final String MSG_BORRAR        = "Para borrar una tupla de un conjunto, ingrese:";
    public static final String MSG_BUSCAR        = "Para buscar una tupla en un conjunto, ingrese:";
    public static final String MSG_CLAVE         = "Clave de la tupla:";
    public static final String MSG_CONFIGURACION = "Para obtener la configuración de un conjunto, ingrese:";
    public static final String MSG_CREAR         = "Para crear un conjunto de tuplas, ingrese:";
    public static final String MSG_DIMENSION     = "Número de elementos de una tupla (> = 2):";
    public static final String MSG_ELEMENTO      = "La lista de elementos de la tupla (escriba \"done\" para terminar)";
    public static final String MSG_ELIMINAR      = "Para eliminar un conjunto de tuplas, ingrese:";
    public static final String MSG_INICIO        = "Iniciando TuplaAdmin";
    public static final String MSG_INPUT         = "";
    public static final String MSG_INSERTAR      = "Para insertar una tupla en un conjunto, ingrese:";
    public static final String MSG_NOMBRE        = "Nombre del conjunto de tuplas:";
    public static final String MSG_POSICION      = "Posición a actualizar:";
    public static final String MSG_SERVIDOR      = "La lista de servidores en las que se ubicará la tupla (escriba \"done\" para terminar)";
    public static final String MSG_STARTUP       = "Iniciando servidor ";
    public static final String MSG_TIPO          = "Ingrese 1 si es segmentado, 2 si es replicado o 3 si es particionado.";
    public static final String MSG_VALOR         = "Valor nuevo:";
    public static final String MSG_MENU = "Ingrese el número de la acción a realizar:\n" +
        "\t1 - Crear un conjunto\n" +
        "\t2 - Eliminar un conjunto\n" + 
        "\t3 - Insertar una tupla en un conjunto\n" + 
        "\t4 - Borrar una tupla de un conjunto\n" + 
        "\t5 - Buscar una tupla en un conjunto\n" +
        "\t6 - Actualizar una tupla de un conjunto\n" +
        "\t7 - Obtener la configuración de un conjunto\n\n" +
        "\t0 - Salir";

    public static final String LOG_PATH          = "tuplas.log";

    public static final String ERR_ACTUALIZAR    = "La tupla no pudo ser modificada - ocurrió algún problema en los servidores";
    public static final String ERR_BORRAR        = "No se pudo borrar la tupla - ocurrió un problema en los servidores";
    public static final String ERR_BUSCAR        = "No se encontró la tupla";
    public static final String ERR_CREAR         = "No se pudo crear la tupla - no hay servidores disponibles";
    public static final String ERR_DIMENSION     = "El número de elementos de una tupla debe ser mayor o igual a 2.";
    public static final String ERR_ELIMINAR      = "No se pudo eliminar el conjunto - ocurrió un problema en los servidores";
    public static final String ERR_INSERTAR      = "No se pudo insertar la tupla - ocurrió un problema en los servidores";
    public static final String ERR_INTENTOS      = "Demasiados intentos fallidos. Intente de nuevo";
    public static final String ERR_SERVIDOR      = "No se encontró disponible al servidor ";
    public static final String ERR_TIPO          = "Tipo inválido. Opciones -\n\t1 - segmentado\n\t2 - replicado\n\t3 - particionado";
    public static final String ERR_EXISTE        = "No existe el conjunto "; 


    public static final String EXITO_ACTUALIZAR  = "La tupla se actualizó satisfactoriamente";
    public static final String EXITO_BUSCAR      = "La tupla fue encontrada satisfactoriamente.";
    public static final String EXITO_CREAR       = "El conjunto se creó satisfactoriamente.";
    public static final String EXITO_ELIMINAR    = "El conjunto se eliminó satisfactoriamente.";
    public static final String EXITO_INSERTAR    = "La tupla se agregó satisfactoriamente.";
    public static final String EXITO_BORRAR      = "La tupla se borró satisfactoriamente.";

    public static final int SEGMENTADO   = 1;
    public static final int REPLICADO    = 2;
    public static final int PARTICIONADO = 3;

    public static final int ACK = 1;

    public static void print(String id, String msg) {
        System.out.println(id + "> " + msg);
    }

    public static void printErr(String id, String msg) {
        System.err.println(id + "> " + msg);
    }


}
