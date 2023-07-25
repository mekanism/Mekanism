package mekanism.common.integration.computer;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraftforge.fml.ModList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@ParametersAreNotNullByDefault
public class ComputerMethodFactory<T>{
    protected static String[] NO_STRINGS = new String[0];
    protected static Class<?>[] NO_CLASSES = new Class[0];

    protected static MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected static MethodHandle getMethodHandle(Class<?> containingClass, String methodName, Class<?>... params) {
        try {
            Method method = containingClass.getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return lookup.unreflect(method);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Couldn't get method handle for "+methodName, roe);
        }
    }

    protected static MethodHandle getGetterHandle(Class<?> containingClass, String fieldName) {
        try {
            Field field = containingClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return lookup.unreflectGetter(field);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Couldn't get getter methodhandle for "+fieldName, roe);
        }
    }

    protected static void unwrapException(Throwable t) throws ComputerException {
        if (t instanceof WrongMethodTypeException wmte) {
            throw new RuntimeException("Method not bound correctly: "+wmte.getMessage(), wmte);
        } else if (t.getCause() instanceof ComputerException cause){
            throw cause;
        }
        throw new RuntimeException(t.getMessage(), t);
    }

    private final List<MethodData<T>> methods = new ArrayList<>();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    protected void register(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] argumentNames, Class<?>[] argClasses, Class<?> returnType, ComputerFunctionCaller<T> handler) {
        if (!methodsKnown.add(new ObjectIntImmutablePair<>(name, argumentNames.length))) {
            throw new RuntimeException("Duplicate method name "+name+"_"+argumentNames.length);
        }
        this.methods.add(new MethodData<>(name, restriction, requiredMods, threadSafe, argumentNames, argClasses, returnType, handler));
    }

    void bindTo(T subject, BoundMethodHolder holder) {
        for (MethodData<T> methodData : this.methods) {
            if (methodData.restriction.test(subject) && modsLoaded(methodData.requiredMods)) {
                holder.register(methodData.name(), methodData.threadSafe(), methodData.argumentNames(), methodData.argClasses(), methodData.returnType(), subject, methodData.handler());
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
        Object apply(T t, BaseComputerHelper u) throws ComputerException;
    }

    public record MethodData<T>(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] argumentNames, Class<?>[] argClasses, Class<?> returnType, ComputerFunctionCaller<T> handler){}

    protected interface MHUser<RETURN> {
        RETURN supply() throws Throwable;
    }
}
