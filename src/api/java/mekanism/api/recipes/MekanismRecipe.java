package mekanism.api.recipes;

import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.inventory.IgnoredIInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/**
 * Base class for helping wrap our recipes into IRecipes.
 */
public abstract class MekanismRecipe implements Recipe<IgnoredIInventory> {//TODO: Should we make implementations override equals and hashcode?

    private final ResourceLocation id;

    /**
     * @param id Recipe name.
     */
    protected MekanismRecipe(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "Recipe name cannot be null.");
    }

    /**
     * Writes this recipe to a PacketBuffer.
     *
     * @param buffer The buffer to write to.
     */
    public abstract void write(FriendlyByteBuf buffer);

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(@Nonnull IgnoredIInventory inv, @Nonnull Level world) {
        //TODO: Decide if we ever want to make use of this method
        //Default to not being able to match incomplete recipes though
        return !isIncomplete();
    }

    @Override
    public boolean isSpecial() {
        //Note: If we make this non-dynamic, we can make it show in vanilla's crafting book and also then obey the recipe locking.
        // For now none of that works/makes sense in our concept so don't lock it
        return true;
    }

    //Force implementation of this method as our ingredients is always empty so the super implementation would have all ours as incomplete
    @Override
    public abstract boolean isIncomplete();

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull IgnoredIInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}