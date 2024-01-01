package mekanism.common.integration.computer;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A base class for the annotation generator to extend. It's constructor will call
 * {@link #register(String, MethodRestriction, String[], boolean, Class, ComputerFunctionCaller, String[], Class[])} to set up the possible methods. These will then be
 * tested and, if not restricted, "bound" to the holder in {@link #bindTo(Object, BoundMethodHolder)} Methods with the same name must have different parameter counts.
 *
 * @param <T> the "subject" that this Factory's methods operate on.
 */
@ParametersAreNotNullByDefault
public class ComputerMethodFactory<T> {

    protected static String[] NO_STRINGS = new String[0];
    protected static Class<?>[] NO_CLASSES = new Class[0];
    private static final Comparator<MethodData<?>> METHODDATA_COMPARATOR = Comparator.<MethodData<?>, String>comparing(MethodData::name).thenComparing(md -> md.argumentNames().length);

    protected static MethodHandles.Lookup lookup = MethodHandles.lookup();

    @SuppressWarnings("unused")//may be used by annotation processor
    protected static MethodHandle getMethodHandle(Class<?> containingClass, String methodName, Class<?>... params) {
        try {
            Method method = containingClass.getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return lookup.unreflect(method);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Couldn't get method handle for " + methodName, roe);
        }
    }

    @SuppressWarnings("unused")//may be used by annotation processor
    protected static MethodHandle getGetterHandle(Class<?> containingClass, String fieldName) {
        try {
            Field field = containingClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return lookup.unreflectGetter(field);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Couldn't get getter MethodHandle for " + fieldName, roe);
        }
    }

    private final List<MethodData<T>> methods = new ArrayList<>();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    protected void register(MethodData.Builder<T> methodData) {
        this.register(methodData.build());
    }

    protected void register(MethodData<T> methodData) {
        if (!methodsKnown.add(new ObjectIntImmutablePair<>(methodData.name(), methodData.argumentNames().length))) {
            throw new RuntimeException("Duplicate method name " + methodData.name() + "_" + methodData.argumentNames().length);
        }
        this.methods.add(methodData);
    }

    void bindTo(@Nullable T subject, BoundMethodHolder holder) {
        WeakReference<T> weakSubject = subject == null ? null : new WeakReference<>(subject);
        for (MethodData<T> methodData : this.methods) {
            if (methodData.supports(subject)) {
                holder.register(methodData, weakSubject, false);
            }
        }
    }

    public List<MethodHelpData> getHelpData() {
        return this.methods.stream().sorted(METHODDATA_COMPARATOR).map(MethodHelpData::from).collect(Collectors.toList());
    }

    @FunctionalInterface
    public interface ComputerFunctionCaller<T> {

        /**
         * Applies this function to the given arguments.
         *
         * @param t the subject
         * @param u the computer helper for the current integration
         *
         * @return the (converted) function result
         *
         * @throws ComputerException if arguments are invalid or other failure happens during processing
         */
        Object apply(@Nullable T t, BaseComputerHelper u) throws ComputerException;
    }

}