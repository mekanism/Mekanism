package mekanism.common.content.qio;

import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import net.minecraft.world.World;

public interface IQIOCraftingWindowHolder extends IContentsListener {

    byte MAX_CRAFTING_WINDOWS = 3;

    @Nullable
    World getHolderWorld();

    QIOCraftingWindow[] getCraftingWindows();

    /**
     * @apiNote Only should be used on the server, so it is perfectly safe to always just be returning null when on the client.
     */
    @Nullable
    QIOFrequency getFrequency();
}