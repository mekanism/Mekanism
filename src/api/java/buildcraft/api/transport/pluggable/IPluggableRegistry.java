package buildcraft.api.transport.pluggable;

import net.minecraft.util.ResourceLocation;

public interface IPluggableRegistry {
    void register(PluggableDefinition definition);

    PluggableDefinition getDefinition(ResourceLocation identifier);
}
