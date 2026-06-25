package mekanism.client.gui.machine;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiInnerScreen.VerticalPositioning;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.effect.BoltFeatureRenderer.BoltRenderState;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.FadeFunction;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class GuiAntiprotonicNucleosynthesizer extends GuiConfigurableTile<TileEntityAntiprotonicNucleosynthesizer,
      MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> {

    private static final int SCREEN_X = 45;
    private static final int SCREEN_Y = 18;
    private static final int SCREEN_WIDTH = 104;
    private static final int SCREEN_HEIGHT = 68;
    private static final Vector3fc FROM = new Vector3f(SCREEN_X + 2, SCREEN_Y + (SCREEN_HEIGHT - 4) / 2F, 0);
    private static final Vector3fc TO = FROM.add(SCREEN_WIDTH - 4, 0, 0, new Vector3f());
    private static final BoltRenderInfo BOLT_RENDER_INFO = new BoltRenderInfo().color(Color.rgbad(0.45F, 0.45F, 0.5F, 1));

    private final BoltRenderer bolt = new BoltRenderer();
    private final Supplier<BoltEffect> boltSupplier = () -> new BoltEffect(BOLT_RENDER_INFO, FROM, TO, 15)
          .count(Math.min(Mth.ceil(tile.getProcessRate() / 8F), 20))
          .size(1)
          .lifespan(1)
          .spawn(SpawnFunction.CONSECUTIVE)
          .fade(FadeFunction.NONE);

    public GuiAntiprotonicNucleosynthesizer(MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer> container, Inventory inv, Component title) {
        super(container, inv, title, DEFAULT_IMAGE_WIDTH + 20, DEFAULT_IMAGE_HEIGHT + 27);
        dynamicSlots = true;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, SCREEN_X, SCREEN_Y, SCREEN_WIDTH, SCREEN_HEIGHT))
              .text(() -> List.of(MekanismLang.PROCESS_RATE.translate(TextUtils.getPercent(tile.getProcessRate()))))
              .alignment(TextAlignment.CENTER)
              .verticalAlignment(VerticalPositioning.BOTTOM)
              .padding(2)
              .recipeViewerCategory(tile);
        addRenderableWidget(new GuiEnergyTab(this, tile.energyContainer(), tile::getEnergyUsed));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.gasTank, tile::getChemicalTanks, GaugeType.SMALL_MED, this, 5, 18))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT));
        addRenderableWidget(new GuiEnergyGauge(tile.energyContainer(), GaugeType.SMALL_MED, this, 172, 18))
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
        }, 5, 88, 183, Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170)))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        long gameTime = tile.getGameTime();
        float partialTicks = MekanismRenderer.getPartialTick();
        bolt.update(this, boltSupplier.get(), gameTime, partialTicks);
        List<BoltRenderState> boltRenderStates = bolt.collectBoltStates(gameTime, partialTicks);
        if (!boltRenderStates.isEmpty()) {
            guiGraphics.submitGuiElementRenderState(new BoltElementRenderState(guiGraphics, boltRenderStates, Mth.floor(FROM.x()), SCREEN_Y, Mth.ceil(TO.x()), SCREEN_Y + SCREEN_HEIGHT - 4));
        }
    }

    private record BoltElementRenderState(int x0, int y0, int x1, int y1, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds,
                                   List<BoltRenderState> boltRenderStates
    ) implements GuiElementRenderState {

        public BoltElementRenderState(GuiGraphicsExtractor guiGraphics, List<BoltRenderState> boltRenderStates, int x0, int y0, int x1, int y1) {
            Matrix3x2fc pose = new Matrix3x2f(guiGraphics.pose());
            ScreenRectangle scissorArea = guiGraphics.peekScissorStack();
            ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
            this(x0, y0, x1, y1, pose, scissorArea, scissorArea == null ? bounds : scissorArea.intersection(bounds), boltRenderStates);
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            for (BoltRenderState state : boltRenderStates) {
                for (Vector3fc vertex : state.vertices) {
                    vertexConsumer.addVertexWith2DPose(pose, vertex.x(), vertex.y()).setColor(state.color);
                }
            }
        }

        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.LIGHTNING;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }
    }
}