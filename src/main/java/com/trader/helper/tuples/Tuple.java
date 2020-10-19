package com.trader.helper.tuples;

import lombok.Data;

import java.util.Objects;

/**
 * @author yjt
 * @since 2020/10/19 下午8:18
 */
@Data
public class Tuple<T, E> {

    private T o1;

    private E o2;

    public static <T, E> Tuple<T, E> of(T o1, E o2) {
        Tuple<T, E> tuple = new Tuple<>();
        tuple.o1 = o1;
        tuple.o2 = o2;
        return tuple;
    }

    @Override
    public boolean equals(Object target) {
        if (!(target instanceof Tuple)) {
            return false;
        }

        if (target == null) {
            return false;
        }
        return o1.equals(((Tuple) target).o1) && o2.equals(((Tuple) target).o2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(o1, o2);
    }

    @Override
    public String toString() {
        return o1.toString() + o2.toString();
    }
}
