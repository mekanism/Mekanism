package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.FadeFunction;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

public class GuiAntiprotonicNucleosynthesizer extends GuiConfigurableTile<TileEntityAntiprotonicNucleosynthesizer, MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> {

    private static final Vector3d from = new Vector3d(47, 50, 0), to = new Vector3d(147, 50, 0);
    private static final BoltRenderInfo boltRenderInfo = new BoltRenderInfo().color(Color.rgbad(0.45F, 0.45F, 0.5F, 1));

    private final BoltRenderer bolt = new BoltRenderer();
    private final Supplier<BoltEffect> boltSupplier = () -> new BoltEffect(boltRenderInfo, from, to, 15)
          .count((int) Math.min(Math.ceil(tile.getProcessRate() / 8F), 20))
          .size(1)
          .lifespan(1)
          .spawn(SpawnFunction.CONSECUTIVE)
          .fade(FadeFunction.NONE);

    public GuiAntiprotonicNucleosynthesizer(MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 27;
        xSize += 20;
        playerInventoryTitleY = ySize - 93;
    }

    @Override
    public void init() {
        super.init();

        addButton(new GuiInnerScreen(this, 45, 18, 104, 68).jeiCategory(tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiEnergyTab(tile.getEnergyContainer(), () -> tile.clientEnergyUsed, this));
        addButton(new GuiGasGauge(() -> tile.gasTank, () -> tile.getGasTanks(null), GaugeType.SMALL_MED, this, 5, 18));
        addButton(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.SMALL_MED, this, 172, 18));
        addButton(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(tile.getScaledProgress()));
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getScaledProgress());
            }
        }, 5, 88, xSize - 12, ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawString(matrix, tile.getName(), (xSize - getStringWidth(tile.getName())) / 2, titleY, titleTextColor());
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        drawTextScaledBound(matrix, MekanismLang.PROCESS_RATE.translate(TextUtils.getPercent(tile.getProcessRate())), 48, 76, screenTextColor(), 100);
        super.drawForegroundText(matrix, mouseX, mouseY);
        matrix.push();
        matrix.translate(0, 0, 100);
        IRenderTypeBuffer.Impl renderer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        bolt.update(this, boltSupplier.get(), MekanismRenderer.getPartialTick());
        bolt.render(MekanismRenderer.getPartialTick(), matrix, renderer);
        renderer.finish(MekanismRenderType.MEK_LIGHTNING);
        matrix.pop();
    }
}