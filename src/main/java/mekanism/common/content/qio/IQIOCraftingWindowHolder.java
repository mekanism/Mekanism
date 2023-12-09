package mekanism.common.content.qio;

import mekanism.api.IContentsListener;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IQIOCraftingWindowHolder extends IContentsListener {

    byte MAX_CRAFTING_WINDOWS = 3;

    @Nullable
    Level getLevel();

    QIOCraftingWindow[] getCraftingWindows();

    /**
     * @apiNote Only should be used on the server, so it is perfectly safe to always just be returning null when on the client.
     */
    @Nullable
    QIOFrequency getFrequency();
}