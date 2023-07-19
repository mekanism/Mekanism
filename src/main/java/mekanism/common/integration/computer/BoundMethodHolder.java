package mekanism.common.integration.computer;

import java.util.HashMap;
import java.util.Map;

public abstract class BoundMethodHolder {
    private final Map<String, MethodData> methods = new HashMap<>();

    public <T> void register(String name, boolean threadSafe, String[] arguments, T subject, ComputerMethodFactory.ComputerFunctionCaller<T> handler) {
        this.methods.put(name, new MethodData(name, threadSafe, arguments, subject, handler));
    }

    public record MethodData(String name, boolean threadSafe, String[] arguments, Object subject, ComputerMethodFactory.ComputerFunctionCaller<?> handler){}
}
