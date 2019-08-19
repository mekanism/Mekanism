package mekanism.client.gui.filter;

import mekanism.common.inventory.container.tile.filter.select.LSFilterSelectContainer;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button.IPressable;
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
    protected IPressable onItemStackButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.LS_FILTER_ITEMSTACK);
    }

    @Override
    protected IPressable onTagButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.LS_FILTER_TAG);
    }

    @Override
    protected IPressable onMaterialButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.LS_FILTER_MATERIAL);
    }

    @Override
    protected IPressable onModIDButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.LS_FILTER_MOD_ID);
    }

    @Override
    protected IPressable onBackButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.BACK_BUTTON);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "filter_select.png");
    }
}