package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class MachineEnergyContainer<TILE extends TileEntityMekanism> extends BasicEnergyContainer {

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> input(TILE tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, listener);
    }

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> internal(TILE tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), internalOnly, internalOnly, tile, listener);
    }

    public static AttributeEnergy validateBlock(TileEntityMekanism tile) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        return Attribute.getOrThrow(tile.getBlockHolder(), AttributeEnergy.class);
    }

    protected final TILE tile;
    private final int baseEnergyPerTick;
    private long currentMaxEnergy;
    protected int currentEnergyPerTick;

    protected MachineEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          TILE tile, @Nullable IContentsListener listener) {
        this(maxEnergy, energyPerTick, canExtract, canInsert, tile, null, null, listener);
    }

    protected MachineEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          TILE tile, @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener) {
        super(maxEnergy, canExtract, canInsert, insertionRateLimiter, extractionRateLimiter, listener);
        this.baseEnergyPerTick = energyPerTick;
        this.tile = tile;
        currentMaxEnergy = getBaseMaxEnergy();
        currentEnergyPerTick = baseEnergyPerTick;
    }

    public boolean adjustableRates() {
        return false;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
        return Math.max(currentMaxEnergy, getAmountAsLong());
    }

    public long getBaseMaxEnergy() {
        return super.getCapacityAsLong();
    }

    public void setMaxEnergy(long maxEnergy) {
        this.currentMaxEnergy = maxEnergy;
        ContainerType.ENERGY.clampContents(this, null);
    }

    public int getEnergyPerTick() {
        return currentEnergyPerTick;
    }

    public int getBaseEnergyPerTick() {
        return baseEnergyPerTick;
    }

    public void setEnergyPerTick(int energyPerTick) {
        this.currentEnergyPerTick = energyPerTick;
    }

    public void updateMaxEnergy(HolderGetter<Upgrade> upgrades) {
        Reference<Upgrade> speedUpgrade = upgrades.get(UpgradeIds.SPEED).orElse(null);
        Reference<Upgrade> energyUpgrade = upgrades.get(UpgradeIds.ENERGY).orElse(null);
        if (speedUpgrade != null && tile.supportsUpgrade(speedUpgrade)) {
            long bufferMultipler = AttributeEnergy.STORAGE_MULTIPLIER;
            //TODO - 26.2: Take this into account for the item's defined max energy so that it doesn't display 1 kFE / 20 FE for an energized smelter
            if (tile instanceof TileEntityProgressMachine<?> progressMachine) {
                bufferMultipler = Math.max(bufferMultipler, progressMachine.ticksRequired);
            }
            if (tile instanceof TileEntityFactory<?> factory) {
                bufferMultipler *= factory.tier.processes;
            }
            setMaxEnergy(getEnergyPerTick() * bufferMultipler);
        } else if (energyUpgrade != null && tile.supportsUpgrade(energyUpgrade)) {
            setMaxEnergy(UpgradeUtils.getMaxEnergy(tile, getBaseMaxEnergy(), energyUpgrade));
        }
    }

    public void updateEnergyPerTick(HolderGetter<Upgrade> upgrades) {
        if (tile.supportsUpgrades()) {
            Reference<Upgrade> speedUpgrade = upgrades.get(UpgradeIds.SPEED).orElse(null);
            Reference<Upgrade> energyUpgrade = upgrades.get(UpgradeIds.ENERGY).orElse(null);
            if (speedUpgrade == null || energyUpgrade == null) {
                //TODO is this line necessary?
                setEnergyPerTick(getBaseEnergyPerTick());
            } else if (tile.supportsUpgrade(energyUpgrade) || tile.supportsUpgrade(speedUpgrade)) {
                setEnergyPerTick(UpgradeUtils.getEnergyPerTick(tile, getBaseEnergyPerTick(), energyUpgrade, speedUpgrade));
            }
        }
    }
}