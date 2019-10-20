package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.OsmiumCompressorContainer;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiOsmiumCompressor extends GuiAdvancedElectricMachine<TileEntityOsmiumCompressor, OsmiumCompressorContainer> {

    public GuiOsmiumCompressor(OsmiumCompressorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.RED;
    }
}