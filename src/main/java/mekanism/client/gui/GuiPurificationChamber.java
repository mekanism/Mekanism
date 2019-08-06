package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPurificationChamber extends GuiAdvancedElectricMachine<PurificationRecipe> {

    public GuiPurificationChamber(InventoryPlayer inventory, TileEntityAdvancedElectricMachine<PurificationRecipe> tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.RED;
    }
}