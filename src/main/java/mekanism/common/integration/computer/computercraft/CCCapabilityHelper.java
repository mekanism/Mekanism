package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CCCapabilityHelper {

    private static final Capability<IPeripheral> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static <TILE extends BlockEntity & IComputerTile> ICapabilityResolver getComputerCraftCapability(TILE tile) {
        if (tile.isComputerCapabilityPersistent()) {
            return BasicCapabilityResolver.persistent(CAPABILITY, () -> MekanismPeripheral.create(tile));
        }
        return BasicCapabilityResolver.create(CAPABILITY, () -> MekanismPeripheral.create(tile));
    }

    public static void registerCCMathHelper() {
        ComputerCraftAPI.registerAPIFactory(CCEnergyHelper::create);
    }
}