package mekanism.api.providers;

import mekanism.api.text.IHasTextComponent;
import net.minecraft.util.ResourceLocation;

//TODO: Do we want an "implementation" of IRegistryObjectProvider
public interface IBaseProvider extends IHasTextComponent {

    ResourceLocation getRegistryName();

    default String getName() {
        return getRegistryName().getPath();
    }
}