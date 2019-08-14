package mekanism.client.gui.filter;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilter<TILE extends TileEntityMekanism> extends GuiMekanismTile<TILE, ContainerFilter> {

    protected GuiFilter(PlayerEntity player, TILE tile) {
        super(tile, new ContainerFilter(player.inventory, tile), player.inventory);
    }

    protected abstract void addButtons();

    protected abstract void sendPacketToServer(int guiID);

    @Override
    public void init() {
        super.init();
        buttons.clear();
        addButtons();
    }
}