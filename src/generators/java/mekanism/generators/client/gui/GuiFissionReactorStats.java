package mekanism.generators.client.gui;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.generators.client.gui.element.GuiFissionReactorTab;
import mekanism.generators.client.gui.element.GuiFissionReactorTab.FissionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFissionReactorStats extends GuiMekanismTile<TileEntityFissionReactorCasing, EmptyTileContainer<TileEntityFissionReactorCasing>> {

    private GuiTextField rateLimitField;

    public GuiFissionReactorStats(EmptyTileContainer<TileEntityFissionReactorCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiFissionReactorTab(this, tile, FissionReactorTab.MAIN));
        func_230480_a_(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return GeneratorsLang.GAS_BURN_RATE.translate(tile.getMultiblock().lastBurnRate);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getMultiblock().lastBurnRate / tile.getMaxBurnRate());
            }
        }, 5, 114, xSize - 12));
        func_230480_a_(rateLimitField = new GuiTextField(this, 77, 128, 49, 12));
        rateLimitField.setEnterHandler(this::setRateLimit);
        rateLimitField.setInputValidator(InputValidator.DECIMAL);
        rateLimitField.setMaxStringLength(4);
        rateLimitField.addCheckmarkButton(this::setRateLimit);
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
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(GeneratorsLang.FISSION_REACTOR_STATS.translate(), 6);
        // heat stats
        drawTextScaledBound(GeneratorsLang.FISSION_HEAT_STATISTICS.translate(), 6, 20, 0x202020, xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_HEAT_CAPACITY.translate(formatInt((int) tile.getMultiblock().heatCapacitor.getHeatCapacity())), 6, 32, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_SURFACE_AREA.translate(formatInt(tile.getMultiblock().surfaceArea)), 6, 42, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_BOIL_EFFICIENCY.translate(tile.getBoilEfficiency()), 6, 52, titleTextColor(), xSize - 12);
        // fuel stats
        drawTextScaledBound(GeneratorsLang.FISSION_FUEL_STATISTICS.translate(), 6, 68, 0x202020, xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_MAX_BURN_RATE.translate(formatInt(tile.getMaxBurnRate())), 6, 80, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_RATE_LIMIT.translate(tile.getMultiblock().rateLimit), 6, 90, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_CURRENT_BURN_RATE.translate(), 6, 104, titleTextColor(), xSize - 12);
        drawTextScaledBound(GeneratorsLang.FISSION_SET_RATE_LIMIT.translate(), 6, 130, titleTextColor(), 69);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}