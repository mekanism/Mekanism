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
    private static final Comparator<BoundMethodData<?>> METHODDATA_COMPARATOR = Comparator.<MethodData<?>, String>comparing(MethodData::name).thenComparing(md -> md.argumentNames().length);
    private static final MethodData<ListMultimap<String, BoundMethodData<?>>> HELP_METHOD = new ComputerMethodFactory.MethodData<>("help", MethodRestriction.NONE, ComputerMethodFactory.NO_STRINGS, true, ComputerMethodFactory.NO_STRINGS, ComputerMethodFactory.NO_CLASSES, Map.class, BoundMethodHolder::generateHelp);
    protected final ListMultimap<String, BoundMethodData<?>> methods = ArrayListMultimap.create();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    protected Lazy<String[]> methodNames = Lazy.of(()->this.methods.keys().toArray(new String[0]));

    public BoundMethodHolder() {
        register(HELP_METHOD, new WeakReference<>(this.methods));
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
        Map<String, Object> helpItems = new HashMap<>();
        methods.values().stream().sorted(METHODDATA_COMPARATOR).forEach(md->
              helpItems.put(md.name()+"("+String.join(", ", md.argumentNames())+")", generateHelpItem(md))
        );
        return helpItems;
    }

    private static Map<String, Object> generateHelpItem(BoundMethodData<?> data) {
        Map<String, Object> helpData = new HashMap<>();
        List<Map<String, Object>> params = new ArrayList<>();
        helpData.put("params", params);
        for (int i = 0; i < data.argumentNames.length; i++) {
            Map<String, Object> arg = new HashMap<>();
            arg.put("name", data.argumentNames[i]);
            arg.put("type", getHumanType(data.argClasses[i]));
            if (Enum.class.isAssignableFrom(data.argClasses[i])) {
                List<String> constList = getEnumConstantNames(data.argClasses[i]);
                arg.put("values", constList);
            }
            params.add(arg);
        }
        if (data.returnType != void.class) {
            helpData.put("returns", getHumanType(data.returnType));
            if (Enum.class.isAssignableFrom(data.returnType)) {
                helpData.put("returnValues", getEnumConstantNames(data.returnType));
            }
        }
        if (data.methodDescription() != null) {
            helpData.put("description", data.methodDescription());
        }
        return helpData;
    }

    @NotNull
    private static String getHumanType(Class<?> type) {
        Class<?> convertedType = BaseComputerHelper.convertType(type);
        return convertedType == Map.class ? "Table" : convertedType.getSimpleName();
    }

    @NotNull
    private static List<String> getEnumConstantNames(Class<?> argClass) {
        Enum<?>[] enumConstants = ((Class<? extends Enum<?>>) argClass).getEnumConstants();
        return Arrays.stream(enumConstants).map(Enum::name).toList();
    }
}
