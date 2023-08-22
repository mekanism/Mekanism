package mekanism.common.integration.computer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public abstract class BoundMethodHolder {
    protected final ListMultimap<String, MethodData> methods = ArrayListMultimap.create();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    protected Lazy<String[]> methodNames = Lazy.of(()->this.methods.keys().toArray(new String[0]));

    public <T> void register(String name, boolean threadSafe, String[] argumentNames, Class<?>[] argClasses, Class<?> returnType, T subject, ComputerMethodFactory.ComputerFunctionCaller<T> handler) {
        if (!methodsKnown.add(new ObjectIntImmutablePair<>(name, argumentNames.length))) {
            throw new RuntimeException("Duplicate method name "+name+"_"+argumentNames.length);
        }
        //noinspection unchecked
        this.methods.put(name, new MethodData(name, threadSafe, argumentNames, argClasses, returnType, subject != null ? new WeakReference<>(subject) : null, (ComputerMethodFactory.ComputerFunctionCaller<Object>) handler));
    }

    public record MethodData(String name, boolean threadSafe, String[] argumentNames, Class<?>[] argClasses, Class<?> returnType, @Nullable WeakReference<Object> subject, ComputerMethodFactory.ComputerFunctionCaller<Object> handler){
        public Object call(BaseComputerHelper helper) throws ComputerException {
            return handler.apply(subject == null ? null : subject.get(), helper);
        }
    }
}
