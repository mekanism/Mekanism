package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.gui.button.GuiReactorLogicButton;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiReactorLogicAdapter extends GuiMekanismTile<TileEntityReactorLogicAdapter> {

    private List<GuiReactorLogicButton> typeButtons = new ArrayList<>();
    private Button coolingButton;

    public GuiReactorLogicAdapter(PlayerInventory inventory, final TileEntityReactorLogicAdapter tile) {
        super(tile, new ContainerNull(inventory.player, tile));
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(coolingButton = new GuiButtonDisableableImage(guiLeft + 23, guiTop + 19, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(0)))));
        for (ReactorLogic type : ReactorLogic.values()) {
            int typeShift = 22 * type.ordinal();
            GuiReactorLogicButton button = new GuiReactorLogicButton(guiLeft + 24, guiTop + 32 + typeShift, type, tileEntity, getGuiLocation(),
                  onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1, type.ordinal()));
            buttons.add(button);
            typeButtons.add(button);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        renderScaledText(LangUtils.localize("gui.coolingMeasurements") + ": " + EnumColor.RED + LangUtils.transOnOff(tileEntity.activeCooled), 36, 20, 0x404040, 117);
        renderScaledText(LangUtils.localize("gui.redstoneOutputMode") + ": " + EnumColor.RED + tileEntity.logicType.getLocalizedName(), 23, 123, 0x404040, 130);
        String text = LangUtils.localize("gui.status") + ": " + EnumColor.RED + LangUtils.localize("gui." + (tileEntity.checkMode() ? "outputting" : "idle"));
        font.drawString(text, (xSize / 2) - (font.getStringWidth(text) / 2), 136, 0x404040);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (GuiReactorLogicButton button : typeButtons) {
            ReactorLogic type = button.getType();
            int typeOffset = 22 * type.ordinal();
            renderItem(type.getRenderStack(), 27, 35 + typeOffset);
            font.drawString(EnumColor.WHITE + type.getLocalizedName(), 46, 34 + typeOffset, 0x404040);
            if (button.isMouseOver(mouseX, mouseY)) {
                displayTooltips(MekanismUtils.splitTooltip(type.getDescription(), ItemStack.EMPTY), xAxis, yAxis);
            }
        }
        if (coolingButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(LangUtils.localize("gui.toggleCooling"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiReactorLogicAdapter.png");
    }
}