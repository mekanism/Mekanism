package mekanism.client.gui;

import java.io.IOException;
import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseType;
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
import mekanism.common.base.IFactory.MachineFuelType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiFactory extends GuiMekanismTile<TileEntityFactory> {

    public GuiFactory(InventoryPlayer inventory, TileEntityFactory tile) {
        super(tile, new ContainerFactory(inventory, tile));
        ySize += 11;
        ResourceLocation resource = tileEntity.tier.guiLocation;
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiRecipeType(this, tileEntity, resource));
        addGuiElement(new GuiSideConfigurationTab(this, tileEntity, resource));
        addGuiElement(new GuiTransporterConfigTab(this, 34, tileEntity, resource));
        addGuiElement(new GuiSortingTab(this, tileEntity, resource));
        addGuiElement(new GuiEnergyInfo(() -> {
            String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.lastUsage);
            return Arrays.asList(LangUtils.localize("gui.using") + ": " + multiplier + "/t",
                  LangUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 4, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 93) + 2, 0x404040);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69) {
            displayTooltip(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis, yAxis);
        } else if (xAxis >= 8 && xAxis <= 168 && yAxis >= 78 && yAxis <= 83) {
            if (tileEntity.getRecipeType().getFuelType() == MachineFuelType.ADVANCED) {
                GasStack gasStack = tileEntity.gasTank.getGas();
                displayTooltip(gasStack != null ? gasStack.getGas().getLocalizedName() + ": " + tileEntity.gasTank.getStored() : LangUtils.localize("gui.none"), xAxis, yAxis);
            } else if (tileEntity.getRecipeType() == RecipeType.INFUSING) {
                InfuseType type = tileEntity.infuseStored.getType();
                displayTooltip(type != null ? type.getLocalizedName() + ": " + tileEntity.infuseStored.getAmount() : LangUtils.localize("gui.empty"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        int displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiLeft + 165, guiTop + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        int xOffset = tileEntity.tier == FactoryTier.BASIC ? 59 : tileEntity.tier == FactoryTier.ADVANCED ? 39 : 33;
        int xDistance = tileEntity.tier == FactoryTier.BASIC ? 38 : tileEntity.tier == FactoryTier.ADVANCED ? 26 : 19;

        for (int i = 0; i < tileEntity.tier.processes; i++) {
            int xPos = xOffset + (i * xDistance);
            displayInt = tileEntity.getScaledProgress(20, i);
            drawTexturedModalRect(guiLeft + xPos, guiTop + 33, 176, 52, 8, displayInt);
        }

        if (tileEntity.getRecipeType().getFuelType() == MachineFuelType.ADVANCED) {
            if (tileEntity.getScaledGasLevel(160) > 0) {
                GasStack gas = tileEntity.gasTank.getGas();
                if (gas != null) {
                    MekanismRenderer.color(gas);
                    displayGauge(8, 78, tileEntity.getScaledGasLevel(160), 5, gas.getGas().getSprite());
                    MekanismRenderer.resetColor();
                }
            }
        } else if (tileEntity.getRecipeType() == RecipeType.INFUSING) {
            if (tileEntity.getScaledInfuseLevel(160) > 0) {
                displayGauge(8, 78, tileEntity.getScaledInfuseLevel(160), 5, tileEntity.infuseStored.getType().sprite);
            }
        }
    }

    public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, TextureAtlasSprite icon) {
        if (icon != null) {
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            drawTexturedModalRect(guiLeft + xPos, guiTop + yPos, icon, sizeX, sizeY);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if (button == 0 || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            int xAxis = x - guiLeft;
            int yAxis = y - guiTop;
            if (xAxis > 8 && xAxis < 168 && yAxis > 78 && yAxis < 83) {
                ItemStack stack = mc.player.inventory.getItemStack();
                if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                    TileNetworkList data = TileNetworkList.withContents(1);
                    Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return tileEntity.tier.guiLocation;
    }
}