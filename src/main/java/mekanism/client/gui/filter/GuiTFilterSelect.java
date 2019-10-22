package mekanism.client.gui.filter;

import mekanism.common.inventory.container.tile.filter.select.LSFilterSelectContainer;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTFilterSelect extends GuiFilterSelect<TileEntityLogisticalSorter, LSFilterSelectContainer> {

    public GuiTFilterSelect(LSFilterSelectContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected Runnable onItemStackButton() {
        return () -> sendPacketToServer(ClickedTileButton.LS_FILTER_ITEMSTACK, -1);
    }

    @Override
    protected Runnable onTagButton() {
        return () -> sendPacketToServer(ClickedTileButton.LS_FILTER_TAG, -1);
    }

    @Override
    protected Runnable onMaterialButton() {
        return () -> sendPacketToServer(ClickedTileButton.LS_FILTER_MATERIAL, -1);
    }

    @Override
    protected Runnable onModIDButton() {
        return () -> sendPacketToServer(ClickedTileButton.LS_FILTER_MOD_ID, -1);
    }

    @Override
    protected Runnable onBackButton() {
        return () -> sendPacketToServer(ClickedTileButton.BACK_BUTTON);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "filter_select.png");
    }
}