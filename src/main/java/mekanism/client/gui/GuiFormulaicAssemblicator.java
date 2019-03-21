package mekanism.client.gui;

import java.io.IOException;
import java.util.Arrays;
import mekanism.api.Coord4D;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSideConfigurationTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiTransporterConfigTab;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiFormulaicAssemblicator extends GuiMekanismTile<TileEntityFormulaicAssemblicator> {

    public GuiFormulaicAssemblicator(InventoryPlayer inventory, TileEntityFormulaicAssemblicator tile) {
        super(tile, new ContainerFormulaicAssemblicator(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSideConfigurationTab(this, tileEntity, resource));
        addGuiElement(new GuiTransporterConfigTab(this, 34, tileEntity, resource));
        addGuiElement(new GuiPowerBar(this, tileEntity, resource, 159, 15));
        addGuiElement(new GuiEnergyInfo(() -> {
            String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
            return Arrays.asList(LangUtils.localize("gui.using") + ": " + multiplier + "/t",
                  LangUtils.localize("gui.needed") + ": " + MekanismUtils
                        .getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
        }, this, resource));
        addGuiElement(new GuiSlot(SlotType.POWER, this, resource, 151, 75).with(SlotOverlay.POWER));
        ySize += 64;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 44 && xAxis <= 60 && yAxis >= 75 && yAxis <= 91) {
            drawHoveringText(LangUtils.localize("gui.fillEmpty"), xAxis, yAxis);
        }
        if (xAxis >= 7 && xAxis <= 21 && yAxis >= 45 && yAxis <= 59) {
            drawHoveringText(LangUtils.localize("gui.encodeFormula"), xAxis, yAxis);
        }
        if (xAxis >= 71 && xAxis <= 87 && yAxis >= 75 && yAxis <= 91) {
            drawHoveringText(LangUtils.localize("gui.craftSingle"), xAxis, yAxis);
        }
        if (xAxis >= 89 && xAxis <= 105 && yAxis >= 75 && yAxis <= 91) {
            drawHoveringText(LangUtils.localize("gui.craftAvailable"), xAxis, yAxis);
        }
        if (xAxis >= 107 && xAxis <= 123 && yAxis >= 75 && yAxis <= 91) {
            drawHoveringText(LangUtils.localize("gui.autoModeToggle") + ": " +
                  LangUtils.transOnOff(tileEntity.autoMode), xAxis, yAxis);
        }
        if (xAxis >= 26 && xAxis <= 42 && yAxis >= 75 && yAxis <= 91) {
            drawHoveringText(LangUtils.localize("gui.stockControl") + ": " +
                  LangUtils.transOnOff(tileEntity.stockControl), xAxis, yAxis);
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
        int xAxis = mouseX - guiWidth;
        int yAxis = mouseY - guiHeight;
        if (!tileEntity.autoMode) {
            if (xAxis >= 44 && xAxis <= 60 && yAxis >= 75 && yAxis <= 91) {
                drawTexturedModalRect(guiWidth + 44, guiHeight + 75, 176 + 62, 0, 16, 16);
            } else {
                drawTexturedModalRect(guiWidth + 44, guiHeight + 75, 176 + 62, 16, 16, 16);
            }
        } else {
            drawTexturedModalRect(guiWidth + 44, guiHeight + 75, 176 + 62, 32, 16, 16);
        }
        if (!tileEntity.autoMode && tileEntity.isRecipe) {
            if (canEncode()) {
                if (xAxis >= 7 && xAxis <= 21 && yAxis >= 45 && yAxis <= 59) {
                    drawTexturedModalRect(guiWidth + 7, guiHeight + 45, 176, 0, 14, 14);
                } else {
                    drawTexturedModalRect(guiWidth + 7, guiHeight + 45, 176, 14, 14, 14);
                }
            } else {
                drawTexturedModalRect(guiWidth + 7, guiHeight + 45, 176, 28, 14, 14);
            }

            if (xAxis >= 71 && xAxis <= 87 && yAxis >= 75 && yAxis <= 91) {
                drawTexturedModalRect(guiWidth + 71, guiHeight + 75, 176 + 14, 0, 16, 16);
            } else {
                drawTexturedModalRect(guiWidth + 71, guiHeight + 75, 176 + 14, 16, 16, 16);
            }

            if (xAxis >= 89 && xAxis <= 105 && yAxis >= 75 && yAxis <= 91) {
                drawTexturedModalRect(guiWidth + 89, guiHeight + 75, 176 + 30, 0, 16, 16);
            } else {
                drawTexturedModalRect(guiWidth + 89, guiHeight + 75, 176 + 30, 16, 16, 16);
            }
        } else {
            drawTexturedModalRect(guiWidth + 7, guiHeight + 45, 176, 28, 14, 14);
            drawTexturedModalRect(guiWidth + 71, guiHeight + 75, 176 + 14, 32, 16, 16);
            drawTexturedModalRect(guiWidth + 89, guiHeight + 75, 176 + 30, 32, 16, 16);
        }

        if (tileEntity.formula != null) {
            if (xAxis >= 107 && xAxis <= 123 && yAxis >= 75 && yAxis <= 91) {
                drawTexturedModalRect(guiWidth + 107, guiHeight + 75, 176 + 46, 0, 16, 16);
            } else {
                drawTexturedModalRect(guiWidth + 107, guiHeight + 75, 176 + 46, 16, 16, 16);
            }

            if (xAxis >= 26 && xAxis <= 42 && yAxis >= 75 && yAxis <= 91) {
                drawTexturedModalRect(guiWidth + 26, guiHeight + 75, 176 + 62, 48, 16, 16);
            } else {
                drawTexturedModalRect(guiWidth + 26, guiHeight + 75, 176 + 62, 48 + 16, 16, 16);
            }
        } else {
            drawTexturedModalRect(guiWidth + 107, guiHeight + 75, 176 + 46, 32, 16, 16);
            drawTexturedModalRect(guiWidth + 26, guiHeight + 75, 176 + 62, 48 + 32, 16, 16);
        }

        if (tileEntity.operatingTicks > 0) {
            int display = (int) ((double) tileEntity.operatingTicks * 22 / (double) tileEntity.ticksRequired);
            drawTexturedModalRect(guiWidth + 86, guiHeight + 43, 176, 48, display, 16);
        }

        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSlot.png"));
        drawTexturedModalRect(guiWidth + 90, guiHeight + 25, tileEntity.isRecipe ? 2 : 20, 39, 14, 12);

        if (tileEntity.formula != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = tileEntity.formula.input.get(i);

                if (!stack.isEmpty()) {
                    Slot slot = inventorySlots.inventorySlots.get(i + 20);
                    GlStateManager.pushMatrix();

                    if (slot.getStack().isEmpty() || !slot.getStack().isItemEqual(stack)) {
                        drawGradientRect(guiWidth + slot.xPos, guiHeight + slot.yPos, guiWidth + slot.xPos + 16,
                              guiHeight + slot.yPos + 16, -2137456640, -2137456640);
                    }

                    RenderHelper.enableGUIStandardItemLighting();
                    itemRender.renderItemAndEffectIntoGUI(stack, guiWidth + slot.xPos, guiHeight + slot.yPos);
                    MekanismRenderer.resetColor();
                    GlStateManager.popMatrix();
                }
            }
        }

        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    private boolean canEncode() {
        return tileEntity.formula == null && !tileEntity.inventory.get(2).isEmpty() && tileEntity.inventory.get(2)
              .getItem() instanceof ItemCraftingFormula &&
              ((ItemCraftingFormula) tileEntity.inventory.get(2).getItem()).getInventory(tileEntity.inventory.get(2))
                    == null;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            int xAxis = (mouseX - (width - xSize) / 2);
            int yAxis = (mouseY - (height - ySize) / 2);

            if (!tileEntity.autoMode) {
                if (xAxis >= 44 && xAxis <= 60 && yAxis >= 75 && yAxis <= 91) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                    TileNetworkList data = TileNetworkList.withContents(4);

                    Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                }

                if (tileEntity.isRecipe) {
                    if (canEncode()) {
                        if (xAxis >= 7 && xAxis <= 21 && yAxis >= 45 && yAxis <= 59) {
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                            TileNetworkList data = TileNetworkList.withContents(1);

                            Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                        }
                    }

                    if (xAxis >= 71 && xAxis <= 87 && yAxis >= 75 && yAxis <= 91) {
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                        TileNetworkList data = TileNetworkList.withContents(2);

                        Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                    }

                    if (xAxis >= 89 && xAxis <= 105 && yAxis >= 75 && yAxis <= 91) {
                        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                        TileNetworkList data = TileNetworkList.withContents(3);

                        Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                    }
                }
            }

            if (tileEntity.formula != null) {
                if (xAxis >= 107 && xAxis <= 123 && yAxis >= 75 && yAxis <= 91) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                    TileNetworkList data = TileNetworkList.withContents(0);

                    Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                }

                if (xAxis >= 26 && xAxis <= 42 && yAxis >= 75 && yAxis <= 91) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);

                    TileNetworkList data = TileNetworkList.withContents(5);

                    Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                }
            }
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiFormulaicAssemblicator.png");
    }
}