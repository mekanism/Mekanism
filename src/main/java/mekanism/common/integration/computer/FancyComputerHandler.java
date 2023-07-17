package mekanism.common.integration.computer;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thiakil on 15/07/2023.
 */
public class FancyComputerHandler<T>{
    protected static Object[] EMPTY_ARRAY = new Object[0];
    @SuppressWarnings("unchecked")
    protected static <O> O[] emptyArray() {
        return (O[]) EMPTY_ARRAY;
    }
    protected static MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected static <RETURN> RETURN catchingMethodHandle(MHUser<RETURN> supplier) {
        try {
            return supplier.supply();
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);//todo better method handle exception checking?
        }
    }
    protected interface MHUser<RETURN> {
        RETURN supply() throws Throwable;
    }

    private Map<String, MethodData<T>> methods = new HashMap<>();

    protected void register(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] arguments, ComputerFunctionCaller<T, FancyComputerHelper, Object> handler) {
        this.methods.put(name, new MethodData<>(name, restriction, requiredMods, threadSafe, arguments, handler));
    }

    public interface ComputerFunctionCaller<T, U, R> {

        /**
         * Applies this function to the given arguments.
         *
         * @param t the first function argument
         * @param u the second function argument
         * @return the function result
         */
        R apply(T t, U u) throws ComputerException;
    }
    record MethodData<T>(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] arguments, ComputerFunctionCaller<T, FancyComputerHelper, Object> handler){}
}
