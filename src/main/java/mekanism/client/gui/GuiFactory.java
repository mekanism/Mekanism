package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.api.block.FactoryType;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRecipeType;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiSortingTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiFactory extends GuiMekanismTile<TileEntityFactory, FactoryContainer> {

    public GuiFactory(FactoryContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 11;
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = tileEntity.tier.guiLocation;
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiUpgradeTab(this, tileEntity, resource));
        addButton(new GuiRecipeType(this, tileEntity, resource));
        addButton(new GuiSideConfigurationTab(this, tileEntity, resource));
        addButton(new GuiTransporterConfigTab(this, tileEntity, resource));
        addButton(new GuiSortingTab(this, tileEntity, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.using"), ": ", EnergyDisplay.of(tileEntity.lastUsage), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.needed"), ": ", EnergyDisplay.of(tileEntity.getNeededEnergy()))
        ), this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 93) + 2, 0x404040);
        //TODO: 1.14 Convert to GuiElement
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69) {
            displayTooltip(EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy()).getTextComponent(), xAxis, yAxis);
        } else if (xAxis >= 8 && xAxis <= 168 && yAxis >= 78 && yAxis <= 83) {
            if (tileEntity instanceof TileEntityItemStackGasToItemStackFactory) {
                TileEntityItemStackGasToItemStackFactory itemGasToItemFactory = (TileEntityItemStackGasToItemStackFactory) tileEntity;
                GasTank gasTank = itemGasToItemFactory.gasTank;
                if (gasTank.isEmpty()) {
                    displayTooltip(TextComponentUtil.translate("gui.mekanism.none"), xAxis, yAxis);
                } else {
                    displayTooltip(TextComponentUtil.build(gasTank.getStack(), ": " + gasTank.getStored()), xAxis, yAxis);
                }
            } else if (tileEntity.getFactoryType() == FactoryType.INFUSING) {
                if (tileEntity.infusionTank.isEmpty()) {
                    displayTooltip(TextComponentUtil.translate("gui.mekanism.empty"), xAxis, yAxis);
                } else {
                    displayTooltip(TextComponentUtil.build(tileEntity.infusionTank.getType(), ": " + tileEntity.infusionTank.getStored()), xAxis, yAxis);
                }
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedRect(guiLeft + 165, guiTop + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        int xOffset = tileEntity.tier == FactoryTier.BASIC ? 59 : tileEntity.tier == FactoryTier.ADVANCED ? 39 : 33;
        int xDistance = tileEntity.tier == FactoryTier.BASIC ? 38 : tileEntity.tier == FactoryTier.ADVANCED ? 26 : 19;

        for (int i = 0; i < tileEntity.tier.processes; i++) {
            int xPos = xOffset + (i * xDistance);
            displayInt = tileEntity.getScaledProgress(20, i);
            drawTexturedRect(guiLeft + xPos, guiTop + 33, 176, 52, 8, displayInt);
        }

        if (tileEntity instanceof TileEntityItemStackGasToItemStackFactory) {
            TileEntityItemStackGasToItemStackFactory itemGasToItemFactory = (TileEntityItemStackGasToItemStackFactory) tileEntity;
            int scaledGas = itemGasToItemFactory.getScaledGasLevel(160);
            if (scaledGas > 0) {
                GasStack gas = itemGasToItemFactory.gasTank.getStack();
                if (!gas.isEmpty()) {
                    MekanismRenderer.color(gas);
                    displayGauge(8, 78, scaledGas, 5, gas.getType().getSprite());
                    MekanismRenderer.resetColor();
                }
            }
        } else if (tileEntity.getFactoryType() == FactoryType.INFUSING) {
            if (tileEntity.getScaledInfuseLevel(160) > 0) {
                MekanismRenderer.color(tileEntity.infusionTank.getType().getTint());
                displayGauge(8, 78, tileEntity.getScaledInfuseLevel(160), 5, tileEntity.infusionTank.getType().getSprite());
                MekanismRenderer.resetColor();
            }
        }
    }

    public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, TextureAtlasSprite icon) {
        if (icon != null) {
            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            drawTexturedRectFromIcon(guiLeft + xPos, guiTop + yPos, icon, sizeX, sizeY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 || InputMappings.isKeyDown(minecraft.mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            double xAxis = mouseX - guiLeft;
            double yAxis = mouseY - guiTop;
            if (xAxis > 8 && xAxis < 168 && yAxis > 78 && yAxis < 83) {
                ItemStack stack = minecraft.player.inventory.getItemStack();
                if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                    Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1)));
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                }
            }
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return tileEntity.tier.guiLocation;
    }
}