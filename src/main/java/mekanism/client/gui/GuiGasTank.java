package mekanism.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.GasTankContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiGasTank extends GuiMekanismTile<TileEntityGasTank, GasTankContainer> {

    public GuiGasTank(GasTankContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiSideConfigurationTab(this, tileEntity, resource));
        addButton(new GuiTransporterConfigTab(this, tileEntity, resource));
        addButton(new GuiSlot(SlotType.OUTPUT, this, resource, 7, 7).with(SlotOverlay.PLUS));
        addButton(new GuiSlot(SlotType.INPUT, this, resource, 7, 39).with(SlotOverlay.MINUS));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        ITextComponent component;
        if (tileEntity.gasTank.getStored() == Integer.MAX_VALUE) {
            component = TextComponentUtil.translate("gui.mekanism.infinite");
        } else if (tileEntity.tier.getStorage() == Integer.MAX_VALUE) {
            component = TextComponentUtil.build(tileEntity.gasTank.getStored(), "/", Translation.of("gui.mekanism.infinite"));
        } else {
            component = TextComponentUtil.getString(tileEntity.gasTank.getStored() + "/" + tileEntity.tier.getStorage());
        }
        drawString(component, 45, 40, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        GasStack gasStack = tileEntity.gasTank.getGas();
        if (gasStack != null) {
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.gas"), ": ", gasStack), 45, 49, 0x404040, 112);
        } else {
            renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.gas"), ": ", Translation.of("gui.mekanism.none")), 45, 49, 0x404040, 112);
        }
        drawString(TextComponentUtil.translate("container.inventory"), 8, ySize - 96 + 2, 0x404040);
        ITextComponent dumpingComponent = TextComponentUtil.build(tileEntity.dumping);
        drawString(dumpingComponent, 156 - getStringWidth(dumpingComponent), 73, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = GasMode.chooseByMode(tileEntity.dumping, 10, 18, 26);
        drawTexturedRect(guiLeft + 160, guiTop + 73, 176, displayInt, 8, 8);
        if (tileEntity.gasTank.getGas() != null) {
            int scale = (int) (((double) tileEntity.gasTank.getStored() / tileEntity.tier.getStorage()) * 72);
            drawTexturedRect(guiLeft + 65, guiTop + 17, 176, 0, scale, 10);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        double xAxis = mouseX - guiLeft;
        double yAxis = mouseY - guiTop;
        if (xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82) {
            TileNetworkList data = TileNetworkList.withContents(0);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "gas_tank.png");
    }
}