package mekanism.common.integration.computer;

import java.util.HashMap;
import java.util.Map;

public abstract class BoundMethodHolder {
    private final Map<String, MethodData> methods = new HashMap<>();

    public void register(String name, boolean threadSafe, String[] arguments, BoundCaller handler) {
        this.methods.put(name, new MethodData(name, threadSafe, arguments, handler));
    }

    public interface BoundCaller {
        Object apply(FancyComputerHelper u) throws ComputerException;
    }

    public record MethodData(String name, boolean threadSafe, String[] arguments, BoundCaller handler){}
}
