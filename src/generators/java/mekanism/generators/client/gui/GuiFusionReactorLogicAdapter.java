package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.generators.client.gui.element.button.ReactorLogicButton;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFusionReactorLogicAdapter extends GuiMekanismTile<TileEntityFusionReactorLogicAdapter, EmptyTileContainer<TileEntityFusionReactorLogicAdapter>> {

    private static final int DISPLAY_COUNT = 4;

    private GuiScrollBar scrollBar;

    public GuiFusionReactorLogicAdapter(EmptyTileContainer<TileEntityFusionReactorLogicAdapter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiElementHolder(this, 16, 31, 130, 90));
        addButton(new MekanismImageButton(this, guiLeft + 16, guiTop + 19, 11, 18, getButtonLocation("toggle"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, tile)), getOnHover(GeneratorsLang.REACTOR_LOGIC_TOGGLE_COOLING)));
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
        drawTextScaledBound(matrix, GeneratorsLang.REACTOR_LOGIC_ACTIVE_COOLING.translate(EnumColor.RED, OnOff.of(tile.activeCooled)), 29, 20, titleTextColor(), 117);
        drawTextScaledBound(matrix, GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(EnumColor.RED, tile.logicType), 16, 123, titleTextColor(), 144);
        drawCenteredText(matrix, MekanismLang.STATUS.translate(EnumColor.RED, tile.checkMode() ? GeneratorsLang.REACTOR_LOGIC_OUTPUTTING : MekanismLang.IDLE),
              0, xSize, 136, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }
}