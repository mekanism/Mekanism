package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.MMaterialFilter;
import mekanism.common.inventory.container.tile.filter.DMMaterialFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMMaterialFilter extends GuiMaterialFilter<MMaterialFilter, TileEntityDigitalMiner, DMMaterialFilterContainer> {

    public GuiMMaterialFilter(DMMaterialFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(saveButton = new TranslationButton(this, guiLeft + 27, guiTop + 62, 60, 20, "gui.mekanism.save", () -> {
            if (!filter.getMaterialItem().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
            } else {
                status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.itemFilter.noItem"));
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(this, guiLeft + 89, guiTop + 62, 60, 20, "gui.mekanism.delete", () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.DIGITAL_MINER_CONFIG);
        }));
        addButton(new MekanismImageButton(this, guiLeft + 5, guiTop + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(isNew ? ClickedTileButton.DM_SELECT_FILTER_TYPE : ClickedTileButton.DIGITAL_MINER_CONFIG)));
        addButton(new MekanismImageButton(this, guiLeft + 148, guiTop + 45, 14, 16, getButtonLocation("exclamation"),
              () -> filter.requireStack = !filter.requireStack, getOnHoverReplace(filter)));
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getMaterialItem().isEmpty()) {
            renderScaledText(filter.getMaterialItem().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        drawMinerForegroundLayer(filter.getMaterialItem());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - guiLeft;
            double yAxis = mouseY - guiTop;
            if (overTypeInput(xAxis, yAxis)) {
                materialMouseClicked();
            } else {
                minerFilterClickCommon(xAxis, yAxis, filter);
            }
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "miner_filter.png");
    }
}