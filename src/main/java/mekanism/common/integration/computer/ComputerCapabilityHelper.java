package mekanism.common.integration.computer;

import java.util.function.Consumer;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.integration.computer.computercraft.CCCapabilityHelper;
import mekanism.common.integration.computer.opencomputers2.OC2CapabilityHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ComputerCapabilityHelper {

    public static <TILE extends BlockEntity & IComputerTile> void addComputerCapabilities(TILE tile, Consumer<ICapabilityResolver> capabilityAdder) {
        if (Mekanism.hooks.computerCompatEnabled() && tile.hasComputerSupport()) {
            if (Mekanism.hooks.CCLoaded) {
                //If ComputerCraft is loaded add the capability for it
                capabilityAdder.accept(CCCapabilityHelper.getComputerCraftCapability(tile));
            }
            if (Mekanism.hooks.OC2Loaded) {
                //If OpenComputers2 is loaded add the capability for it
                capabilityAdder.accept(OC2CapabilityHelper.getOpenComputers2Capability(tile));
            }
        }
    }
}