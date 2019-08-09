package mekanism.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration.GuiPos;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.client.gui.button.GuiSideDataButton;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTransporterConfig extends GuiMekanismTile<TileEntityMekanism> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();
    private ISideConfiguration configurable;
    private List<GuiSideDataButton> sideDataButtons = new ArrayList<>();
    private Button backButton;
    private Button strictInputButton;
    private Button colorButton;
    private int buttonID = 0;

    public GuiTransporterConfig(PlayerEntity player, ISideConfiguration tile) {
        super((TileEntityMekanism) tile, new ContainerNull(player, (TileEntityMekanism) tile));
        ySize = 95;
        configurable = tile;
        slotPosMap.put(0, new GuiPos(54, 64));
        slotPosMap.put(1, new GuiPos(54, 34));
        slotPosMap.put(2, new GuiPos(54, 49));
        slotPosMap.put(3, new GuiPos(39, 64));
        slotPosMap.put(4, new GuiPos(39, 49));
        slotPosMap.put(5, new GuiPos(69, 49));
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 6, guiTop + 6, 14, 14, 190, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketSimpleGui(Coord4D.get((TileEntity) configurable), 0, Mekanism.proxy.getGuiId(((TileEntity) configurable).getBlockType())))));
        buttons.add(strictInputButton = new GuiButtonDisableableImage(guiLeft + 156, guiTop + 6, 14, 14, 204, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.STRICT_INPUT, Coord4D.get((TileEntity) configurable), 0, 0, null))));
        buttons.add(colorButton = new GuiColorButton(guiLeft + 122, guiTop + 49, 16, 16, () -> configurable.getEjector().getOutputColor(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get((TileEntity) configurable),
                    InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, 0, null))));
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos guiPos = slotPosMap.get(i);
            Direction facing = Direction.byIndex(i);
            GuiSideDataButton button = new GuiSideDataButton(guiLeft + guiPos.xPos, guiTop + guiPos.yPos, getGuiLocation(), i,
                  () -> configurable.getConfig().getOutput(TransmissionType.ITEM, facing), () -> configurable.getEjector().getInputColor(facing), () -> (TileEntity) configurable);
            buttons.add(button);
            sideDataButtons.add(button);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String text = LangUtils.localize("gui.configuration.transporter");
        font.drawString(text, (xSize / 2) - (font.getStringWidth(text) / 2), 5, 0x404040);
        text = LangUtils.localize("gui.strictInput") + " (" + LangUtils.transOnOff(configurable.getEjector().hasStrictInput()) + ")";
        renderScaledText(text, 53, 17, 0x00CD00, 70);
        font.drawString(LangUtils.localize("gui.input"), 48, 81, 0x787878);
        font.drawString(LangUtils.localize("gui.output"), 114, 68, 0x787878);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (GuiSideDataButton button : sideDataButtons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                SideData data = button.getSideData();
                if (data != TileComponentConfig.EMPTY) {
                    EnumColor color = button.getColor();
                    displayTooltip(color != null ? color.getColoredName() : LangUtils.localize("gui.none"), xAxis, yAxis);
                }
                break;
            }
        }
        if (strictInputButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(LangUtils.localize("gui.configuration.strictInput"), xAxis, yAxis);
        } else if (colorButton.isMouseOver(mouseX, mouseY)) {
            if (configurable.getEjector().getOutputColor() != null) {
                displayTooltip(configurable.getEjector().getOutputColor().getColoredName(), xAxis, yAxis);
            } else {
                displayTooltip(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 1) {
            TileEntity tile = (TileEntity) configurable;
            if (colorButton.isMouseOver(mouseX, mouseY)) {
                //Allow going backwards
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tile), 1, 0, null));
            } else {
                //Handle right clicking the side data buttons
                for (GuiSideDataButton sideDataButton : sideDataButtons) {
                    if (sideDataButton.isMouseOver(mouseX, mouseY)) {
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tile), 1, sideDataButton.getSlotPosMapIndex(), null));
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTransporterConfig.png");
    }
}