package mekanism.generators.client.gui;

import java.text.DecimalFormat;
import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.inventory.container_old.ContainerWindGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiWindGenerator extends GuiMekanismTile<TileEntityWindGenerator, ContainerWindGenerator> {

    private final DecimalFormat powerFormat = new DecimalFormat("0.##");

    public GuiWindGenerator(PlayerInventory inventory, TileEntityWindGenerator tile) {
        super(tile, new ContainerWindGenerator(inventory, tile), inventory);
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("mekanism.gui.producing"), ": ",
                    EnergyDisplay.of(tileEntity.getActive() ? MekanismGeneratorsConfig.generators.windGenerationMin.get() * tileEntity.getCurrentMultiplier() : 0), "/t"),
              TextComponentUtil.build(Translation.of("mekanism.gui.maxOutput"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"))
              , this, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 45, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        drawString(EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy()).getTextComponent(), 51, 26, 0x00CD00);
        //TODO: Why is this different from how all the other ones do it
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.power"),
              ": " + powerFormat.format(MekanismUtils.convertToDisplay(MekanismGeneratorsConfig.generators.windGenerationMin.get() * tileEntity.getCurrentMultiplier()))),
              51, 35, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("mekanism.gui.out"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"), 51, 44, 0x00CD00);
        int size = 44;
        if (!tileEntity.getActive()) {
            size += 9;
            String reason = "gui.skyBlocked";
            if (tileEntity.isBlacklistDimension()) {
                reason = "gui.noWind";
            }
            drawString(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of(reason)), 51, size, 0x00CD00);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 20, guiTop + 37, 176, tileEntity.getActive() ? 52 : 64, 12, 12);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png");
    }
}