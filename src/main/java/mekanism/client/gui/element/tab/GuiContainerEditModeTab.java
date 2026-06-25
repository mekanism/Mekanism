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
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;

public class GuiContainerEditModeTab<TILE extends TileEntityMekanism & IFluidContainerManager> extends GuiInsetElement<TILE> {

    private static final Identifier BOTH = Mekanism.rl("container_edit_mode/both");
    private static final Identifier FILL = Mekanism.rl("container_edit_mode/fill");
    private static final Identifier EMPTY = Mekanism.rl("container_edit_mode/empty");

    private final Map<ContainerEditMode, Tooltip> tooltips = new EnumMap<>(ContainerEditMode.class);

    public GuiContainerEditModeTab(IGuiWrapper gui, TILE tile) {
        super(BOTH, gui, tile, gui.getImageWidth(), 138, 26, 18, false);
    }

    @Override
    protected Identifier getOverlay() {
        return switch (dataSource.getContainerEditMode()) {
            case FILL -> FILL;
            case EMPTY -> EMPTY;
            default -> super.getOverlay();
        };
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(tooltips.computeIfAbsent(dataSource.getContainerEditMode(), mode -> TooltipUtils.create(mode.getTextComponent())));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        PacketUtils.sendToServer(new PacketGuiInteract(event.button() == InputConstants.MOUSE_BUTTON_LEFT ? GuiInteraction.NEXT_MODE : GuiInteraction.PREVIOUS_MODE, dataSource));
    }

    @Override
    public boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected int getTabColor() {
        return SpecialColors.TAB_CONTAINER_EDIT_MODE.argb();
    }
}
