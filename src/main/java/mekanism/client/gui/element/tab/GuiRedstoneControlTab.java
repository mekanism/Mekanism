package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.EnumMap;
import java.util.Map;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;

public class GuiRedstoneControlTab extends GuiInsetElement<TileEntityMekanism> {

    private static final Identifier REDSTONE_PULSE_ID = Mekanism.rl("redstone_control/pulse");
    private static final Identifier DISABLED = Mekanism.rl("redstone_control/disabled");
    private static final Identifier HIGH = Mekanism.rl("redstone_control/high");
    private static final Identifier LOW = Mekanism.rl("redstone_control/low");

    private final Map<RedstoneControl, Tooltip> tooltips = new EnumMap<>(RedstoneControl.class);

    public GuiRedstoneControlTab(IGuiWrapper gui, TileEntityMekanism tile) {
        super(DISABLED, gui, tile, gui.getXSize(), 137, 26, 18, false);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(tooltips.computeIfAbsent(dataSource.getControlType(), type -> TooltipUtils.create(type.getTextComponent())));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        PacketUtils.sendToServer(new PacketGuiInteract(event.button() == InputConstants.MOUSE_BUTTON_LEFT ? GuiInteraction.NEXT_REDSTONE_CONTROL
                                                                                                          : GuiInteraction.PREVIOUS_REDSTONE_CONTROL, dataSource));
    }

    @Override
    public boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected Identifier getOverlay() {
        return switch (dataSource.getControlType()) {
            case HIGH -> HIGH;
            case LOW -> LOW;
            case PULSE -> REDSTONE_PULSE_ID;
            default -> super.getOverlay();
        };
    }

    @Override
    protected int getTabColor() {
        return SpecialColors.TAB_REDSTONE_CONTROL.argb();
    }
}