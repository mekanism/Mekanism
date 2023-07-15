package mekanism.common.integration.computer;

import java.util.function.BiConsumer;

/**
 * Created by Thiakil on 15/07/2023.
 */
public class FancyComputerHandler<T>{

    protected void register(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, BiConsumer<T, FancyComputerHelper> handler) {
        //todo record class again
    }
}
