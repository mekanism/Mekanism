package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.robit.RobitSkin;
import net.minecraft.util.ResourceLocation;

public interface IRobitSkinProvider extends IBaseProvider {

    /**
     * Gets the robit skin this provider represents.
     */
    @Nonnull
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