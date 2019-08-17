package mekanism.client.gui.filter;

import mekanism.common.inventory.container.tile.filter.select.DMFilterSelectContainer;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMFilterSelect extends GuiFilterSelect<TileEntityDigitalMiner, DMFilterSelectContainer> {

    public GuiMFilterSelect(DMFilterSelectContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected IPressable onItemStackButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.DM_FILTER_ITEMSTACK);
    }

    @Override
    protected IPressable onTagButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.DM_FILTER_TAG);
    }

    @Override
    protected IPressable onMaterialButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.DM_FILTER_MATERIAL);
    }

    @Override
    protected IPressable onModIDButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.DM_FILTER_MOD_ID);
    }

    @Override
    protected IPressable onBackButton() {
        return onPress -> sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiFilterSelect.png");
    }
}