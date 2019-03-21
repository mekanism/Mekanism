package mekanism.client.gui;

import java.io.IOException;
import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRecipeType;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSideConfigurationTab;
import mekanism.client.gui.element.GuiSortingTab;
import mekanism.client.gui.element.GuiTransporterConfigTab;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.base.IFactory.MachineFuelType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.api.TileNetworkList;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

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
                  LangUtils.localize("gui.needed") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    4, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 93) + 2, 0x404040);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69) {
            drawHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis,
                  yAxis);
        }
        if (xAxis >= 8 && xAxis <= 168 && yAxis >= 78 && yAxis <= 83) {
            if (tileEntity.getRecipeType().getFuelType() == MachineFuelType.ADVANCED) {
                drawHoveringText(
                      tileEntity.gasTank.getGas() != null ? tileEntity.gasTank.getGas().getGas().getLocalizedName()
                            + ": " + tileEntity.gasTank.getStored() : LangUtils.localize("gui.none"), xAxis, yAxis);
            } else if (tileEntity.getRecipeType() == RecipeType.INFUSING) {
                drawHoveringText(
                      tileEntity.infuseStored.type != null ? tileEntity.infuseStored.type.getLocalizedName() + ": "
                            + tileEntity.infuseStored.amount : LangUtils.localize("gui.empty"), xAxis, yAxis);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        int xOffset = tileEntity.tier == FactoryTier.BASIC ? 59 : (tileEntity.tier == FactoryTier.ADVANCED ? 39 : 33);
        int xDistance = tileEntity.tier == FactoryTier.BASIC ? 38 : (tileEntity.tier == FactoryTier.ADVANCED ? 26 : 19);

        for (int i = 0; i < tileEntity.tier.processes; i++) {
            int xPos = xOffset + (i * xDistance);
            displayInt = tileEntity.getScaledProgress(20, i);
            drawTexturedModalRect(guiWidth + xPos, guiHeight + 33, 176, 52, 8, displayInt);
        }

        if (tileEntity.getRecipeType().getFuelType() == MachineFuelType.ADVANCED) {
            if (tileEntity.getScaledGasLevel(160) > 0) {
                GasStack gas = tileEntity.gasTank.getGas();
                if (gas != null) {
                    //TODO: Use GuiGasGauge?
                    int tint = gas.getGas().getTint();
                    if (tint != -1) {
                        MekanismRenderer.color(tint);
                    }
                    displayGauge(8, 78, tileEntity.getScaledGasLevel(160), 5, gas.getGas().getSprite());
                    MekanismRenderer.resetColor();
                }
            }
        } else if (tileEntity.getRecipeType() == RecipeType.INFUSING) {
            if (tileEntity.getScaledInfuseLevel(160) > 0) {
                displayGauge(8, 78, tileEntity.getScaledInfuseLevel(160), 5, tileEntity.infuseStored.type.sprite);
            }
        }
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, TextureAtlasSprite icon) {
        if (icon != null) {
            int guiWidth = (width - xSize) / 2;
            int guiHeight = (height - ySize) / 2;
            mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
            drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, icon, sizeX, sizeY);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if (button == 0 || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            int xAxis = (x - (width - xSize) / 2);
            int yAxis = (y - (height - ySize) / 2);
            if (xAxis > 8 && xAxis < 168 && yAxis > 78 && yAxis < 83) {
                ItemStack stack = mc.player.inventory.getItemStack();
                if (!stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
                    TileNetworkList data = TileNetworkList.withContents(1);
                    Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
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