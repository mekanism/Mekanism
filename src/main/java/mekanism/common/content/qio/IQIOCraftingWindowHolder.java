package mekanism.common.content.qio;

import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import net.minecraft.world.World;

public interface IQIOCraftingWindowHolder extends IContentsListener {

    byte MAX_CRAFTING_WINDOWS = 3;

    @Nullable
    World getHolderWorld();

    QIOCraftingWindow[] getCraftingWindows();
}