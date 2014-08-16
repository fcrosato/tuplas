public class Data {
    public static final String SPLIT = "-";
    public static final String SUBSPLIT = ",";
    public static final String SUBJECT_LEAVING  = "Leaving";
    public static final String SUBJECT_JOINING  = "Joining";
    public static final String SUBJECT_SET      = "Set";
    public static final String SUBJECT_CREAR    = "Crear";
    public static final String SUBJECT_ELIMINAR = "Eliminar";
    public static final String SUBJECT_INSERTAR = "Insertar";

    public static final int SEGMENTADO   = 1;
    public static final int REPLICADO    = 2;
    public static final int PARTICIONADO = 3;

    public static void print(Object msg) {
        System.out.println(msg.toString());
    }

}
