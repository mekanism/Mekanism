package mekanism.common.integration.computer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.Objects;
import mekanism.common.integration.computer.ComputerMethodFactory.ComputerFunctionCaller;
import mekanism.common.integration.computer.ComputerMethodFactory.MethodData;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public abstract class BoundMethodHolder {
    protected final ListMultimap<String, BoundMethodData<?>> methods = ArrayListMultimap.create();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    protected Lazy<String[]> methodNames = Lazy.of(()->this.methods.keys().toArray(new String[0]));

    public <T> void register(MethodData<T> method, @Nullable WeakReference<T> subject) {
        if (!methodsKnown.add(new ObjectIntImmutablePair<>(method.name(), method.argumentNames().length))) {
            throw new RuntimeException("Duplicate method name "+method.name()+"_"+method.argumentNames().length);
        }
        this.methods.put(method.name(), new BoundMethodData<>(method, subject));
    }

    public record BoundMethodData<T>(MethodData<T> method, @Nullable WeakReference<T> subject) {

        public Object call(BaseComputerHelper helper) throws ComputerException {
            return method.handler().apply(unwrappedSubject(), helper);
        }

        @Nullable
        private T unwrappedSubject() {
            return subject == null ? null : subject.get();
        }

        public String name() {
            return method.name();
        }

        public boolean threadSafe() {
            return method.threadSafe();
        }

        public String[] argumentNames() {
            return method.argumentNames();
        }

        public Class<?>[] argClasses() {
            return method.argClasses();
        }

        public Class<?> returnType() {
            return method.returnType();
        }

        public ComputerFunctionCaller<T> handler() {
            return method.handler();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof BoundMethodData<?> that && method.equals(that.method) && Objects.equals(unwrappedSubject(), that.unwrappedSubject());
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            T subject = unwrappedSubject();
            result = 31 * result + (subject != null ? subject.hashCode() : 0);
            return result;
        }
    }
}
