package org.apache.shenyu.client.core.client.matcher;

import java.util.Objects;

public interface Matcher<T> {

    boolean match(T element);

    default Matcher<T> and(Matcher<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> match(t) && other.match(t);
    }

    default Matcher<T> or(Matcher<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> match(t) || other.match(t);
    }

    default Matcher<T> negate() {
        return (t) -> !match(t);
    }

    static <T> Matcher<T> not(Matcher<? super T> target) {
        Objects.requireNonNull(target);
        return (Matcher<T>) target.negate();
    }
}
