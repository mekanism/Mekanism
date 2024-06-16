package mekanism.api.functions;

@FunctionalInterface
public interface LongObjectToLongFunction<U> {
    long apply(long a, U b);
}
