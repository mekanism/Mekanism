package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiOsmiumCompressor extends GuiAdvancedElectricMachine<OsmiumCompressorRecipe> {

    public GuiOsmiumCompressor(InventoryPlayer inventory, TileEntityAdvancedElectricMachine<OsmiumCompressorRecipe> tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.RED;
    }
}