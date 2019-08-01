package mekanism.common.block.states;

import java.util.Arrays;
import java.util.List;
import mekanism.common.base.IBlockType;
import mekanism.common.config.MekanismConfig;
import net.minecraft.item.ItemStack;

public enum MachineType implements IBlockType {
    ENRICHMENT_CHAMBER,
    OSMIUM_COMPRESSOR,
    COMBINER,
    CRUSHER,
    DIGITAL_MINER,
    BASIC_FACTORY,
    ADVANCED_FACTORY,
    ELITE_FACTORY,
    METALLURGIC_INFUSER,
    PURIFICATION_CHAMBER,
    ENERGIZED_SMELTER,
    TELEPORTER,
    ELECTRIC_PUMP,
    PERSONAL_CHEST,
    CHARGEPAD,
    LOGISTICAL_SORTER,
    ROTARY_CONDENSENTRATOR,
    CHEMICAL_OXIDIZER,
    CHEMICAL_INFUSER,
    CHEMICAL_INJECTION_CHAMBER,
    ELECTROLYTIC_SEPARATOR,
    PRECISION_SAWMILL,
    CHEMICAL_DISSOLUTION_CHAMBER,
    CHEMICAL_WASHER,
    CHEMICAL_CRYSTALLIZER,
    SEISMIC_VIBRATOR,
    PRESSURIZED_REACTION_CHAMBER,
    FLUID_TANK,
    FLUIDIC_PLENISHER,
    LASER,
    LASER_AMPLIFIER,
    LASER_TRACTOR_BEAM,
    QUANTUM_ENTANGLOPORTER,
    SOLAR_NEUTRON_ACTIVATOR,
    OREDICTIONIFICATOR,
    RESISTIVE_HEATER,
    FORMULAIC_ASSEMBLICATOR,
    FUELWOOD_HEATER;

    public static List<MachineType> getValidMachines() {
        return Arrays.asList(values());
    }

    public static MachineType get(ItemStack stack) {
        return null;
    }

    @Override
    public String getBlockName() {
        return "";
    }

    @Override
    public boolean isEnabled() {
        //TODO: Replace with IBlockDisableable
        return MekanismConfig.current().general.machinesManager.isEnabled(this);
    }

    public double getUsage() {
        //TODO: Use IBlockElectric instead
        return 0;
    }

    public double getStorage() {
        //TODO: Use IBlockElectric instead
        return 0;
    }

    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }
}