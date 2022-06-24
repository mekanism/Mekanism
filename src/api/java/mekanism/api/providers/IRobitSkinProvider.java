package mekanism.api.providers;

import mekanism.api.robit.RobitSkin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface IRobitSkinProvider extends IBaseProvider {

    /**
     * Gets the robit skin this provider represents.
     */
    RobitSkin getSkin();

    @Override
    default ResourceLocation getRegistryName() {
        return getSkin().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return getSkin().getTranslationKey();
    }
}