package mekanism.common.util;

import javax.annotation.Nonnull;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public final class CapabilityUtils {

    public static boolean hasCapability(ICapabilityProvider provider, Capability<?> cap, EnumFacing side) {
        if (provider == null || cap == null) {
            return false;
        }

        return provider.hasCapability(cap, side);
    }

    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap, EnumFacing side) {
        if (provider == null || cap == null) {
            return null;
        }

        return provider.getCapability(cap, side);
    }

    public static <T extends TileEntityContainerBlock & ISideConfiguration> boolean isCapabilityDisabled(
          @Nonnull Capability<?> capability, EnumFacing side, T tile) {
        TransmissionType type = null;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            type = TransmissionType.ITEM;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            type = TransmissionType.GAS;
        } else if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            type = TransmissionType.HEAT;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            type = TransmissionType.FLUID;
        }
        //Energy is handled by the TileEntityElectricBlock anyways in the super clauses so no need to bother with it
        if (type != null) {
            TileComponentConfig config = tile.getConfig();
            return config != null && config.supports(type) && config.hasSideForData(type, tile.facing, 0, side);
        }
        return false;
    }
}
