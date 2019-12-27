package mekanism.generators.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.button.MekanismButton.IHoverable;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.client.gui.button.ReactorLogicButton;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.inventory.container.reactor.ReactorLogicAdapterContainer;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiReactorLogicAdapter extends GuiMekanismTile<TileEntityReactorLogicAdapter, ReactorLogicAdapterContainer> {

    public GuiReactorLogicAdapter(ReactorLogicAdapterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new MekanismImageButton(this, guiLeft + 23, guiTop + 19, 11, 18, getButtonLocation("toggle"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0))), getOnHover(GeneratorsLang.REACTOR_LOGIC_TOGGLE_COOLING)));
        for (ReactorLogic type : ReactorLogic.values()) {
            int typeShift = 22 * type.ordinal();
            addButton(new ReactorLogicButton(this, guiLeft + 24, guiTop + 32 + typeShift, type, tile, getGuiLocation(),
                  () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1, type))), getOnHover()));
        }
    }

    private IHoverable getOnHover() {
        return (onHover, xAxis, yAxis) -> {
            if (onHover instanceof ReactorLogicButton) {
                ReactorLogic type = ((ReactorLogicButton) onHover).getType();
                int typeOffset = 22 * type.ordinal();
                renderItem(type.getRenderStack(), 27, 35 + typeOffset);
                drawString(TextComponentUtil.build(EnumColor.WHITE, type), 46, 34 + typeOffset, 0x404040);
                displayTooltip(type.getDescription(), xAxis, yAxis);
            }
        };
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        renderScaledText(GeneratorsLang.REACTOR_LOGIC_ACTIVE_COOLING.translate(EnumColor.RED, OnOff.of(tile.activeCooled)), 36, 20, 0x404040, 117);
        renderScaledText(GeneratorsLang.REACTOR_LOGIC_REDSTONE_OUTPUT_MODE.translate(EnumColor.RED, tile.logicType), 23, 123, 0x404040, 130);
        drawCenteredText(MekanismLang.STATUS.translate(EnumColor.RED, tile.checkMode() ? GeneratorsLang.REACTOR_LOGIC_OUTPUTTING : MekanismLang.IDLE),
              0, xSize, 136, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "reactor_logic_adapter.png");
    }
}