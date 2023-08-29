package mekanism.common.integration.computer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.Objects;
import mekanism.common.integration.computer.ComputerMethodFactory.ComputerFunctionCaller;
import mekanism.common.integration.computer.ComputerMethodFactory.MethodData;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BoundMethodHolder {
    private static final Comparator<BoundMethodData<?>> METHODDATA_COMPARATOR = Comparator.<BoundMethodData<?>, String>comparing(BoundMethodData::name).thenComparing(md -> md.argumentNames().length);
    private static final MethodData<ListMultimap<String, BoundMethodData<?>>> HELP_METHOD = new ComputerMethodFactory.MethodData<>("help", MethodRestriction.NONE, ComputerMethodFactory.NO_STRINGS, true, ComputerMethodFactory.NO_STRINGS, ComputerMethodFactory.NO_CLASSES, Map.class, BoundMethodHolder::generateHelp, null, false);
    private static final MethodData<ListMultimap<String, BoundMethodData<?>>> HELP_METHOD_WITH_NAME = new ComputerMethodFactory.MethodData<>("help", MethodRestriction.NONE, ComputerMethodFactory.NO_STRINGS, true, new String[]{"methodName"}, new Class[]{String.class}, Map.class, BoundMethodHolder::generateHelpSpecific, null, false);

    protected final ListMultimap<String, BoundMethodData<?>> methods = ArrayListMultimap.create();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    protected Lazy<String[]> methodNames = Lazy.of(()->this.methods.keys().toArray(new String[0]));

    public BoundMethodHolder() {
        register(HELP_METHOD, new WeakReference<>(this.methods));
        register(HELP_METHOD_WITH_NAME, new WeakReference<>(this.methods));
    }

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

    public static Object generateHelp(ListMultimap<String, BoundMethodData<?>> methods, BaseComputerHelper helper) {
        if (methods == null) {
            return null;
        }
        Map<String, MethodHelpData> helpItems = new HashMap<>();
        methods.values().stream().sorted(METHODDATA_COMPARATOR).forEach(md->
              helpItems.put(md.name()+"("+String.join(", ", md.argumentNames())+")", MethodHelpData.from(md))
        );
        return helper.convert(helpItems, helper::convert, helper::convert);
    }

    public static Object generateHelpSpecific(ListMultimap<String, BoundMethodData<?>> methods, BaseComputerHelper helper) throws ComputerException {
        if (methods == null) {
            return null;
        }
        String methodName = helper.getString(0);
        Map<String, MethodHelpData> helpItems = new HashMap<>();
        methods.values().stream().sorted(METHODDATA_COMPARATOR).filter(md->md.name().equalsIgnoreCase(methodName)).forEach(md->
              helpItems.put(md.name()+"("+String.join(", ", md.argumentNames())+")", MethodHelpData.from(md))
        );
        if (helpItems.isEmpty()) {
            return helper.convert("Method name not found: "+methodName);
        }
        return helper.convert(helpItems, helper::convert, helper::convert);
    }
}
