package mekanism.client.gui.filter;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Container;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilter<TILE extends TileEntityMekanism> extends GuiMekanismTile<TILE> {

    protected GuiFilter(TILE tile, Container container) {
        super(tile, container);
    }

    protected GuiFilter(PlayerEntity player, TILE tile) {
        super(tile, new ContainerFilter(player.inventory, tile));
    }

    protected abstract void addButtons();

    protected abstract void sendPacketToServer(int guiID);

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        addButtons();
    }
}