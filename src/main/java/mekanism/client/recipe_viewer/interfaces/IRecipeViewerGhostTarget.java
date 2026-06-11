package mekanism.client.recipe_viewer.interfaces;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface IRecipeViewerGhostTarget {

    /// @return `null` if it doesn't actually currently support ghost handling
    @Nullable
    IGhostIngredientConsumer getGhostHandler();

    /// Number of pixels on each side that make up the border, and should be ignored when creating the target area.
    default int borderSize() {
        return 0;
    }

    interface IGhostIngredientConsumer extends Consumer<Object> {

        @Nullable
        Object supportedTarget(@Nullable Object ingredient);
    }

    interface IGhostItemConsumer extends IGhostIngredientConsumer {

        @Nullable
        @Override
        default ItemStack supportedTarget(@Nullable Object ingredient) {
            return ingredient instanceof ItemStack stack && !stack.isEmpty() ? stack : null;
        }
    }

    interface IGhostBlockItemConsumer extends IGhostItemConsumer {

        @Nullable
        @Override
        default ItemStack supportedTarget(@Nullable Object ingredient) {
            ItemStack supported = IGhostItemConsumer.super.supportedTarget(ingredient);
            //Only allow block items
            return supported != null && supported.getItem() instanceof BlockItem ? supported : null;
        }
    }
}