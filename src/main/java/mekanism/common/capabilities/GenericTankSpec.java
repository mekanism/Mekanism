package mekanism.common.capabilities;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;

public abstract class GenericTankSpec<TYPE> {

    public final Predicate<@NotNull TYPE> isValid;
    public final BiPredicate<@NotNull TYPE, @NotNull AutomationType> canExtract;
    public final TriPredicate<@NotNull TYPE, @NotNull AutomationType, @NotNull ItemStack> canInsert;
    private final Predicate<@NotNull ItemStack> supportsStack;

    protected GenericTankSpec(BiPredicate<@NotNull TYPE, @NotNull AutomationType> canExtract,
          TriPredicate<@NotNull TYPE, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<@NotNull TYPE> isValid,
          Predicate<@NotNull ItemStack> supportsStack) {
        this.isValid = isValid;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.supportsStack = supportsStack;
    }

    public boolean supportsStack(ItemStack stack) {
        return supportsStack.test(stack);
    }
}