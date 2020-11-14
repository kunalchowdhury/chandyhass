public class B {
    String a, b;

    public B(String a, String b) {
        this.a = a;
        this.b = b;
    }

    void boom(String s, int v){
        a = a + "s";
        b = b + Integer.toOctalString(v);
    }
}
