package mekanism.client.gui;

import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.tile.prefab.TileEntityChanceMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPrecisionSawmill extends GuiChanceMachine<SawmillRecipe> {

    public GuiPrecisionSawmill(InventoryPlayer inventory, TileEntityChanceMachine<SawmillRecipe> tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.PURPLE;
    }
}