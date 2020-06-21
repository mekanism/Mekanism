package mekanism.client.jei.interfaces;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public interface IJEIGhostTarget {

    /**
     * @return {@code null} if it doesn't actually currently support ghost handling
     */
    @Nullable
    IGhostIngredientConsumer getGhostHandler();

    /**
     * Number of pixels on each side that make up the border, and should be ignored when creating the target area.
     */
    default int borderSize() {
        return 0;
    }

    interface IGhostIngredientConsumer extends Consumer<Object> {

        boolean supportsIngredient(Object ingredient);
    }

    interface IGhostItemConsumer extends IGhostIngredientConsumer {

        @Override
        default boolean supportsIngredient(Object ingredient) {
            return ingredient instanceof ItemStack && !((ItemStack) ingredient).isEmpty();
        }
    }

    interface IGhostBlockItemConsumer extends IGhostItemConsumer {

        @Override
        default boolean supportsIngredient(Object ingredient) {
            //Only allow block items
            return IGhostItemConsumer.super.supportsIngredient(ingredient) && ((ItemStack) ingredient).getItem() instanceof BlockItem;
        }
    }
}