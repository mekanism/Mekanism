package mekanism.client.gui;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration.GuiPos;
import mekanism.client.gui.button.ColorButton;
import mekanism.client.gui.button.DisableableImageButton;
import mekanism.client.gui.button.MekanismButton.IHoverable;
import mekanism.client.gui.button.SideDataButton;
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
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiTransporterConfig extends GuiMekanismTile<TileEntityMekanism, TransporterConfigurationContainer> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();

    public GuiTransporterConfig(TransporterConfigurationContainer container, PlayerInventory inv, ITextComponent title) {
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
        addButton(new DisableableImageButton(guiLeft + 6, guiTop + 6, 14, 14, 190, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tileEntity.getPos()))));
        addButton(new DisableableImageButton(guiLeft + 156, guiTop + 6, 14, 14, 204, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.STRICT_INPUT, Coord4D.get(tileEntity), 0, 0, null)),
              getOnHover("gui.mekanism.configuration.strictInput")));
        addButton(new ColorButton(guiLeft + 122, guiTop + 49, 16, 16, this, () -> getTile().getEjector().getOutputColor(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tileEntity),
                    InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, 0, null)),
              onRightClick -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT_COLOR, Coord4D.get(tileEntity), 1, 0, null))));
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos guiPos = slotPosMap.get(i);
            Direction facing = Direction.byIndex(i);
            addButton(new SideDataButton(guiLeft + guiPos.xPos, guiTop + guiPos.yPos, getGuiLocation(), i,
                  () -> getTile().getConfig().getOutput(TransmissionType.ITEM, facing), () -> getTile().getEjector().getInputColor(facing), tileEntity, null,
                  ConfigurationPacket.INPUT_COLOR, getOnHover()));
        }
    }

    public <TILE extends TileEntityMekanism & ISideConfiguration> TILE getTile() {
        return (TILE) tileEntity;
    }

    private IHoverable getOnHover() {
        return (onHover, xAxis, yAxis) -> {
            if (onHover instanceof SideDataButton) {
                SideDataButton button = (SideDataButton) onHover;
                SideData data = button.getSideData();
                if (data != TileComponentConfig.EMPTY) {
                    EnumColor color = button.getColor();
                    if (color != null) {
                        displayTooltip(color.getColoredName(), xAxis, yAxis);
                    } else {
                        displayTooltip(TextComponentUtil.translate("gui.mekanism.none"), xAxis, yAxis);
                    }
                }
            }
        };
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(TextComponentUtil.translate("gui.mekanism.configuration.transporter"), 0, xSize, 5, 0x404040);
        renderScaledText(TextComponentUtil.build(Translation.of("gui.mekanism.strictInput"), " (", OnOff.of(getTile().getEjector().hasStrictInput()), ")"),
              53, 17, 0x00CD00, 70);
        drawString(TextComponentUtil.translate("gui.mekanism.input"), 48, 81, 0x787878);
        drawString(TextComponentUtil.translate("gui.mekanism.output"), 114, 68, 0x787878);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "transporter_config.png");
    }
}