package mekanism.common.attachments.containers;

import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ContainsRecipe<INPUT_CACHE extends IInputRecipeCache, TYPE> {

    boolean check(INPUT_CACHE cache, @Nullable Level level, TYPE value);
}