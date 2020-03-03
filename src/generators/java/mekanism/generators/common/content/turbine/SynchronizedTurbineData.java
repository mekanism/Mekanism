package mekanism.generators.common.content.turbine;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedTurbineData extends SynchronizedData<SynchronizedTurbineData> {

    public static final float ROTATION_THRESHOLD = 0.001F;
    public static Object2FloatMap<String> clientRotationMap = new Object2FloatOpenHashMap<>();

    public MultiblockFluidTank<TileEntityTurbineCasing> fluidTank;
    public List<IExtendedFluidTank> fluidTanks;

    public IExtendedFluidTank ventTank;
    public List<IExtendedFluidTank> ventTanks;

    @Nonnull
    public FluidStack prevFluid = FluidStack.EMPTY;

    public double electricityStored;

    public GasMode dumpMode = GasMode.IDLE;

    public int blades;
    public int vents;
    public int coils;
    public int condensers;

    public int lowerVolume;

    public Coord4D complex;

    public int lastSteamInput;
    public int newSteamInput;

    public int clientDispersers;
    public int clientFlow;
    public float clientRotation;

    public SynchronizedTurbineData(TileEntityTurbineCasing tile) {
        fluidTanks = Collections.singletonList(fluidTank = new TurbineFluidTank(tile));
        ventTank = VariableCapacityFluidTank.create(() -> tile.structure == null ? 1_000 : tile.structure.condensers * MekanismGeneratorsConfig.generators.condenserRate.get(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || tile.structure != null, BasicFluidTank.internalOnly,
              fluid -> fluid.getFluid().isIn(FluidTags.WATER), null);
        ventTanks = Collections.singletonList(ventTank);
    }

    public int getDispersers() {
        return (volLength - 2) * (volWidth - 2) - 1;
    }

    public int getFluidCapacity() {
        return lowerVolume * TurbineUpdateProtocol.FLUID_PER_TANK;
    }

    public double getEnergyCapacity() {
        return volume * 16_000_000D; //16 MJ energy capacity per volume
    }

    public boolean needsRenderUpdate() {
        if ((fluidTank.isEmpty() && !prevFluid.isEmpty()) || (!fluidTank.isEmpty() && prevFluid.isEmpty())) {
            return true;
        }
        return !fluidTank.isEmpty() && (!fluidTank.isFluidEqual(prevFluid) || fluidTank.getFluidAmount() != prevFluid.getAmount());
    }
}