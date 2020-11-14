public class Point {
    int x, y;
    A a;
    B b;

    public Point(A a, B b) {
        this.a = a;
        this.b = b;

    }

    void move(int dx, int dy) {
        x += dx; y += dy;
        a.fun();
        b.boom("From" , 100);

    }
}
