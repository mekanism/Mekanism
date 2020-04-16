package mekanism.generators.client.gui;

import java.text.NumberFormat;
import org.lwjgl.glfw.GLFW;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.client.gui.element.GuiFissionReactorTab;
import mekanism.generators.client.gui.element.GuiFissionReactorTab.FissionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFissionReactorStats extends GuiMekanismTile<TileEntityFissionReactorCasing, EmptyTileContainer<TileEntityFissionReactorCasing>> {

    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    private TextFieldWidget rateLimitField;

    public GuiFissionReactorStats(EmptyTileContainer<TileEntityFissionReactorCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiFissionReactorTab(this, tile, FissionReactorTab.MAIN));
        addButton(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return GeneratorsLang.GAS_BURN_RATE.translate(tile.getLastBurnRate());
            }

            @Override
            public double getLevel() {
                return Math.min(1, (double) tile.getLastBurnRate() / (double) tile.getMaxBurnRate());
            }
        }, 5, 114, xSize - 12));
        addButton(new MekanismImageButton(this, getGuiLeft() + 114, getGuiTop() + 128, 11, 12, getButtonLocation("checkmark"), this::setRateLimit));
        addButton(rateLimitField = new TextFieldWidget(font, getGuiLeft() + 77, getGuiTop() + 128, 36, 11, ""));
        rateLimitField.setMaxStringLength(4);
    }

    private void setRateLimit() {
        if (!rateLimitField.getText().isEmpty()) {
            MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.INJECTION_RATE, tile, Integer.parseInt(rateLimitField.getText())));
            rateLimitField.setText("");
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(GeneratorsLang.FISSION_REACTOR_STATS.translate(), 0, getXSize(), 6, 0x404040);
        // heat stats
        renderScaledText(GeneratorsLang.FISSION_HEAT_STATISTICS.translate(), 6, 20, 0x202020, xSize - 12);
        renderScaledText(GeneratorsLang.FISSION_HEAT_CAPACITY.translate(nf.format(tile.getHeatCapacity())), 6, 32, 0x404040, xSize - 12);
        renderScaledText(GeneratorsLang.FISSION_SURFACE_AREA.translate(nf.format(tile.getSurfaceArea())), 6, 42, 0x404040, xSize - 12);
        renderScaledText(GeneratorsLang.FISSION_BOIL_EFFICIENCY.translate(tile.getBoilEfficiency()), 6, 52, 0x404040, xSize - 12);
        // fuel stats
        renderScaledText(GeneratorsLang.FISSION_FUEL_STATISTICS.translate(), 6, 68, 0x202020, xSize - 12);
        renderScaledText(GeneratorsLang.FISSION_MAX_BURN_RATE.translate(nf.format(tile.getMaxBurnRate())), 6, 80, 0x404040, xSize - 12);
        renderScaledText(GeneratorsLang.FISSION_RATE_LIMIT.translate(nf.format(tile.getRateLimit())), 6, 90, 0x404040, xSize - 12);
        renderScaledText(GeneratorsLang.FISSION_CURRENT_BURN_RATE.translate(nf.format(tile.getRateLimit())), 6, 104, 0x404040, xSize - 12);
        renderScaledText(GeneratorsLang.FISSION_SET_RATE_LIMIT.translate(), 6, 130, 0x404040, 69);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (rateLimitField.canWrite()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                rateLimitField.setFocused2(false);
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                setRateLimit();
                return true;
            }
            return rateLimitField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        if (rateLimitField.canWrite()) {
            if (Character.isDigit(c)) {
                //Only allow a subset of characters to be entered into the frequency text box
                return rateLimitField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }
}