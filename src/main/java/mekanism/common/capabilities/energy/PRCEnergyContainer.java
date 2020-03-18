package mekanism.common.capabilities.energy;

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

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PRCEnergyContainer extends MachineEnergyContainer<TileEntityPressurizedReactionChamber> {

    public static PRCEnergyContainer input(TileEntityPressurizedReactionChamber tile) {
        IBlockElectric electricBlock = validateBlock(tile);
        return new PRCEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile);
    }

    private PRCEnergyContainer(double maxEnergy, double energyPerTick, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          TileEntityPressurizedReactionChamber tile) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile);
    }

    @Override
    public double getBaseEnergyPerTick() {
        CachedRecipe<PressurizedReactionRecipe> recipe = tile.getUpdatedCache(0);
        if (recipe == null) {
            return super.getBaseEnergyPerTick();
        }
        return super.getBaseEnergyPerTick() + recipe.getRecipe().getEnergyRequired();
    }
}