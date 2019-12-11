package mekanism.generators.client.gui;

import java.text.DecimalFormat;
import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.inventory.container.WindGeneratorContainer;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiWindGenerator extends GuiMekanismTile<TileEntityWindGenerator, WindGeneratorContainer> {

    private final DecimalFormat powerFormat = new DecimalFormat("0.##");

    public GuiWindGenerator(WindGeneratorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tile, resource));
        addButton(new GuiSecurityTab<>(this, tile, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.producing"), ": ",
                    EnergyDisplay.of(tile.getActive() ? MekanismGeneratorsConfig.generators.windGenerationMin.get() * tile.getCurrentMultiplier() : 0), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.maxOutput"), ": ", EnergyDisplay.of(tile.getMaxOutput()), "/t")),
              this, resource));
        addButton(new GuiVerticalPowerBar(this, tile, resource, 164, 15));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        drawString(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent(), 51, 26, 0x00CD00);
        //TODO: Why is this different from how all the other ones do it
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.power"),
              ": " + powerFormat.format(MekanismUtils.convertToDisplay(MekanismGeneratorsConfig.generators.windGenerationMin.get() * tile.getCurrentMultiplier()))),
              51, 35, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.out"), ": ", EnergyDisplay.of(tile.getMaxOutput()), "/t"), 51, 44, 0x00CD00);
        int size = 44;
        if (!tile.getActive()) {
            size += 9;
            String reason = "gui.mekanism.skyBlocked";
            if (tile.isBlacklistDimension()) {
                reason = "gui.mekanism.noWind";
            }
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of(reason)), 51, size, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 20, guiTop + 37, 176, tile.getActive() ? 52 : 64, 12, 12);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "wind_generator.png");
    }
}