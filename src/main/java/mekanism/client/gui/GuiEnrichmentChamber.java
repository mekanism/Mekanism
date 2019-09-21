package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.electric.EnrichmentChamberContainer;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiEnrichmentChamber extends GuiElectricMachine<TileEntityEnrichmentChamber, EnrichmentChamberContainer> {

    public GuiEnrichmentChamber(EnrichmentChamberContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.BLUE;
    }
}