package buildcraft.api.lists;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class ListMatchHandler {
    public enum Type {
        TYPE,
        MATERIAL,
        CLASS
    }

    public abstract boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise);

    public abstract boolean isValidSource(Type type, @Nonnull ItemStack stack);

    /** Get custom client examples.
     * 
     * @param type
     * @param stack
     * @return A List (even empty!) if the examples satisfy this handler, null if iteration and .matches should be used
     *         instead. */
    public NonNullList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        return null;
    }
}
