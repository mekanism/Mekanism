package mekanism.common.tags;

import mekanism.common.Mekanism;
import net.minecraft.resources.IReloadableResourceManager;

public class TagManagerReloadHelper {

    public static void addListener(IReloadableResourceManager resourceManager) {
        //TODO: It would make sense to eventually make a PR to forge to make custom tags easier to do/manage
        resourceManager.addReloadListener(Mekanism.instance.getTagManager());
    }
}