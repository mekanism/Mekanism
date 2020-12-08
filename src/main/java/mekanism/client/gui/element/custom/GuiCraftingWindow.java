package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRightArrow;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;

public class GuiCraftingWindow extends GuiWindow {

    private final TileEntityMekanism tile;

    public GuiCraftingWindow(IGuiWrapper gui, int x, int y, TileEntityMekanism tile) {
        super(gui, x, y, 118, 80);
        this.tile = tile;
        interactionStrategy = InteractionStrategy.ALL;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 8 + column * 18, relativeY + 18 + row * 18));
            }
        }
        addChild(new GuiRightArrow(gui, relativeX + 66, relativeY + 38).jeiCrafting());
        addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 92, relativeY + 36));
        //TODO: Implement something for this
        //Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_TRACK_SIDE_CONFIG, tile, 1));
        //((MekanismContainer) ((GuiMekanism<?>) guiObj).getContainer()).startTracking(1, ((ISideConfiguration) tile).getConfig());
    }

    @Override
    public void close() {
        super.close();
        //TODO: Implement something for this
        //Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CONTAINER_STOP_TRACKING, tile, 1));
        //((MekanismContainer) ((GuiMekanism<?>) guiObj).getContainer()).stopTracking(1);
    }

    public <TILE extends TileEntityMekanism & ISideConfiguration> TILE getTile() {
        return (TILE) tile;
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        //TODO: Should this have its own translation key
        drawTitleText(matrix, MekanismLang.CRAFTING.translate(), 5);
    }
}