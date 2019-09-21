package mekanism.api.function;

//TODO: JavaDoc, basically is a further specialization of IntFunction<Integer> so that auto boxing is not needed
@FunctionalInterface
public interface IntToIntFunction {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     *
     * @return the function result
     */
    int apply(int value);
}