package mekanism.common.integration.computer;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.function.Consumer;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.integration.computer.computercraft.CCEnergyHelper;
import mekanism.common.integration.computer.computercraft.MekanismPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ComputerCapabilityHelper {

    @CapabilityInject(IPeripheral.class)
    public static Capability<IPeripheral> COMPUTER_CRAFT_CAPABILITY;

    public static <TILE extends TileEntity & IComputerTile> void addComputerCapabilities(TILE tile, Consumer<ICapabilityResolver> capabilityAdder) {
        if (tile.hasComputerSupport()) {
            if (Mekanism.hooks.CCLoaded) {
                //If ComputerCraft is loaded add the capability for it
                capabilityAdder.accept(ComputerCapabilityHelper.getComputerCraftCapability(tile));
            }
            //If OpenComputers loaded, add capability for it
        }
    }

    private static <TILE extends TileEntity & IComputerTile> ICapabilityResolver getComputerCraftCapability(TILE tile) {
        if (tile.isComputerCapabilityPersistent()) {
            return BasicCapabilityResolver.persistent(COMPUTER_CRAFT_CAPABILITY, () -> MekanismPeripheral.create(tile));
        }
        return BasicCapabilityResolver.create(COMPUTER_CRAFT_CAPABILITY, () -> MekanismPeripheral.create(tile));
    }

    //Only call this if ComputerCraft is loaded
    public static void registerCCMathHelper() {
        ComputerCraftAPI.registerAPIFactory(CCEnergyHelper::create);
    }
}