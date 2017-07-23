package buildcraft.api.core;

import net.minecraftforge.common.config.Property;

@Deprecated
public interface IBuildCraftMod {
    Property getOption(String name);
}
