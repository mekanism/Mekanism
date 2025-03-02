package mekanism.generators.common.tile;

import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntitySolarGenerator extends TileEntityGenerator {

    private static final RelativeSide[] ENERGY_SIDES = {RelativeSide.BOTTOM};
    private boolean seesSun;
    private long lastProductionAmount = 0;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;
    @Nullable
    protected SolarCheck solarCheck;

    public TileEntitySolarGenerator(BlockPos pos, BlockState state) {
        this(GeneratorsBlocks.SOLAR_GENERATOR, pos, state, MekanismGeneratorsConfig.generators.solarGeneration);
    }

    protected TileEntitySolarGenerator(Holder<Block> blockProvider, BlockPos pos, BlockState state, @NotNull LongSupplier maxOutput) {
        super(blockProvider, pos, state, maxOutput);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35));
        return builder.build();
    }

    @ComputerMethod
    public boolean canSeeSun() {
        return seesSun;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (solarCheck == null) {
            recheckSettings();
        }
        energySlot.drainContainer();
        // Sort out if the generator can see the sun; we no longer check if it's raining here,
        // since under the new rules, we can still generate power when it's raining, albeit at a
        // significant penalty.
        seesSun = checkCanSeeSun();
        if (seesSun && canFunction() && getEnergyContainer().getNeeded() > 0L) {
            setActive(true);
            long production = getProduction();
            lastProductionAmount = production - getEnergyContainer().insert(production, Action.EXECUTE, AutomationType.INTERNAL);
        } else {
            setActive(false);
            lastProductionAmount = 0L;
        }
        return sendUpdatePacket;
    }

    protected void recheckSettings() {
        if (level == null) {
            return;
        }
        solarCheck = new SolarCheck(level, worldPosition);
        updateMaxOutputRaw(MathUtils.clampToLong(getConfiguredMax() * solarCheck.getPeakMultiplier()));
    }

    protected boolean checkCanSeeSun() {
        if (solarCheck == null) {
            return false;
        }
        solarCheck.recheckCanSeeSun();
        return solarCheck.canSeeSun();
    }

    public long getProduction() {
        if (level == null || solarCheck == null) {
            return 0L;
        }
        float brightness = getBrightnessMultiplier(level);
        //Production is a function of the peak possible output in this biome and sun's current brightness
        return MathUtils.clampToLong(getConfiguredMax() * (brightness * solarCheck.getGenerationMultiplier()));
    }

    protected float getBrightnessMultiplier(@NotNull Level world) {
        //Get the brightness of the sun; note that there are some implementations that depend on the base
        // brightness function which doesn't take into account the fact that rain can't occur in some biomes.
        //TODO: Galacticraft solar energy multiplier (see TileEntitySolarGenerator 1.12 branch).
        // Also do that for the Solar Neutron Activator and Solar Recharging Unit
        return WorldUtils.getSunBrightness(world, 1.0F);
    }

    @Override
    protected RelativeSide[] getEnergySides() {
        return ENERGY_SIDES;
    }

    protected long getConfiguredMax() {
        return MekanismGeneratorsConfig.generators.solarGeneration.get();
    }

    @Override
    public long getProductionRate() {
        return lastProductionAmount;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::canSeeSun, value -> seesSun = value));
        container.track(syncableMaxOutput());
        container.track(SyncableLong.create(this::getProductionRate, value -> lastProductionAmount = value));
    }

    protected static class SolarCheck {

        private final boolean needsRainCheck;
        private final float peakMultiplier;
        protected final BlockPos pos;
        protected final Level world;
        protected boolean canSeeSun;

        public SolarCheck(Level world, BlockPos pos) {
            this.world = world;
            this.pos = pos;
            Biome b = this.world.getBiomeManager().getBiome(this.pos).value();
            needsRainCheck = b.getPrecipitationAt(this.pos) != Precipitation.NONE;
            // Consider the best temperature to be 0.8; biomes that are higher than that
            // will suffer an efficiency loss (semiconductors don't like heat); biomes that are cooler
            // get a boost. We scale the efficiency to around 30% so that it doesn't totally dominate
            float tempEff = 0.3F * (0.8F - b.getTemperature(this.pos));

            // Treat rainfall as a proxy for humidity; any humidity works as a drag on overall efficiency.
            // As with temperature, we scale it so that it doesn't overwhelm production. Note the signedness
            // on the scaling factor. Also note that we only use rainfall as a proxy if it CAN rain; some dimensions
            // (like the End) have rainfall set, but can't actually support rain.
            float humidityEff = needsRainCheck ? -0.3F * b.getModifiedClimateSettings().downfall() : 0;
            peakMultiplier = 1.0F + tempEff + humidityEff;
        }

        public void recheckCanSeeSun() {
            canSeeSun = WorldUtils.canSeeSun(world, pos);
        }

        public boolean canSeeSun() {
            return canSeeSun;
        }

        public float getPeakMultiplier() {
            return peakMultiplier;
        }

        public float getGenerationMultiplier() {
            if (!canSeeSun) {
                return 0;
            }
            if (needsRainCheck && (this.world.isRaining() || this.world.isThundering())) {
                //If the generator is in a biome where it can rain, and it's raining penalize production by 80%
                return peakMultiplier * 0.2F;
            }
            return peakMultiplier;
        }
    }
}
