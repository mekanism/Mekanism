package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.tile.prefab.TileEntityChanceMachine;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPrecisionSawmill extends GuiChanceMachine<SawmillRecipe> {

    public GuiPrecisionSawmill(PlayerInventory inventory, TileEntityChanceMachine<SawmillRecipe> tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.PURPLE;
    }
}