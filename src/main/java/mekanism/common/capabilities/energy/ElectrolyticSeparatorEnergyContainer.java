package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.TileEntityElectrolyticSeparator;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectrolyticSeparatorEnergyContainer extends MachineEnergyContainer<TileEntityElectrolyticSeparator> {

    public static ElectrolyticSeparatorEnergyContainer input(TileEntityElectrolyticSeparator tile) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ElectrolyticSeparatorEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile);
    }

    private ElectrolyticSeparatorEnergyContainer(double maxEnergy, double energyPerTick, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          TileEntityElectrolyticSeparator tile) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile);
    }

    @Override
    public double getBaseEnergyPerTick() {
        CachedRecipe<ElectrolysisRecipe> recipe = tile.getUpdatedCache(0);
        if (recipe == null) {
            return super.getBaseEnergyPerTick();
        }
        return super.getBaseEnergyPerTick() * recipe.getRecipe().getEnergyMultiplier();
    }

    @Override
    public void updateEnergyPerTick() {
        //Update our energy per tick to be based off of our recipe
        this.currentEnergyPerTick = getBaseEnergyPerTick();
        //TODO: Test that separator actually uses the correct amount of power
    }
}