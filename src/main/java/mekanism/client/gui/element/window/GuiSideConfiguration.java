package mekanism.client.gui.element.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.SideDataButton;
import mekanism.client.gui.element.button.TooltipToggleButton;
import mekanism.client.gui.element.tab.GuiConfigTypeTab;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.configuration_update.PacketBatchConfiguration;
import mekanism.common.network.to_server.configuration_update.PacketEjectConfiguration;
import mekanism.common.network.to_server.configuration_update.PacketSideData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class GuiSideConfiguration<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiWindow {

    private final List<GuiConfigTypeTab> configTabs = new ArrayList<>();
    private final Map<RelativeSide, SideDataButton> sideConfigButtons = new EnumMap<>(RelativeSide.class);
    private final MekanismButton ejectButton;
    private final TILE tile;
    private TransmissionType currentType;

    public GuiSideConfiguration(IGuiWrapper gui, int x, int y, TILE tile, SelectedWindowData windowData) {
        super(gui, x, y, 156, 135, windowData);
        if (windowData.type != WindowType.SIDE_CONFIG) {
            throw new IllegalArgumentException("Side configs must have a side config window type");
        }
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        List<TransmissionType> transmissions = this.tile.getConfig().getTransmissions();
        //Get the top transmission as the initial type
        setCurrentType(transmissions.getFirst());
        //TODO: Try to make the GUI look a bit better as it still seems a bit off with the scales and such
        // Maybe we want to eventually add some sort of "in world preview" type thing
        addChild(new GuiInnerScreen(gui, relativeX + 38, relativeY + 25, 80, 12, () -> {
            ConfigInfo config = getCurrentConfig();
            if (config == null || !config.canEject()) {
                return Collections.singletonList(MekanismLang.NO_EJECT.translate());
            }
            return Collections.singletonList(MekanismLang.EJECT.translate(OnOff.of(config.isEjecting())));
        }).tooltip(() -> {
            ConfigInfo config = getCurrentConfig();
            if (config == null || !config.canEject()) {
                return Collections.singletonList(MekanismLang.CANT_EJECT_TOOLTIP.translate());
            }
            return Collections.emptyList();
        }));
        for (int i = 0; i < transmissions.size(); i++) {
            GuiConfigTypeTab tab = new GuiConfigTypeTab(gui, transmissions.get(i), relativeX + (i < 4 ? -26 : width), relativeY + (2 + 28 * (i % 4)), this, i < 4);
            addChild(tab);
            configTabs.add(tab);
        }
        ejectButton = addChild(new MekanismImageButton(gui, relativeX + 136, relativeY + 6, 14, getButtonLocation("auto_eject"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketEjectConfiguration(this.tile.getBlockPos(), currentType))))
              .setTooltip(MekanismLang.AUTO_EJECT);
        addChild(new TooltipToggleButton(gui, relativeX + 136, relativeY + 95, 14, getButtonLocation("clear_sides"),
              () -> getTargetType(DataType::getNext) == DataType.NONE, (element, mouseX, mouseY) -> {
            DataType targetType = getTargetType(DataType::getNext);
            return PacketUtils.sendToServer(new PacketBatchConfiguration(this.tile.getBlockPos(), Screen.hasShiftDown() ? null : currentType, targetType));
        }, (element, mouseX, mouseY) -> {
            DataType targetType = getTargetType(DataType::getPrevious);
            return PacketUtils.sendToServer(new PacketBatchConfiguration(this.tile.getBlockPos(), Screen.hasShiftDown() ? null : currentType, targetType));
        }, TooltipUtils.create(MekanismLang.SIDE_CONFIG_CLEAR, MekanismLang.SIDE_CONFIG_CLEAR_ALL), TooltipUtils.create(MekanismLang.SIDE_CONFIG_INCREMENT)));
        addSideDataButton(RelativeSide.BOTTOM, 67, 92);
        addSideDataButton(RelativeSide.TOP, 67, 46);
        addSideDataButton(RelativeSide.FRONT, 67, 69);
        addSideDataButton(RelativeSide.BACK, 44, 92);
        addSideDataButton(RelativeSide.LEFT, 44, 69);
        addSideDataButton(RelativeSide.RIGHT, 90, 69);
        updateTabs();
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).startTracking(MekanismContainer.SIDE_CONFIG_WINDOW, this.tile.getConfig());
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_SIDE_CONFIG, tile, MekanismContainer.SIDE_CONFIG_WINDOW));
    }

    private DataType getTargetType(BiFunction<DataType, Predicate<DataType>, DataType> shift) {
        if (Screen.hasShiftDown()) {
            return DataType.NONE;
        }
        ConfigInfo info = tile.getConfig().getConfig(currentType);
        if (info != null) {
            DataType commonType = null;
            for (RelativeSide side : EnumUtils.SIDES) {
                if (info.isSideEnabled(side)) {
                    DataType current = info.getDataType(side);
                    if (commonType == null) {
                        commonType = current;
                    } else if (commonType != current) {
                        return DataType.NONE;
                    }
                }
            }
            if (commonType != null) {
                return shift.apply(commonType, info::supports);
            }
        }
        return DataType.NONE;
    }

    private void addSideDataButton(RelativeSide side, int xPos, int yPos) {
        sideConfigButtons.put(side, addChild(new SideDataButton(gui(), relativeX + xPos, relativeY + yPos, side,
              () -> tile.getConfig().getDataType(currentType, side), () -> {
            DataType dataType = tile.getConfig().getDataType(currentType, side);
            return dataType == null ? EnumColor.GRAY : dataType.getColor();
        }, tile, (pos, clickType, inputSide) -> new PacketSideData(pos, clickType, inputSide, currentType), true)));
    }

    @Override
    public void close() {
        super.close();
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, MekanismContainer.SIDE_CONFIG_WINDOW));
        ((MekanismContainer) ((GuiMekanism<?>) gui()).getMenu()).stopTracking(MekanismContainer.SIDE_CONFIG_WINDOW);
    }

    public void setCurrentType(TransmissionType type) {
        currentType = type;
    }

    @Nullable
    private ConfigInfo getCurrentConfig() {
        return this.tile.getConfig().getConfig(currentType);
    }

    public void updateTabs() {
        //Hide the current tab and make the others visible
        for (GuiConfigTypeTab tab : configTabs) {
            tab.visible = currentType != tab.getTransmissionType();
        }
        //Disable or enable the eject button based on if the transmission type supports ejecting
        ConfigInfo config = getCurrentConfig();
        if (config == null) {//Should never actually be null but just in case handle it
            ejectButton.active = false;
            for (SideDataButton button : sideConfigButtons.values()) {
                button.active = false;
            }
        } else {
            ejectButton.active = config.canEject();
            for (Map.Entry<RelativeSide, SideDataButton> entry : sideConfigButtons.entrySet()) {
                entry.getValue().active = config.isSideEnabled(entry.getKey());
            }
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.CONFIG_TYPE.translate(currentType), 5);
        drawScrollingString(guiGraphics, MekanismLang.SLOTS.translate(), 0, 120, TextAlignment.CENTER, subheadingTextColor(), 4, false);
    }

    @Override
    protected int getTitlePadEnd() {
        return super.getTitlePadEnd() + 18;
    }
}