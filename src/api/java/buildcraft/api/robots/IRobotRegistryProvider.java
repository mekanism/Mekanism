package buildcraft.api.robots;

import net.minecraft.world.World;

public interface IRobotRegistryProvider {
    IRobotRegistry getRegistry(World world);
}
