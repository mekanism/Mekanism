package mekanism.client.gui.machine;

import java.util.function.Supplier;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GuiAntiprotonicNucleosynthesizer extends GuiConfigurableTile<TileEntityAntiprotonicNucleosynthesizer,
      MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> {

    private static final Vec3 from = new Vec3(47, 50, 0), to = new Vec3(147, 50, 0);
    private static final BoltRenderInfo boltRenderInfo = new BoltRenderInfo().color(Color.rgbad(0.45F, 0.45F, 0.5F, 1));

    private final BoltRenderer bolt = new BoltRenderer();
    private final Supplier<BoltEffect> boltSupplier = () -> new BoltEffect(boltRenderInfo, from, to, 15)
          .count(Math.min(Mth.ceil(tile.getProcessRate() / 8F), 20))
          .size(1)
          .lifespan(1)
          .spawn(SpawnFunction.CONSECUTIVE)
          .fade(FadeFunction.NONE);
    private GuiInnerScreen screen;

    public GuiAntiprotonicNucleosynthesizer(MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer> container, Inventory inv, Component title) {
        super(container, inv, title, DEFAULT_IMAGE_WIDTH + 20, DEFAULT_IMAGE_HEIGHT + 27);
        dynamicSlots = true;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        screen = addRenderableWidget(new GuiInnerScreen(this, 45, 18, 104, 68).recipeViewerCategory(tile));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.gasTank, () -> tile.getChemicalTanks(null), GaugeType.SMALL_MED, this, 5, 18))
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
    protected void drawForegroundText(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        screen.drawScrollingString(guiGraphics, MekanismLang.PROCESS_RATE.translate(TextUtils.getPercent(tile.getProcessRate())), 0,
              screen.getHeight() - font().lineHeight - 2, TextAlignment.CENTER, screenTextColor(), 2, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        //TODO - 26.1: GuiGraphics.pose() now returns Matrix3x2fStack (2D only), and GuiGraphicsExtractor
        // no longer exposes bufferSource(). GUI custom 3D rendering needs architectural rework.
        /*PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 100);
        MultiBufferSource.BufferSource renderer = guiGraphics.bufferSource();
        float partialTicks = MekanismRenderer.getPartialTick();
        bolt.update(this, boltSupplier.get(), partialTicks);
        bolt.render(Minecraft.getInstance().level.getGameTime(), partialTicks, pose, renderer);
        renderer.endBatch(MekanismRenderType.MEK_LIGHTNING);
        pose.popPose();*/
    }
}