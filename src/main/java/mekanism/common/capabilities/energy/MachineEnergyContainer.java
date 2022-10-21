package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MachineEnergyContainer<TILE extends TileEntityMekanism> extends BasicEnergyContainer {

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> input(TILE tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile, listener);
    }

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> internal(TILE tile, @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), internalOnly, internalOnly, tile, listener);
    }

    public static AttributeEnergy validateBlock(TileEntityMekanism tile) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Block block = tile.getBlockType();
        if (!Attribute.has(block, AttributeEnergy.class)) {
            throw new IllegalArgumentException("Block provider must be an electric block");
        }
        return Attribute.get(block, AttributeEnergy.class);
    }

    protected final TILE tile;
    private final FloatingLong baseEnergyPerTick;
    private FloatingLong currentMaxEnergy;
    protected FloatingLong currentEnergyPerTick;

    protected MachineEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, TILE tile, @Nullable IContentsListener listener) {
        super(maxEnergy, canExtract, canInsert, listener);
        this.baseEnergyPerTick = energyPerTick.copyAsConst();
        this.tile = tile;
        currentMaxEnergy = getBaseMaxEnergy();
        currentEnergyPerTick = baseEnergyPerTick;
    }

    public boolean adjustableRates() {
        return false;
    }

    @Override
    public FloatingLong getMaxEnergy() {
        return currentMaxEnergy;
    }

    public FloatingLong getBaseMaxEnergy() {
        return super.getMaxEnergy();
    }

    public void setMaxEnergy(FloatingLong maxEnergy) {
        Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
        this.currentMaxEnergy = maxEnergy;
        if (getEnergy().greaterThan(getMaxEnergy())) {
            setEnergy(getMaxEnergy());
        }
    }

    public FloatingLong getEnergyPerTick() {
        return currentEnergyPerTick;
    }

    public FloatingLong getBaseEnergyPerTick() {
        return baseEnergyPerTick;
    }

    public void setEnergyPerTick(FloatingLong energyPerTick) {
        Objects.requireNonNull(energyPerTick, "Energy per tick cannot be null");
        this.currentEnergyPerTick = energyPerTick;
    }

    public void updateMaxEnergy() {
        if (tile.supportsUpgrade(Upgrade.ENERGY)) {
            setMaxEnergy(MekanismUtils.getMaxEnergy(tile, getBaseMaxEnergy()));
        }
    }

    public void updateEnergyPerTick() {
        if (tile.supportsUpgrades()) {
            TileComponentUpgrade upgradeComponent = tile.getComponent();
            if (upgradeComponent.supports(Upgrade.ENERGY) || upgradeComponent.supports(Upgrade.SPEED)) {
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(tile, getBaseEnergyPerTick()));
            }
        }
    }
}