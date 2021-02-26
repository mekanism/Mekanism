package mekanism.common.integration.computer;

import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.integration.computer.computercraft.MekanismPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ComputerCapabilityHelper {

    @CapabilityInject(IPeripheral.class)
    public static Capability<IPeripheral> COMPUTER_CRAFT_CAPABILITY;

    public static <TILE extends TileEntity & IComputerTile> ICapabilityResolver getComputerCraftCapability(TILE tile) {
        if (!tile.hasComputerSupport()) {
            throw new IllegalArgumentException("Tile does not support computers.");
        }
        if (tile.isComputerCapabilityPersistent()) {
            return BasicCapabilityResolver.persistent(COMPUTER_CRAFT_CAPABILITY, () -> new MekanismPeripheral<>(tile));
        }
        return BasicCapabilityResolver.create(COMPUTER_CRAFT_CAPABILITY, () -> new MekanismPeripheral<>(tile));
    }
}