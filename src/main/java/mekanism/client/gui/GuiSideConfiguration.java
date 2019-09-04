package mekanism.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.button.MekanismButton.IHoverable;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.SideDataButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.tile.SideConfigurationContainer;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSideConfiguration extends GuiMekanismTile<TileEntityMekanism, SideConfigurationContainer> {

    private Map<Integer, GuiPos> slotPosMap = new HashMap<>();
    private TransmissionType currentType;
    private List<GuiConfigTypeTab> configTabs = new ArrayList<>();

    public GuiSideConfiguration(SideConfigurationContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize = 95;
        currentType = getTopTransmission();
        slotPosMap.put(0, new GuiPos(81, 64));
        slotPosMap.put(1, new GuiPos(81, 34));
        slotPosMap.put(2, new GuiPos(81, 49));
        slotPosMap.put(3, new GuiPos(66, 64));
        slotPosMap.put(4, new GuiPos(66, 49));
        slotPosMap.put(5, new GuiPos(96, 49));
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

        addButton(new MekanismImageButton(guiLeft + 6, guiTop + 6, 14, getButtonLocation("back"),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tileEntity.getPos()))));
        addButton(new MekanismImageButton(guiLeft + 156, guiTop + 6, 14, getButtonLocation("auto_eject"),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.EJECT, Coord4D.get(tileEntity), 0, 0, currentType)),
              getOnHover("gui.mekanism.autoEject")));
        for (int i = 0; i < slotPosMap.size(); i++) {
            GuiPos guiPos = slotPosMap.get(i);
            Direction facing = Direction.byIndex(i);
            addButton(new SideDataButton(guiLeft + guiPos.xPos, guiTop + guiPos.yPos, getGuiLocation(), i,
                  () -> getTile().getConfig().getOutput(currentType, facing), () -> getTile().getConfig().getOutput(currentType, facing).color, tileEntity, currentType,
                  ConfigurationPacket.SIDE_DATA, getOnHover()));
        }
    }

    private IHoverable getOnHover() {
        return (onHover, xAxis, yAxis) -> {
            if (onHover instanceof SideDataButton) {
                SideData data = ((SideDataButton) onHover).getSideData();
                if (data != TileComponentConfig.EMPTY) {
                    displayTooltip(TextComponentUtil.build(data.color, data, " (", data.color.getColoredName(), ")"), xAxis, yAxis);
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
        if (getTile().getConfig().canEject(currentType)) {
            drawString(TextComponentUtil.build(Translation.of("gui.mekanism.eject"), ": ", OnOff.of(getTile().getConfig().isEjecting(currentType))), 53, 17, 0x00CD00);
        } else {
            drawString(TextComponentUtil.translate("gui.mekanism.noEject"), 53, 17, 0x00CD00);
        }
        drawString(TextComponentUtil.translate("gui.mekanism.slots"), 77, 81, 0x787878);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        if (tileEntity == null || minecraft.world.getTileEntity(tileEntity.getPos()) == null) {
            minecraft.displayGuiScreen(null);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "configuration.png");
    }

    public static class GuiPos {

        public final int xPos;
        public final int yPos;

        public GuiPos(int x, int y) {
            xPos = x;
            yPos = y;
        }
    }
}