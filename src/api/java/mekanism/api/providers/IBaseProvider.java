package mekanism.api.providers;

import mekanism.api.text.IHasTextComponent;
import net.minecraft.util.ResourceLocation;

public interface IBaseProvider extends IHasTextComponent {

    ResourceLocation getRegistryName();

    default String getName() {
        return getRegistryName().getPath();
    }
}