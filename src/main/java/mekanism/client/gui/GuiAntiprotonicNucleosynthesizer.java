package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar.ColorFunction;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.render.BoltRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Color;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityAntiprotonicNucleosynthesizer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

public class GuiAntiprotonicNucleosynthesizer extends GuiMekanismTile<TileEntityAntiprotonicNucleosynthesizer, MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer>> {

    private static final Vec3d from = new Vec3d(47, 50, 0), to = new Vec3d(146, 50, 0);
    private BoltRenderer bolt = new BoltRenderer().withSize(1F).withColor(0.45F, 0.45F, 0.5F, 1);

    public GuiAntiprotonicNucleosynthesizer(MekanismTileContainer<TileEntityAntiprotonicNucleosynthesizer> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 27;
        xSize += 20;
    }

    @Override
    public void init() {
        super.init();
        // manually run init stuff as we want to render slots over our components
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;

        addButton(new GuiInnerScreen(this, 45, 18, 104, 68));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiEnergyInfo(tile.getEnergyContainer(), this));
        addButton(new GuiGasGauge(() -> tile.gasTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 5, 18));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 178, 18, 46));
        addButton(new GuiDynamicHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.PROGRESS.translate("100%");
            }

            @Override
            public double getLevel() {
                return 1;
            }
        }, 6, 88, xSize - 14, ColorFunction.scale(Color.rgb(60, 45, 74), Color.rgb(100, 30, 170))));
        addSlots();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 3, 0x404040);
        renderScaledText(MekanismLang.PROCESS_RATE.translate("1800%"), 48, 76, 0x00CD00, 100);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        MatrixStack matrix = new MatrixStack();
        matrix.push();
        IRenderTypeBuffer.Impl renderer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        bolt.render(from, to, 4, 12, MekanismRenderer.getPartialTick(), matrix, renderer, MekanismRenderer.FULL_LIGHT);
        renderer.finish(RenderType.getLightning());
        matrix.pop();
    }
}