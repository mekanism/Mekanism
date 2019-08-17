package mekanism.client.gui.filter;

import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
import mekanism.common.Mekanism;
import mekanism.common.content.miner.MMaterialFilter;
import mekanism.common.inventory.container.tile.filter.DMMaterialFilterContainer;
import mekanism.common.inventory.container.tile.filter.DMModIDFilterContainer;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMMaterialFilter extends GuiMaterialFilter<MMaterialFilter, TileEntityDigitalMiner, DMMaterialFilterContainer> {

    public GuiMMaterialFilter(PlayerEntity player, TileEntityDigitalMiner tile, int index) {
        //TODO
        super(player, tile);
        origFilter = (MMaterialFilter) tileEntity.filters.get(index);
        filter = ((MMaterialFilter) tileEntity.filters.get(index)).clone();
    }

    public GuiMMaterialFilter(DMMaterialFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        isNew = true;
        filter = new MMaterialFilter();
    }

    @Override
    protected void addButtons() {
        buttons.add(saveButton = new GuiButtonTranslation(guiLeft + 27, guiTop + 62, 60, 20, "gui.save", onPress -> {
            if (!filter.getMaterialItem().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(Coord4D.get(tileEntity), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), false, origFilter, filter));
                }
                sendPacketToServer(0);
            } else {
                status = TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.itemFilter.noItem"));
                ticker = 20;
            }
        }));
        buttons.add(deleteButton = new GuiButtonTranslation(guiLeft + 89, guiTop + 62, 60, 20, "gui.delete", onPress -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(Coord4D.get(tileEntity), true, origFilter, null));
            sendPacketToServer(0);
        }));
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacketToServer(isNew ? 4 : 0)));
        buttons.add(replaceButton = new GuiButtonDisableableImage(guiLeft + 148, guiTop + 45, 14, 14, 199, 14, -14, getGuiLocation(),
              onPress -> filter.requireStack = !filter.requireStack));
    }

    @Override
    protected void sendPacketToServer(int guiID) {
        Mekanism.packetHandler.sendToServer(new PacketDigitalMinerGui(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), guiID, 0, 0));
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        if (!filter.getMaterialItem().isEmpty()) {
            renderScaledText(filter.getMaterialItem().getDisplayName(), 35, 41, 0x00CD00, 107);
        }
        drawMinerForegroundLayer(mouseX, mouseY, filter.getMaterialItem());
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
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiMMaterialFilter.png");
    }
}