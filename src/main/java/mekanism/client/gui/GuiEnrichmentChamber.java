package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiEnrichmentChamber extends GuiElectricMachine<EnrichmentRecipe> {

    public GuiEnrichmentChamber(InventoryPlayer inventory, TileEntityElectricMachine<EnrichmentRecipe> tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.BLUE;
    }
}