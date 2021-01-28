package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;

public class TileEntitySolarGenerator extends TileEntityGenerator {

    private static final FloatingLong RAIN_MULTIPLIER = FloatingLong.createConst(0.2);
    private boolean seesSun;
    private boolean needsRainCheck = true;
    private FloatingLong peakOutput = FloatingLong.ZERO;
    private boolean settingsChecked;
    private FloatingLong lastProductionAmount = FloatingLong.ZERO;

    private EnergyInventorySlot energySlot;

    public TileEntitySolarGenerator() {
        this(GeneratorsBlocks.SOLAR_GENERATOR, MekanismGeneratorsConfig.generators.solarGeneration.get().multiply(2));
    }

    public TileEntitySolarGenerator(IBlockProvider blockProvider, @Nonnull FloatingLong output) {
        super(blockProvider, output);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), this, 143, 35));
        return builder.build();
    }

    public boolean canSeeSun() {
        return seesSun;
    }

    protected void recheckSettings() {
        World world = getWorld();
        if (world == null) {
            return;
        }
        Biome b = world.getBiomeManager().getBiome(getPos());
        needsRainCheck = b.getPrecipitation() != RainType.NONE;
        // Consider the best temperature to be 0.8; biomes that are higher than that
        // will suffer an efficiency loss (semiconductors don't like heat); biomes that are cooler
        // get a boost. We scale the efficiency to around 30% so that it doesn't totally dominate
        float tempEff = 0.3F * (0.8F - b.getTemperature(getPos()));

        // Treat rainfall as a proxy for humidity; any humidity works as a drag on overall efficiency.
        // As with temperature, we scale it so that it doesn't overwhelm production. Note the signedness
        // on the scaling factor. Also note that we only use rainfall as a proxy if it CAN rain; some dimensions
        // (like the End) have rainfall set, but can't actually support rain.
        float humidityEff = -0.3F * (needsRainCheck ? b.getDownfall() : 0.0F);
        peakOutput = getConfiguredMax().multiply(1.0F + tempEff + humidityEff);
        settingsChecked = true;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (!settingsChecked) {
            recheckSettings();
        }
        energySlot.drainContainer();
        // Sort out if the generator can see the sun; we no longer check if it's raining here,
        // since under the new rules, we can still generate power when it's raining, albeit at a
        // significant penalty.
        seesSun = WorldUtils.canSeeSun(world, getSkyCheckPos());
        if (seesSun && MekanismUtils.canFunction(this) && !getEnergyContainer().getNeeded().isZero()) {
            setActive(true);
            FloatingLong production = getProduction();
            lastProductionAmount = production.subtract(getEnergyContainer().insert(production, Action.EXECUTE, AutomationType.INTERNAL));
        } else {
            setActive(false);
            lastProductionAmount = FloatingLong.ZERO;
        }
    }

    protected BlockPos getSkyCheckPos() {
        return pos;
    }

    public FloatingLong getProduction() {
        World world = getWorld();
        if (world == null) {
            return FloatingLong.ZERO;
        }
        //Get the brightness of the sun; note that there are some implementations that depend on the base
        // brightness function which doesn't take into account the fact that rain can't occur in some biomes.
        float brightness = WorldUtils.getSunBrightness(world, 1.0F);
        //TODO: Galacticraft solar energy multiplier (see TileEntitySolarGenerator 1.12 branch). Also do that for the Solar Neutron Activator

        //Production is a function of the peak possible output in this biome and sun's current brightness
        FloatingLong production = peakOutput.multiply(brightness);
        //If the generator is in a biome where it can rain and it's raining penalize production by 80%
        if (needsRainCheck && (world.isRaining() || world.isThundering())) {
            production = production.timesEqual(RAIN_MULTIPLIER);
        }
        return production;
    }

    @Override
    protected RelativeSide[] getEnergySides() {
        return new RelativeSide[]{RelativeSide.BOTTOM};
    }

    protected FloatingLong getConfiguredMax() {
        return MekanismGeneratorsConfig.generators.solarGeneration.get();
    }

    @Override
    public boolean lightUpdate() {
        return false;
    }

    @Override
    public FloatingLong getMaxOutput() {
        return peakOutput;
    }

    public FloatingLong getLastProductionAmount() {
        return lastProductionAmount;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::canSeeSun, value -> seesSun = value));
        container.track(SyncableFloatingLong.create(this::getMaxOutput, value -> peakOutput = value));
        container.track(SyncableFloatingLong.create(this::getLastProductionAmount, value -> lastProductionAmount = value));
    }
}