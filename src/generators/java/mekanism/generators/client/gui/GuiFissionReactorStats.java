package mekanism.generators.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.InputValidator;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.gui.element.GuiFissionReactorTab;
import mekanism.generators.client.gui.element.GuiFissionReactorTab.FissionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract.GeneratorsGuiInteraction;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFissionReactorStats extends GuiMekanismTile<TileEntityFissionReactorCasing, EmptyTileContainer<TileEntityFissionReactorCasing>> {

    private GuiTextField rateLimitField;

    public GuiFissionReactorStats(EmptyTileContainer<TileEntityFissionReactorCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiFissionReactorTab(this, tile, FissionReactorTab.MAIN));
        addButton(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return GeneratorsLang.GAS_BURN_RATE.translate(tile.getMultiblock().lastBurnRate);
            }

            @Override
            public double getLevel() {
                FissionReactorMultiblockData multiblock = tile.getMultiblock();
                return Math.min(1, multiblock.lastBurnRate / multiblock.getMaxBurnRate());
            }
        }, 5, 114, imageWidth - 12));
        rateLimitField = addButton(new GuiTextField(this, 77, 128, 49, 12));
        rateLimitField.setEnterHandler(this::setRateLimit);
        rateLimitField.setInputValidator(InputValidator.DECIMAL);
        rateLimitField.setMaxStringLength(4);
        rateLimitField.addCheckmarkButton(this::setRateLimit);
    }

    private void setRateLimit() {
        if (!rateLimitField.getText().isEmpty()) {
            try {
                double limit = Double.parseDouble(rateLimitField.getText());
                if (limit >= 0 && limit <= tile.getMultiblock().getMaxBurnRate()) {
                    // round to two decimals
                    limit = UnitDisplayUtils.roundDecimals(limit);
                    MekanismGenerators.packetHandler.sendToServer(new PacketGeneratorsGuiInteract(GeneratorsGuiInteraction.INJECTION_RATE, tile, limit));
                    rateLimitField.setText("");
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.FISSION_REACTOR_STATS.translate(), titleLabelY);
        FissionReactorMultiblockData multiblock = tile.getMultiblock();
        // heat stats
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_HEAT_STATISTICS.translate(), 6, 20, headingTextColor(), imageWidth - 12);
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_HEAT_CAPACITY.translate(TextUtils.format((long) multiblock.heatCapacitor.getHeatCapacity())), 6, 32, titleTextColor(), imageWidth - 12);
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_SURFACE_AREA.translate(TextUtils.format(multiblock.surfaceArea)), 6, 42, titleTextColor(), imageWidth - 12);
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_BOIL_EFFICIENCY.translate(tile.getBoilEfficiency()), 6, 52, titleTextColor(), imageWidth - 12);
        // fuel stats
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_FUEL_STATISTICS.translate(), 6, 68, headingTextColor(), imageWidth - 12);
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_MAX_BURN_RATE.translate(TextUtils.format(multiblock.getMaxBurnRate())), 6, 80, titleTextColor(), imageWidth - 12);
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_RATE_LIMIT.translate(multiblock.rateLimit), 6, 90, titleTextColor(), imageWidth - 12);
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_CURRENT_BURN_RATE.translate(), 6, 104, titleTextColor(), imageWidth - 12);
        drawTextScaledBound(matrix, GeneratorsLang.FISSION_SET_RATE_LIMIT.translate(), 6, 130, titleTextColor(), 69);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}