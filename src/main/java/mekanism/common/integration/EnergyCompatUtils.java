package mekanism.common.integration;

import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.forgeenergy.ForgeStrictEnergyHandler;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public abstract class EnergyCompatUtils {

    @Nullable
    public static IStrictEnergyHandler get(TileEntity tile, Direction side) {
        if (tile == null || tile.getWorld() == null) {
            return null;
        }
        Optional<IStrictEnergyHandler> energyAcceptorCap = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.STRICT_ENERGY_CAPABILITY, side));
        if (energyAcceptorCap.isPresent()) {
            return energyAcceptorCap.get();
        }
        if (MekanismUtils.useForge()) {
            Optional<IEnergyStorage> forgeEnergyCap = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityEnergy.ENERGY, side));
            if (forgeEnergyCap.isPresent()) {
                return new ForgeStrictEnergyHandler(forgeEnergyCap.get());
            }
        }
        return null;
    }
}