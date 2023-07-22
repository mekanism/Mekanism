package mekanism.common.integration.computer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BoundMethodHolder {
    private final Multimap<String, MethodData> methods = HashMultimap.create();
    /**
     * Method + arg count pairs to make sure methods are unique
     */
    private final Set<ObjectIntPair<String>> methodsKnown = new HashSet<>();

    public <T> void register(String name, boolean threadSafe, String[] arguments, T subject, ComputerMethodFactory.ComputerFunctionCaller<T> handler) {
        if (!methodsKnown.add(new ObjectIntImmutablePair<>(name, arguments.length))) {
            throw new RuntimeException("Duplicate method name "+name+"_"+arguments.length);
        }
        this.methods.put(name, new MethodData(name, threadSafe, arguments, subject, handler));
    }

    public record MethodData(String name, boolean threadSafe, String[] arguments, Object subject, ComputerMethodFactory.ComputerFunctionCaller<?> handler){}
}
