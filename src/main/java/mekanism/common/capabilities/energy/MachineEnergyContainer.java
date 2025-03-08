package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
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
        AttributeEnergy attributeEnergy = Attribute.get(tile.getBlockHolder(), AttributeEnergy.class);
        if (attributeEnergy == null) {
            throw new IllegalArgumentException("Block provider must be an electric block");
        }
        return attributeEnergy;
    }

    protected final TILE tile;
    private final long baseEnergyPerTick;
    private long currentMaxEnergy;
    protected long currentEnergyPerTick;

    protected MachineEnergyContainer(long maxEnergy, long energyPerTick, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, TILE tile, @Nullable IContentsListener listener) {
        super(maxEnergy, canExtract, canInsert, listener);
        this.baseEnergyPerTick = energyPerTick;
        this.tile = tile;
        currentMaxEnergy = getBaseMaxEnergy();
        currentEnergyPerTick = baseEnergyPerTick;
    }

    public boolean adjustableRates() {
        return false;
    }

    @Override
    public long getMaxEnergy() {
        return currentMaxEnergy;
    }

    public long getBaseMaxEnergy() {
        return super.getMaxEnergy();
    }

    public void setMaxEnergy(long maxEnergy) {
        this.currentMaxEnergy = maxEnergy;
        //Clamp the energy
        setEnergy(getEnergy());
    }

    public long getEnergyPerTick() {
        return currentEnergyPerTick;
    }

    public long getBaseEnergyPerTick() {
        return baseEnergyPerTick;
    }

    public void setEnergyPerTick(long energyPerTick) {
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