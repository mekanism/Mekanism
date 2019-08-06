package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiEnergizedSmelter extends GuiElectricMachine<SmeltingRecipe> {

    public GuiEnergizedSmelter(InventoryPlayer inventory, TileEntityElectricMachine<SmeltingRecipe> tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.GREEN;
    }
}