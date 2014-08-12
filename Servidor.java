public class Servidor {
    private String _dns;

    public Servidor(String dns) {
        _dns = dns;
    }

    public String nombre() {
        return _dns;
    }

    public void setDns(String dns) {
        _dns = dns;
    }
}
