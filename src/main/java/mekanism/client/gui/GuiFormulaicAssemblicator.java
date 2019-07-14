package mekanism.client.gui;

import java.io.IOException;
import java.util.Arrays;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.GuiButtonImageMek;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFormulaicAssemblicator extends GuiMekanismTile<TileEntityFormulaicAssemblicator> {

    private GuiButtonImageMek encodeFormulaButton;
    private GuiButtonImageMek stockControlButton;
    private GuiButtonImageMek fillEmptyButton;
    private GuiButtonImageMek craftSingleButton;
    private GuiButtonImageMek craftAvailableButton;
    private GuiButtonImageMek autoModeButton;

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
                  LangUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
        }, this, resource));
        addGuiElement(new GuiSlot(SlotType.POWER, this, resource, 151, 75).with(SlotOverlay.POWER));
        ySize += 64;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(encodeFormulaButton = new GuiButtonImageMek(0, guiLeft + 7, guiTop + 45, 14, 14, 176, 14, -14, 14, getGuiLocation()));
        buttonList.add(stockControlButton = new GuiButtonImageMek(1, guiLeft + 26, guiTop + 75, 16, 16, 238, 48 + 16, -16, 16, getGuiLocation()));
        buttonList.add(fillEmptyButton = new GuiButtonImageMek(2, guiLeft + 44, guiTop + 75, 16, 16, 238, 16, -16, 16, getGuiLocation()));
        buttonList.add(craftSingleButton = new GuiButtonImageMek(3, guiLeft + 71, guiTop + 75, 16, 16, 190, 16, -16, 16, getGuiLocation()));
        buttonList.add(craftAvailableButton = new GuiButtonImageMek(4, guiLeft + 89, guiTop + 75, 16, 16, 206, 16, -16, 16, getGuiLocation()));
        buttonList.add(autoModeButton = new GuiButtonImageMek(5, guiLeft + 107, guiTop + 75, 16, 16, 222, 16, -16, 16, getGuiLocation()));
        updateEnabledButtons();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == encodeFormulaButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(1)));
        } else if (guibutton.id == stockControlButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(5)));
        } else if (guibutton.id == fillEmptyButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(4)));
        } else if (guibutton.id == craftSingleButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(2)));
        } else if (guibutton.id == craftAvailableButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(3)));
        } else if (guibutton.id == autoModeButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(0)));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        encodeFormulaButton.enabled = !tileEntity.autoMode && tileEntity.isRecipe && canEncode();
        stockControlButton.enabled = tileEntity.formula != null;
        fillEmptyButton.enabled = !tileEntity.autoMode;
        craftSingleButton.enabled = !tileEntity.autoMode && tileEntity.isRecipe;
        craftAvailableButton.enabled = !tileEntity.autoMode && tileEntity.isRecipe;
        autoModeButton.enabled = tileEntity.formula != null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (fillEmptyButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.fillEmpty"), xAxis, yAxis);
        } else if (encodeFormulaButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.encodeFormula"), xAxis, yAxis);
        } else if (craftSingleButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.craftSingle"), xAxis, yAxis);
        } else if (craftAvailableButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.craftAvailable"), xAxis, yAxis);
        } else if (autoModeButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.autoModeToggle") + ": " + LangUtils.transOnOff(tileEntity.autoMode), xAxis, yAxis);
        } else if (stockControlButton.isMouseOver()) {
            drawHoveringText(LangUtils.localize("gui.stockControl") + ": " + LangUtils.transOnOff(tileEntity.stockControl), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        if (tileEntity.operatingTicks > 0) {
            int display = (int) ((double) tileEntity.operatingTicks * 22 / (double) tileEntity.ticksRequired);
            drawTexturedModalRect(guiLeft + 86, guiTop + 43, 176, 48, display, 16);
        }

        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSlot.png"));
        drawTexturedModalRect(guiLeft + 90, guiTop + 25, tileEntity.isRecipe ? 2 : 20, 39, 14, 12);

        if (tileEntity.formula != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = tileEntity.formula.input.get(i);
                if (!stack.isEmpty()) {
                    Slot slot = inventorySlots.inventorySlots.get(i + 20);
                    int guiX = guiLeft + slot.xPos;
                    int guiY = guiTop + slot.yPos;
                    if (slot.getStack().isEmpty() || !tileEntity.formula.isIngredientInPos(tileEntity.getWorld(), slot.getStack(), i)) {
                        drawColorIcon(guiX, guiY, EnumColor.DARK_RED, 0.8F);
                    }
                    renderItem(stack, guiX, guiY);
                }
            }
        }
    }

    private boolean canEncode() {
        if (tileEntity.formula != null) {
            return false;
        }
        ItemStack formulaStack = tileEntity.inventory.get(TileEntityFormulaicAssemblicator.SLOT_FORMULA);
        return !formulaStack.isEmpty() && formulaStack.getItem() instanceof ItemCraftingFormula && ((ItemCraftingFormula) formulaStack.getItem()).getInventory(formulaStack) == null;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiFormulaicAssemblicator.png");
    }
}