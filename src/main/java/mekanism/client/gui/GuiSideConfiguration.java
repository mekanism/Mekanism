package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.button.MekanismButton.IHoverable;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.SideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.tile.SideConfigurationContainer;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiSideConfiguration extends GuiMekanismTile<TileEntityMekanism, SideConfigurationContainer> {

    private List<GuiPos> slotPosList = new ArrayList<>();
    private TransmissionType currentType;
    private List<GuiConfigTypeTab> configTabs = new ArrayList<>();

    public GuiSideConfiguration(SideConfigurationContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize = 95;
        currentType = getTopTransmission();
        slotPosList.add(new GuiPos(RelativeSide.BOTTOM, 81, 64));
        slotPosList.add(new GuiPos(RelativeSide.TOP, 81, 34));
        slotPosList.add(new GuiPos(RelativeSide.FRONT, 81, 49));
        slotPosList.add(new GuiPos(RelativeSide.BACK, 66, 64));
        slotPosList.add(new GuiPos(RelativeSide.LEFT, 66, 49));
        slotPosList.add(new GuiPos(RelativeSide.RIGHT, 96, 49));
    }

    public <TILE extends TileEntityMekanism & ISideConfiguration> TILE getTile() {
        return (TILE) tileEntity;
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        List<TransmissionType> transmissions = getTile().getConfig().getTransmissions();
        for (int i = 0; i < transmissions.size(); i++) {
            TransmissionType type = transmissions.get(i);
            //TODO: Figure out if there is a simpler way to do y
            GuiConfigTypeTab tab = new GuiConfigTypeTab(this, type, resource, (i < 3 ? -26 : 176), 2 + ((i % 3) * (26 + 2)));
            addButton(tab);
            configTabs.add(tab);
        }
        updateTabs();

        addButton(new MekanismImageButton(this, guiLeft + 6, guiTop + 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tileEntity.getPos()))));
        addButton(new MekanismImageButton(this, guiLeft + 156, guiTop + 6, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT, Coord4D.get(tileEntity), 0, 0, currentType)),
              getOnHover("gui.mekanism.autoEject")));
        for (GuiPos guiPos : slotPosList) {
            addButton(new SideDataButton(this, guiLeft + guiPos.xPos, guiTop + guiPos.yPos, guiPos.relativeSide.ordinal(),
                  () -> getTile().getConfig().getDataType(currentType, guiPos.relativeSide), () -> {
                DataType dataType = getTile().getConfig().getDataType(currentType, guiPos.relativeSide);
                return dataType == null ? EnumColor.GRAY : dataType.getColor();
            }, tileEntity, () -> currentType, ConfigurationPacket.SIDE_DATA, getOnHover()));
        }
    }

    private IHoverable getOnHover() {
        return (onHover, xAxis, yAxis) -> {
            if (onHover instanceof SideDataButton) {
                DataType dataType = ((SideDataButton) onHover).getDataType();
                if (dataType != null) {
                    displayTooltip(TextComponentUtil.build(dataType.getColor(), dataType, " (", dataType.getColor().getColoredName(), ")"), xAxis, yAxis);
                }
            }
        };
    }

    public TransmissionType getTopTransmission() {
        return getTile().getConfig().getTransmissions().get(0);
    }

    public void setCurrentType(TransmissionType type) {
        currentType = type;
    }

    public void updateTabs() {
        //TODO: Is there a reason to not have them just always all be visible?
        for (GuiConfigTypeTab tab : configTabs) {
            tab.visible = currentType != tab.getTransmissionType();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(TextComponentUtil.build(currentType, " ", Translation.of("gui.mekanism.config")), 0, xSize, 5, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        ConfigInfo config = getTile().getConfig().getConfig(currentType);
        if (config != null && config.canEject()) {
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.eject"), ": ", OnOff.of(config.isEjecting())), 53, 17, 0x00CD00);
        } else {
            drawString(TextComponentUtil.translate("gui.mekanism.noEject"), 53, 17, 0x00CD00);
        }
        drawString(TextComponentUtil.translate("gui.mekanism.slots"), 77, 81, 0x787878);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        if (tileEntity == null || MekanismUtils.getTileEntity(minecraft.world, tileEntity.getPos()) == null) {
            minecraft.displayGuiScreen(null);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "configuration.png");
    }

    public static class GuiPos {

        public final RelativeSide relativeSide;
        public final int xPos;
        public final int yPos;

        public GuiPos(RelativeSide side, int x, int y) {
            relativeSide = side;
            xPos = x;
            yPos = y;
        }
    }
}