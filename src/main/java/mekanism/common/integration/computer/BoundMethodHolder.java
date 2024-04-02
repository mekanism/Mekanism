package mekanism.common.integration.computer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.common.integration.computer.ComputerMethodFactory.ComputerFunctionCaller;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public abstract class BoundMethodHolder {

    private static final Comparator<BoundMethodData<?>> METHODDATA_COMPARATOR = Comparator.<BoundMethodData<?>, String>comparing(BoundMethodData::name).thenComparing(md -> md.argumentNames().length);
    private static final MethodData<ListMultimap<String, BoundMethodData<?>>> HELP_METHOD = MethodData.builder("help", BoundMethodHolder::generateHelp)
          .returnType(Map.class)
          .returnExtra(String.class, MethodHelpData.class)
          .build();
    private static final MethodData<ListMultimap<String, BoundMethodData<?>>> HELP_METHOD_WITH_NAME = MethodData.builder("help", BoundMethodHolder::generateHelpSpecific)
          .returnType(Map.class)
          .returnExtra(String.class, MethodHelpData.class)
          .arguments(new String[]{"methodName"}, new Class[]{String.class})
          .build();


    protected final ListMultimap<String, BoundMethodData<?>> methods = ArrayListMultimap.create();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    protected Lazy<String[]> methodNames = Lazy.of(() -> this.methods.keys().toArray(new String[0]));

    protected BoundMethodHolder() {
        register(HELP_METHOD, new WeakReference<>(this.methods), true);
        register(HELP_METHOD_WITH_NAME, new WeakReference<>(this.methods), true);
    }

    public <T> void register(MethodData<T> method, @Nullable WeakReference<T> subject, boolean isHelpMethod) {
        if (!methodsKnown.add(new ObjectIntImmutablePair<>(method.name(), method.argumentNames().length))) {
            throw new RuntimeException("Duplicate method name " + method.name() + "_" + method.argumentNames().length);
        }
        this.methods.put(method.name(), new BoundMethodData<>(method, subject, isHelpMethod));
    }

    public record BoundMethodData<T>(MethodData<T> method, @Nullable WeakReference<T> subject, boolean isHelpMethod) {

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
            return o instanceof BoundMethodData<?> that && method.equals(that.method) && subjectEquals(that);
        }

        private boolean subjectEquals(BoundMethodData<?> that) {
            T mySubject = unwrappedSubject();
            Object otherSubject = that.unwrappedSubject();
            if (isHelpMethod) {//compare helps by reference, to avoid a Stack Overflow
                return mySubject == otherSubject;
            }
            return Objects.equals(mySubject, otherSubject);
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            T subject = unwrappedSubject();
            result = 31 * result + (subject == null ? 0 : subject.hashCode());
            return result;
        }
    }

    public static Object generateHelp(ListMultimap<String, BoundMethodData<?>> methods, BaseComputerHelper helper) {
        if (methods == null) {
            return helper.voidResult();
        }
        Map<String, MethodHelpData> helpItems = methods.values().stream()
              .sorted(METHODDATA_COMPARATOR)
              .collect(Collectors.toMap(md -> md.name() + "(" + String.join(", ", md.argumentNames()) + ")", MethodHelpData::from, (a, b) -> b));
        return helper.convert(helpItems, helper::convert, helper::convert);
    }

    public static Object generateHelpSpecific(ListMultimap<String, BoundMethodData<?>> methods, BaseComputerHelper helper) throws ComputerException {
        if (methods == null) {
            return helper.voidResult();
        }
        String methodName = helper.getString(0);
        List<BoundMethodData<?>> toSort = new ArrayList<>(methods.values());
        toSort.sort(METHODDATA_COMPARATOR);
        Map<String, MethodHelpData> helpItems = new HashMap<>();
        for (BoundMethodData<?> md : toSort) {
            if (md.name().equalsIgnoreCase(methodName)) {
                helpItems.put(md.name() + "(" + String.join(", ", md.argumentNames()) + ")", MethodHelpData.from(md));
            }
        }
        if (helpItems.isEmpty()) {
            return helper.convert("Method name not found: " + methodName);
        }
        return helper.convert(helpItems, helper::convert, helper::convert);
    }
}