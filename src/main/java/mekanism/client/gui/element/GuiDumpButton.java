package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.level.block.entity.BlockEntity;

//TODO: Potentially make this extend MekanismImageButton
public class GuiDumpButton<TILE extends BlockEntity & IHasDumpButton> extends GuiTexturedElement {

    protected final TILE tile;

    public GuiDumpButton(IGuiWrapper gui, TILE tile, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI, "dump.png"), gui, x, y, 21, 10);
        this.tile = tile;
        this.clickSound = BUTTON_CLICK_SOUND;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, 0, 0, width, height, width, height);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.DUMP_BUTTON, tile));
    }
}