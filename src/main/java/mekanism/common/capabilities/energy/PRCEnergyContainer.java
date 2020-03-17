package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.inventory.AutomationType;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import net.minecraft.block.Block;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PRCEnergyContainer extends MachineEnergyContainer {

    public static PRCEnergyContainer input(TileEntityPressurizedReactionChamber tile) {
        Block block = tile.getBlockType().getBlock();
        if (!(block instanceof IBlockElectric)) {
            throw new IllegalArgumentException("Block provider must be an electric block");
        }
        Objects.requireNonNull(tile, "Tile cannot be null");
        IBlockElectric electricBlock = (IBlockElectric) block;
        return new PRCEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile);
    }

    private final TileEntityPressurizedReactionChamber tile;

    protected PRCEnergyContainer(double maxEnergy, double energyPerTick, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          TileEntityPressurizedReactionChamber tile) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile);
        this.tile = tile;
    }

    @Override
    public double getBaseEnergyPerTick() {
        CachedRecipe<PressurizedReactionRecipe> recipe = tile.getUpdatedCache(0);
        double extra = recipe == null ? 0 : recipe.getRecipe().getEnergyRequired();
        return super.getBaseEnergyPerTick() + extra;
    }
}