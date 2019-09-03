package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
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
import mekanism.generators.common.inventory.container.fuel.BioGeneratorContainer;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBioGenerator extends GuiMekanismTile<TileEntityBioGenerator, BioGeneratorContainer> {

    public GuiBioGenerator(BioGeneratorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.producing"), ": ",
                    EnergyDisplay.of(tileEntity.getActive() ? MekanismGeneratorsConfig.generators.bioGeneration.get() : 0), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.maxOutput"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"))
              , this, resource));
        addButton(new GuiVerticalPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 16, 34));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 142, 34).with(SlotOverlay.POWER));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 45, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        drawString(EnergyDisplay.of(tileEntity.getEnergy()).getTextComponent(), 51, 26, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.bioGenerator.bioFuel"), ": " + tileEntity.bioFuelSlot.fluidStored), 51, 35, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.out"), ": ", EnergyDisplay.of(tileEntity.getMaxOutput()), "/t"), 51, 44, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tileEntity.getScaledFuelLevel(52);
        drawTexturedRect(guiLeft + 7, guiTop + 17 + 52 - displayInt, 176, 52 + 52 - displayInt, 4, displayInt);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "bio_generator.png");
    }
}