package mekanism.client.gui.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.FadeFunction;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;

public class GuiAntiprotonicNucleosynthesizer extends GuiConfigurableTile<TileEntityAntiprotonicNucleosynthesizer,
      MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> {

    private static final Vec3 from = new Vec3(47, 50, 0), to = new Vec3(147, 50, 0);
    private static final BoltRenderInfo boltRenderInfo = new BoltRenderInfo().color(Color.rgbad(0.45F, 0.45F, 0.5F, 1));

    private final BoltRenderer bolt = new BoltRenderer();
    private final Supplier<BoltEffect> boltSupplier = () -> new BoltEffect(boltRenderInfo, from, to, 15)
          .count((int) Math.min(Math.ceil(tile.getProcessRate() / 8F), 20))
          .size(1)
          .lifespan(1)
          .spawn(SpawnFunction.CONSECUTIVE)
          .fade(FadeFunction.NONE);

    public GuiAntiprotonicNucleosynthesizer(MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 27;
        imageWidth += 20;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 45, 18, 104, 68).jeiCategory(tile));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
        addRenderableWidget(new GuiGasGauge(() -> tile.gasTank, () -> tile.getGasTanks(null), GaugeType.SMALL_MED, this, 5, 18))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT));
        addRenderableWidget(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.SMALL_MED, this, 172, 18))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY));
        addRenderableWidget(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(tile.getScaledProgress()));
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getScaledProgress());
            }
        }, 5, 88, 183, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        drawString(matrix, title, (imageWidth - getStringWidth(title)) / 2, titleLabelY, titleTextColor());
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        drawTextScaledBound(matrix, MekanismLang.PROCESS_RATE.translate(TextUtils.getPercent(tile.getProcessRate())), 48, 76, screenTextColor(), 100);
        super.drawForegroundText(matrix, mouseX, mouseY);
        matrix.pushPose();
        matrix.translate(0, 0, 100);
        MultiBufferSource.BufferSource renderer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        bolt.update(this, boltSupplier.get(), MekanismRenderer.getPartialTick());
        bolt.render(MekanismRenderer.getPartialTick(), matrix, renderer);
        renderer.endBatch(MekanismRenderType.MEK_LIGHTNING);
        matrix.popPose();
    }
}