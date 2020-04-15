package mekanism.generators.common.content.fission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.MultiblockHeatCapacitor;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.registries.MekanismGases;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;

public class SynchronizedFissionReactorData extends SynchronizedData<SynchronizedFissionReactorData> implements IMekanismFluidHandler, IMekanismGasHandler, ITileHeatHandler {

    public static final double CASING_HEAT_CAPACITY = 5;

    public static final double INVERSE_INSULATION_COEFFICIENT = 100_000;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 10;

    public static final int WATER_PER_VOLUME = 100_000;
    public static final long STEAM_PER_VOLUME = 1_000_000;

    public List<Coord4D> portLocations = new ArrayList<>();
    public int fuelAssemblies;

    public MultiblockFluidTank<TileEntityFissionReactorCasing> waterTank;
    public MultiblockGasTank<TileEntityFissionReactorCasing> fuelTank;

    public MultiblockGasTank<TileEntityFissionReactorCasing> steamTank;
    public MultiblockGasTank<TileEntityFissionReactorCasing> wasteTank;
    public MultiblockHeatCapacitor<TileEntityFissionReactorCasing> heatCapacitor;

    private List<IExtendedFluidTank> fluidTanks;
    private List<IChemicalTank<Gas, GasStack>> gasTanks;
    private List<IHeatCapacitor> heatCapacitors;

    public SynchronizedFissionReactorData(TileEntityFissionReactorCasing tile) {
        waterTank = MultiblockFluidTank.input(tile, () -> tile.structure == null ? 0 : getVolume() * WATER_PER_VOLUME, fluid -> fluid.getFluid().isIn(FluidTags.WATER));
        fluidTanks = Collections.singletonList(waterTank);
        fuelTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : getVolume() * STEAM_PER_VOLUME,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL, (stack, automationType) -> tile.structure != null,
            gas -> gas == MekanismGases.FISSILE_FUEL.getGas(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        steamTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : getVolume() * STEAM_PER_VOLUME,
            (stack, automationType) -> tile.structure != null, (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.STEAM.getGas());
        wasteTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : getVolume() * STEAM_PER_VOLUME,
            (stack, automationType) -> tile.structure != null, (stack, automationType) -> automationType != AutomationType.EXTERNAL,
            gas -> gas == MekanismGases.NUCLEAR_WASTE.getGas(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        gasTanks = Arrays.asList(fuelTank, steamTank, wasteTank);
        heatCapacitor = MultiblockHeatCapacitor.create(tile,
            CASING_HEAT_CAPACITY,
            () -> INVERSE_INSULATION_COEFFICIENT,
            () -> INVERSE_INSULATION_COEFFICIENT);
        heatCapacitors = Collections.singletonList(heatCapacitor);
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return heatCapacitors;
    }
}
