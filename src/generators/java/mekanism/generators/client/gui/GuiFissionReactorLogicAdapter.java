package mekanism.generators.client.gui;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.generators.client.gui.element.button.ReactorLogicButton;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFissionReactorLogicAdapter extends GuiMekanismTile<TileEntityFissionReactorLogicAdapter, EmptyTileContainer<TileEntityFissionReactorLogicAdapter>> {

    private static final int DISPLAY_COUNT = 4;

    private GuiScrollBar scrollBar;

    public GuiFissionReactorLogicAdapter(EmptyTileContainer<TileEntityFissionReactorLogicAdapter> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth += 20;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiElementHolder(this, 26, 31, 130, 90));
        scrollBar = addRenderableWidget(new GuiScrollBar(this, 156, 31, 90, () -> tile.getModes().length, () -> DISPLAY_COUNT));
        for (int i = 0; i < DISPLAY_COUNT; i++) {
            int typeShift = 22 * i;
            addRenderableWidget(new ReactorLogicButton<>(this, 27, 32 + typeShift, i, tile, FissionReactorLogic.class, scrollBar::getCurrentSelection, tile::getModes, this::changeLogic));
        }
    }

    private void changeLogic(FissionReactorLogic type) {
        if (type != null) {
            PacketUtils.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.LOGIC_TYPE, tile, type.ordinal()));
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        drawScrollingString(guiGraphics, GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(tile.logicType.getColor(), tile.logicType), 0, 123, TextAlignment.CENTER, titleTextColor(), 4, false);
        drawScrollingString(guiGraphics, MekanismLang.STATUS.translate(EnumColor.RED, tile.getStatus()), 0, 136, TextAlignment.CENTER, titleTextColor(), 4, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY) || scrollBar.adjustScroll(deltaY);
    }
}