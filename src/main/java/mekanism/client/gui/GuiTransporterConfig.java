package mekanism.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration.GuiPos;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.client.gui.button.GuiSideDataButton;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.tile.TransporterConfigurationContainer;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTransporterConfig<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiMekanismTile<TILE, TransporterConfigurationContainer<TILE>> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();
    private List<GuiSideDataButton> sideDataButtons = new ArrayList<>();
    private Button backButton;
    private Button strictInputButton;
    private Button colorButton;

    public GuiTransporterConfig(TransporterConfigurationContainer<TILE> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize = 95;
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
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tileEntity.getPos()))));
        buttons.add(strictInputButton = new GuiButtonDisableableImage(guiLeft + 156, guiTop + 6, 14, 14, 204, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.STRICT_INPUT, Coord4D.get(tileEntity), 0, 0, null))));
        buttons.add(colorButton = new GuiColorButton(guiLeft + 122, guiTop + 49, 16, 16, () -> tileEntity.getEjector().getOutputColor(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tileEntity),
                    InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, 0, null))));
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos guiPos = slotPosMap.get(i);
            Direction facing = Direction.byIndex(i);
            GuiSideDataButton button = new GuiSideDataButton(guiLeft + guiPos.xPos, guiTop + guiPos.yPos, getGuiLocation(), i,
                  () -> tileEntity.getConfig().getOutput(TransmissionType.ITEM, facing), () -> tileEntity.getEjector().getInputColor(facing), () -> tileEntity);
            buttons.add(button);
            sideDataButtons.add(button);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(TextComponentUtil.translate("gui.configuration.transporter"), 0, xSize, 5, 0x404040);
        renderScaledText(TextComponentUtil.build(Translation.of("gui.strictInput"), " (", OnOff.of(tileEntity.getEjector().hasStrictInput()), ")"),
              53, 17, 0x00CD00, 70);
        drawString(TextComponentUtil.translate("mekanism.gui.input"), 48, 81, 0x787878);
        drawString(TextComponentUtil.translate("gui.output"), 114, 68, 0x787878);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        for (GuiSideDataButton button : sideDataButtons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                SideData data = button.getSideData();
                if (data != TileComponentConfig.EMPTY) {
                    EnumColor color = button.getColor();
                    if (color != null) {
                        displayTooltip(color.getColoredName(), xAxis, yAxis);
                    } else {
                        displayTooltip(TextComponentUtil.translate("mekanism.gui.none"), xAxis, yAxis);
                    }
                }
                break;
            }
        }
        if (strictInputButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(TextComponentUtil.translate("mekanism.gui.configuration.strictInput"), xAxis, yAxis);
        } else if (colorButton.isMouseOver(mouseX, mouseY)) {
            if (tileEntity.getEjector().getOutputColor() != null) {
                displayTooltip(tileEntity.getEjector().getOutputColor().getColoredName(), xAxis, yAxis);
            } else {
                displayTooltip(TextComponentUtil.translate("mekanism.gui.none"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 1) {
            if (colorButton.isMouseOver(mouseX, mouseY)) {
                //Allow going backwards
                SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tileEntity), 1, 0, null));
            } else {
                //Handle right clicking the side data buttons
                for (GuiSideDataButton sideDataButton : sideDataButtons) {
                    if (sideDataButton.isMouseOver(mouseX, mouseY)) {
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tileEntity), 1, sideDataButton.getSlotPosMapIndex(), null));
                        break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "transporter_config.png");
    }
}