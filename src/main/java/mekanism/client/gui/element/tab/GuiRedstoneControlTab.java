package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.EnumMap;
import java.util.Map;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;

public class GuiRedstoneControlTab extends GuiInsetElement<TileEntityMekanism> {

    private static final Identifier DISABLED = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_disabled.png");
    private static final Identifier HIGH = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_high.png");
    private static final Identifier LOW = MekanismUtils.getResource(ResourceType.GUI, "redstone_control_low.png");

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
            default -> super.getOverlay();
        };
    }

    @Override
    protected int getTabColor(GuiGraphicsExtractor guiGraphics) {
        return MekanismRenderer.color(SpecialColors.TAB_REDSTONE_CONTROL);
    }

    @Override
    protected void drawBackgroundOverlay(GuiGraphicsExtractor guiGraphics) {
        if (dataSource.getControlType() == RedstoneControl.PULSE) {
            //TODO - 26.2: figure out the rest of the params
            //guiGraphics.blit(RenderPipelines.GUI, MekanismRenderer.REDSTONE_PULSE_ID, getButtonX() + 1, getButtonY() + 1, 0, innerWidth - 2, innerHeight - 2);
        } else {
            super.drawBackgroundOverlay(guiGraphics);
        }
    }
}