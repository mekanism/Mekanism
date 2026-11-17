package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.functions.IntObjectToIntFunction;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IMultiTickProcessing;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class MachineEnergyContainer<TILE extends TileEntityMekanism> extends BasicEnergyContainer {

    private static final IntObjectToIntFunction<?> NO_MODIFICATION = (baseEnergy, _) -> baseEnergy;

    @SuppressWarnings("unchecked")
    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> input(TILE tile, @Nullable IContentsListener listener) {
        return input(tile, (IntObjectToIntFunction<TILE>) NO_MODIFICATION, listener);
    }

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> input(TILE tile, IntObjectToIntFunction<TILE> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, baseEnergyCalculator, listener);
    }

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> internal(TILE tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), internalOnly, internalOnly, tile, listener);
    }

    public static AttributeEnergy validateBlock(TileEntityMekanism tile) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        return Attribute.getOrThrow(tile.getBlockHolder(), AttributeEnergy.class);
    }

    private final IntObjectToIntFunction<TILE> baseEnergyCalculator;
    protected final TILE tile;
    private final int baseEnergyPerTick;
    private long currentMaxEnergy;
    private int currentEnergyPerTick;

    @SuppressWarnings("unchecked")
    protected MachineEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          TILE tile, @Nullable IContentsListener listener) {
        this(maxEnergy, energyPerTick, canExtract, canInsert, tile, (IntObjectToIntFunction<TILE>) NO_MODIFICATION, listener);
    }

    protected MachineEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          TILE tile, IntObjectToIntFunction<TILE> baseEnergyCalculator, @Nullable IContentsListener listener) {
        this(maxEnergy, energyPerTick, canExtract, canInsert, tile, baseEnergyCalculator, null, null, listener);
    }

    protected MachineEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          TILE tile, IntObjectToIntFunction<TILE> baseEnergyCalculator, @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter,
          @Nullable IContentsListener listener) {
        this.baseEnergyCalculator = baseEnergyCalculator;
        this.tile = tile;
        this.baseEnergyPerTick = energyPerTick;
        super(maxEnergy, canExtract, canInsert, insertionRateLimiter, extractionRateLimiter, listener);
        currentMaxEnergy = maxEnergy;
        currentEnergyPerTick = baseEnergyPerTick;
    }

    public boolean adjustableRates() {
        return false;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public final long getCapacityAsLong() {
        return currentMaxEnergy;
    }

    public final void setMaxEnergy(long maxEnergy) {
        this.currentMaxEnergy = maxEnergy;
        ContainerType.ENERGY.clampContents(this, null);
    }

    public final int getEnergyPerTick() {
        return currentEnergyPerTick;
    }

    protected final int getBaseEnergyPerTick() {
        return baseEnergyCalculator.applyAsInt(baseEnergyPerTick, tile);
    }

    protected final long getBaseMaxEnergy() {
        return super.getCapacityAsLong();
    }

    public final void setEnergyPerTick(int energyPerTick) {
        this.currentEnergyPerTick = energyPerTick;
    }

    //TODO - 26.3: Take this into account for the item's defined max energy so that it doesn't display 1 kFE / 20 FE for an energized smelter
    // Maybe move it to the attribute or just store a temporary component of what the last multiplier was
    protected long calculateBufferMultiplier() {
        long bufferMultipler = AttributeEnergy.STORAGE_MULTIPLIER;
        if (tile instanceof IMultiTickProcessing multiTickProcessing) {
            bufferMultipler = Math.max(bufferMultipler, multiTickProcessing.getTicksRequired());
            int operationsPerTick = multiTickProcessing.getOperationsPerTick();
            if (operationsPerTick > 1) {
                //If the upgrades are such that it performs more than one processing operation in a single tick, we need to account for it with the buffer
                //TODO: Should we instead be making it so *everything* including smelters and the like consume energy similarly to exponential machines?
                bufferMultipler *= operationsPerTick;
            }
        }
        if (tile instanceof TileEntityFactory<?> factory) {
            bufferMultipler *= factory.tier.processes;
        }
        return bufferMultipler;
    }

    public void updateEnergy(HolderGetter<Upgrade> upgrades) {
        Reference<Upgrade> speedUpgrade = upgrades.get(UpgradeIds.SPEED).orElse(null);
        Reference<Upgrade> energyUpgrade = upgrades.get(UpgradeIds.ENERGY).orElse(null);
        setEnergyPerTick(calculateEnergyPerTick(getBaseEnergyPerTick(), speedUpgrade, energyUpgrade));
        setMaxEnergy(getEnergyPerTick() * calculateBufferMultiplier());
    }

    protected int calculateEnergyPerTick(int baseEnergyPerTick, @Nullable Reference<Upgrade> speedUpgrade, @Nullable Reference<Upgrade> energyUpgrade) {
        if (tile.supportsUpgrade(speedUpgrade)) {
            double fractionSpeedUpgrades = UpgradeUtils.fractionUpgrades(tile, speedUpgrade);
            double fractionEnergyUpgrades = tile.supportsUpgrade(energyUpgrade) ? UpgradeUtils.fractionUpgrades(tile, energyUpgrade) : 0;
            int energyPerTick = Mth.ceil(baseEnergyPerTick * Math.pow(
                  MekanismConfig.general.maxUpgradeMultiplier.get(),
                  2 * fractionSpeedUpgrades - fractionEnergyUpgrades
            ));
            if (fractionSpeedUpgrades > 0 && tile.upgradeInfoIsExponential(speedUpgrade)) {
                energyPerTick += energyPerTick * tile.getUpgrades(speedUpgrade);
            }
            return energyPerTick;
        }
        return baseEnergyPerTick;
    }
}