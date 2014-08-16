public class Servidor {
    public String ip;
    public int carga;

    Servidor (String _ip, int _carga) {
        ip = _ip;
        carga = _carga;
    }

    @Override
    public String toString() {
        return ("ip: " + ip + " | carga: " + carga);
    }
}
