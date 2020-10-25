package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.client.gui.element.button.ReactorLogicButton;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFissionReactorLogicAdapter extends GuiMekanismTile<TileEntityFissionReactorLogicAdapter, EmptyTileContainer<TileEntityFissionReactorLogicAdapter>> {

    private static final int DISPLAY_COUNT = 4;

    private GuiScrollBar scrollBar;

    public GuiFissionReactorLogicAdapter(EmptyTileContainer<TileEntityFissionReactorLogicAdapter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiElementHolder(this, 16, 31, 130, 90));
        addButton(scrollBar = new GuiScrollBar(this, 146, 31, 90, () -> tile.getModes().length, () -> DISPLAY_COUNT));
        for (int i = 0; i < DISPLAY_COUNT; i++) {
            int typeShift = 22 * i;
            addButton(new ReactorLogicButton<>(this, guiLeft + 17, guiTop + 32 + typeShift, i, tile, scrollBar::getCurrentSelection, tile::getModes, type -> {
                if (type == null) {
                    return;
                }
                MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.LOGIC_TYPE, tile, type.ordinal()));
            }));
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawTextScaledBound(matrix, GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(tile.logicType.getColor(), tile.logicType), 16, 123, titleTextColor(), 144);
        drawCenteredText(matrix, MekanismLang.STATUS.translate(EnumColor.RED, tile.getStatus()), 0, xSize, 136, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }
}
