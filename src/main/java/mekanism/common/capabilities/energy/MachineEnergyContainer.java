package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Upgrade;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineEnergyContainer<TILE extends TileEntityMekanism> extends BasicEnergyContainer {

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> input(TILE tile) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile);
    }

    public static <TILE extends TileEntityMekanism> MachineEnergyContainer<TILE> internal(TILE tile) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MachineEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), internalOnly, internalOnly, tile);
    }

    public static AttributeEnergy validateBlock(TileEntityMekanism tile) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        Block block = tile.getBlockType().getBlock();
        if (!Attribute.has(block, AttributeEnergy.class)) {
            throw new IllegalArgumentException("Block provider must be an electric block");
        }
        return Attribute.get(block, AttributeEnergy.class);
    }

    protected final TILE tile;
    private final double baseEnergyPerTick;
    private double currentMaxEnergy;
    protected double currentEnergyPerTick;

    protected MachineEnergyContainer(double maxEnergy, double energyPerTick, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          TILE tile) {
        super(maxEnergy, canExtract, canInsert, tile);
        this.baseEnergyPerTick = energyPerTick;
        this.tile = tile;
        currentMaxEnergy = maxEnergy;
        currentEnergyPerTick = energyPerTick;
        //TODO: Evaluate if we need to set this anywhere, doing so here is too early
        //Make sure we have the proper max energy set initially
        //updateMaxEnergy();
        //updateEnergyPerTick();
    }

    public boolean adjustableRates() {
        return false;
    }

    @Override
    public double getMaxEnergy() {
        return currentMaxEnergy;
    }

    public double getBaseMaxEnergy() {
        return super.getMaxEnergy();
    }

    public void setMaxEnergy(double maxEnergy) {
        if (maxEnergy < 0) {
            throw new IllegalArgumentException("Max energy must be at least zero");
        }
        this.currentMaxEnergy = maxEnergy;
        if (getEnergy() > getMaxEnergy()) {
            setEnergy(getMaxEnergy());
        }
    }

    public double getEnergyPerTick() {
        return currentEnergyPerTick;
    }

    public double getBaseEnergyPerTick() {
        return baseEnergyPerTick;
    }

    public void setEnergyPerTick(double energyPerTick) {
        if (energyPerTick < 0) {
            throw new IllegalArgumentException("Energy must be at least zero");
        }
        this.currentEnergyPerTick = energyPerTick;
    }

    public void updateMaxEnergy() {
        if (tile.supportsUpgrades() && tile.getSupportedUpgrade().contains(Upgrade.ENERGY)) {
            setMaxEnergy(MekanismUtils.getMaxEnergy(tile, getBaseMaxEnergy()));
        }
    }

    public void updateEnergyPerTick() {
        if (tile.supportsUpgrades()) {
            Set<Upgrade> supportedUpgrades = tile.getSupportedUpgrade();
            if (supportedUpgrades.contains(Upgrade.ENERGY) || supportedUpgrades.contains(Upgrade.SPEED)) {
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(tile, baseEnergyPerTick));
            }
        }
    }
}