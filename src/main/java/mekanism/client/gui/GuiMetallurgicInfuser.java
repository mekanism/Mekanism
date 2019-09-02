package mekanism.client.gui;

import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseType;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiInfuseBar;
import mekanism.client.gui.element.bar.GuiInfuseBar.InfuseInfoProvider;
import mekanism.client.gui.element.bar.GuiPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.inventory.container.tile.MetallurgicInfuserContainer;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMetallurgicInfuser extends GuiMekanismTile<TileEntityMetallurgicInfuser, MetallurgicInfuserContainer> {

    public GuiMetallurgicInfuser(MetallurgicInfuserContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiUpgradeTab(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiSideConfigurationTab(this, tileEntity, resource));
        addButton(new GuiTransporterConfigTab(this, tileEntity, resource));
        addButton(new GuiPowerBar(this, tileEntity, resource, 164, 15));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tileEntity.getEnergyPerTick()), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
        addButton(new GuiSlot(SlotType.EXTRA, this, resource, 16, 34));
        addButton(new GuiSlot(SlotType.INPUT, this, resource, 50, 42));
        addButton(new GuiSlot(SlotType.POWER, this, resource, 142, 34).with(SlotOverlay.POWER));
        addButton(new GuiSlot(SlotType.OUTPUT, this, resource, 108, 42));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.getScaledProgress();
            }
        }, ProgressBar.MEDIUM, this, resource, 70, 46));
        addButton(new GuiInfuseBar(this, new InfuseInfoProvider() {
            @Nonnull
            @Override
            public TextureAtlasSprite getSprite() {
                return tileEntity.infuseStored.getType().sprite;
            }

            @Override
            public ITextComponent getTooltip() {
                InfuseType type = tileEntity.infuseStored.getType();
                if (type == null) {
                    return TextComponentUtil.translate("gui.mekanism.empty");
                }
                return TextComponentUtil.build(type, ": " + tileEntity.infuseStored.getAmount());
            }

            @Override
            public double getLevel() {
                return (double) tileEntity.infuseStored.getAmount() / (double) TileEntityMetallurgicInfuser.MAX_INFUSE;
            }
        }, resource, 7, 15));
        addButton(new GuiDumpButton(this, tileEntity, resource, 140, 65));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), 45, 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "metallurgic_infuser.png");
    }
}