package mekanism.common.integration.computer;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraftforge.fml.ModList;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thiakil on 15/07/2023.
 */
@ParametersAreNotNullByDefault
public class ComputerMethodFactory<T>{
    protected static Object[] EMPTY_ARRAY = new Object[0];
    @SuppressWarnings("unchecked")
    protected static <O> O[] emptyArray() {
        return (O[]) EMPTY_ARRAY;
    }
    protected static MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected static <RETURN> RETURN catchingMethodHandle(MHUser<RETURN> supplier) throws ComputerException {
        try {
            return supplier.supply();
        } catch (WrongMethodTypeException wmte) {
            throw new RuntimeException("Method not bound correctly: "+wmte.getMessage(), wmte);
        } catch (Throwable t) {
            if (t.getCause() instanceof ComputerException cause){
                throw cause;
            }
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    private Map<String, MethodData<T>> methods = new HashMap<>();

    protected void register(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] arguments, ComputerFunctionCaller<T> handler) {
        this.methods.put(name, new MethodData<>(name, restriction, requiredMods, threadSafe, arguments, handler));
    }

    void bindTo(T subject, BoundMethodHolder holder) {
        for (Map.Entry<String, MethodData<T>> entry : this.methods.entrySet()) {
            MethodData<T> methodData = entry.getValue();
            if (methodData.restriction.test(subject) && modsLoaded(methodData.requiredMods)) {
                holder.register(entry.getKey(), methodData.threadSafe, methodData.arguments, (helper)->methodData.handler.apply(subject, helper));
            }
        }
    }

    private boolean modsLoaded(String[] mods) {
        if (mods.length == 0) {
            return true;
        }
        for (String mod : mods) {
            if (!ModList.get().isLoaded(mod)) {
                return false;
            }
        }
        return true;
    }

    public interface ComputerFunctionCaller<T> {

        /**
         * Applies this function to the given arguments.
         *
         * @param t the first function argument
         * @param u the second function argument
         * @return the function result
         */
        Object apply(T t, FancyComputerHelper u) throws ComputerException;
    }

    public record MethodData<T>(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] arguments, ComputerFunctionCaller<T> handler){}

    protected interface MHUser<RETURN> {
        RETURN supply() throws Throwable;
    }
}
