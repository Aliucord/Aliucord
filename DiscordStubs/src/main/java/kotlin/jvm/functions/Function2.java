package kotlin.jvm.functions;

public interface Function2<P1, P2, R> {
    R invoke(P1 p1, P2 p2);
}
