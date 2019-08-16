package mekanism.client.gui.filter;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilter<TILE extends TileEntityMekanism> extends GuiMekanismTile<TILE, FilterContainer<TILE>> {

    //TODO: Switch to using this
    protected GuiFilter(FilterContainer<TILE> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Deprecated
    protected GuiFilter(PlayerEntity player, TILE tile) {
        super(tile, new FilterContainer(player.inventory, tile), player.inventory);
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