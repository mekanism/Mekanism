package buildcraft.api.core;

import net.minecraftforge.common.config.Property;

public interface IBuildCraftMod {
    Property getOption(String name);
}
