package cn.katoumegumi.java.common.function;

import java.util.Objects;

@FunctionalInterface
public interface TripleConsumer<V1,V2,V3> {

    void accept(V1 v1,V2 v2,V3 v3);

    default TripleConsumer<V1,V2,V3> andThen(TripleConsumer<? super V1,? super V2,? super V3> after) {
        Objects.requireNonNull(after);
        return (V1 v1, V2 v2, V3 v3) -> { accept(v1,v2,v3); after.accept(v1,v2,v3); };
    }
}
