package mekanism.common.capabilities.energy;

import java.util.Objects;
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
        return new MachineEnergyContainer<>(tile, baseEnergyCalculator, listener);
    }

    private final IntObjectToIntFunction<TILE> baseEnergyCalculator;
    protected final AttributeEnergy attributeEnergy;
    protected final TILE tile;
    private long currentMaxEnergy;
    private int currentEnergyPerTick;

    @SuppressWarnings("unchecked")
    protected MachineEnergyContainer(TILE tile, @Nullable IContentsListener listener) {
        this(tile, (IntObjectToIntFunction<TILE>) NO_MODIFICATION, listener);
    }

    protected MachineEnergyContainer(TILE tile, IntObjectToIntFunction<TILE> baseEnergyCalculator, @Nullable IContentsListener listener) {
        this(tile, baseEnergyCalculator, null, null, listener);
    }

    protected MachineEnergyContainer(TILE tile, IntObjectToIntFunction<TILE> baseEnergyCalculator, @Nullable RateLimitTracker insertionRateLimiter,
          @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener) {
        this.baseEnergyCalculator = baseEnergyCalculator;
        this.tile = Objects.requireNonNull(tile, "Tile cannot be null");
        AttributeEnergy electricBlock = Attribute.getOrThrow(tile.getBlockHolder(), AttributeEnergy.class);
        this.attributeEnergy = electricBlock;
        super(electricBlock.getStorage(), notExternal, ConstantPredicates.alwaysTrue(), insertionRateLimiter, extractionRateLimiter, listener);
        currentEnergyPerTick = getBaseEnergyPerTick();
        currentMaxEnergy = currentEnergyPerTick * calculateBufferMultiplier();
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

    private int getBaseEnergyPerTick() {
        return baseEnergyCalculator.applyAsInt(attributeEnergy.getUsage(), tile);
    }

    public final void setEnergyPerTick(int energyPerTick) {
        this.currentEnergyPerTick = energyPerTick;
    }

    protected long calculateBufferMultiplier() {
        int ticksRequired = tile instanceof IMultiTickProcessing multiTickProcessing ? multiTickProcessing.getTicksRequired() : attributeEnergy.getBaseTicksRequired();
        long bufferMultiplier = attributeEnergy.getMultiplier(ticksRequired);
        if (tile instanceof IMultiTickProcessing multiTickProcessing) {
            int operationsPerTick = multiTickProcessing.getOperationsPerTick();
            if (operationsPerTick > 1) {
                //If the upgrades are such that it performs more than one processing operation in a single tick, we need to account for it with the buffer
                //TODO: Should we instead be making it so *everything* including smelters and the like consume energy similarly to exponential machines?
                bufferMultiplier *= operationsPerTick;
            }
        }
        return bufferMultiplier;
    }

    public final void updateEnergy(HolderGetter<Upgrade> upgrades) {
        Reference<Upgrade> speedUpgrade = upgrades.get(UpgradeIds.SPEED).orElse(null);
        Reference<Upgrade> energyUpgrade = upgrades.get(UpgradeIds.ENERGY).orElse(null);
        setEnergyPerTick(calculateEnergyPerTick(getBaseEnergyPerTick(), speedUpgrade, energyUpgrade));
        setMaxEnergy(calculateMaxEnergy(getEnergyPerTick(), calculateBufferMultiplier(), speedUpgrade, energyUpgrade));
    }

    protected long calculateMaxEnergy(int energyPerTick, long bufferMultiplier, @Nullable Reference<Upgrade> speedUpgrade, @Nullable Reference<Upgrade> energyUpgrade) {
        return energyPerTick * bufferMultiplier;
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