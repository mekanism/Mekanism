package mekanism.common.integration.computer;

import mekanism.common.integration.computer.ComputerMethodFactory.ComputerFunctionCaller;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public record MethodData<T>(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] argumentNames, Class<?>[] argClasses,
                            Class<?> returnType, Class<?>[] returnExtra, ComputerFunctionCaller<T> handler, @Nullable String methodDescription,
                            boolean requiresPublicSecurity) {

    public boolean supports(@Nullable T subject) {
        return restriction.test(subject) && modsLoaded(requiredMods);
    }

    private boolean modsLoaded(String[] mods) {
        for (String mod : mods) {
            if (!ModList.get().isLoaded(mod)) {
                return false;
            }
        }
        return true;
    }
}
