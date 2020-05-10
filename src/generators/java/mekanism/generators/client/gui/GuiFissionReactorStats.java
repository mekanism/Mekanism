package mekanism.generators.client.gui;

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
                return Math.min(1, tile.getLastBurnRate() / tile.getMaxBurnRate());
            }
        }, 5, 114, xSize - 12));
        addButton(new MekanismImageButton(this, getGuiLeft() + 114, getGuiTop() + 128, 11, 12, getButtonLocation("checkmark"), this::setRateLimit));
        addButton(rateLimitField = new TextFieldWidget(font, getGuiLeft() + 77, getGuiTop() + 128, 36, 11, ""));
        rateLimitField.setMaxStringLength(4);
    }

    private void setRateLimit() {
        if (!rateLimitField.getText().isEmpty()) {
            try {
                double limit = Double.parseDouble(rateLimitField.getText());
                if (limit >= 0 && limit < 10_000) {
                    // round to two decimals
                    limit = (double) Math.round(limit * 100) / 100;
                    MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.INJECTION_RATE, tile, limit));
                    rateLimitField.setText("");
                }
            } catch(Exception e) {} // ignore NFE
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(GeneratorsLang.FISSION_REACTOR_STATS.translate(), 6);
        // heat stats
        drawTextScaledBound(GeneratorsLang.FISSION_HEAT_STATISTICS.translate(), 6, 20, 0x202020, xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_HEAT_CAPACITY.translate(formatInt(tile.getHeatCapacity())), 6, 32, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_SURFACE_AREA.translate(formatInt(tile.getSurfaceArea())), 6, 42, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_BOIL_EFFICIENCY.translate(tile.getBoilEfficiency()), 6, 52, titleTextColor(), xSize - 12);
        // fuel stats
        drawTextScaledBound(GeneratorsLang.FISSION_FUEL_STATISTICS.translate(), 6, 68, 0x202020, xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_MAX_BURN_RATE.translate(formatInt(tile.getMaxBurnRate())), 6, 80, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_RATE_LIMIT.translate(tile.getRateLimit()), 6, 90, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_CURRENT_BURN_RATE.translate(), 6, 104, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_SET_RATE_LIMIT.translate(), 6, 130, titleTextColor(), 69);
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
            if (Character.isDigit(c) || c == '.') {
                return rateLimitField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }
}