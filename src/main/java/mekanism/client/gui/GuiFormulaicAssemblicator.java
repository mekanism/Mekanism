package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiFormulaicAssemblicator extends GuiMekanismTile<TileEntityFormulaicAssemblicator, MekanismTileContainer<TileEntityFormulaicAssemblicator>> {

    private MekanismButton encodeFormulaButton;
    private MekanismButton stockControlButton;
    private MekanismButton fillEmptyButton;
    private MekanismButton craftSingleButton;
    private MekanismButton craftAvailableButton;
    private MekanismButton autoModeButton;

    public GuiFormulaicAssemblicator(MekanismTileContainer<TileEntityFormulaicAssemblicator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile, 159, 15));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(tile.getEnergyPerTick())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(tile.getNeededEnergy()))), this));
        addButton(new GuiSlot(SlotType.POWER, this, 151, 75).with(SlotOverlay.POWER));

        addButton(encodeFormulaButton = new MekanismImageButton(this, getGuiLeft() + 7, getGuiTop() + 45, 14, getButtonLocation("encode_formula"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1))),
              getOnHover(MekanismLang.ENCODE_FORMULA)));
        addButton(stockControlButton = new MekanismImageButton(this, getGuiLeft() + 26, getGuiTop() + 75, 16, getButtonLocation("stock_control"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(5))),
              getOnHover(() -> MekanismLang.STOCK_CONTROL.translate(OnOff.of(tile.stockControl)))));
        addButton(fillEmptyButton = new MekanismImageButton(this, getGuiLeft() + 44, getGuiTop() + 75, 16, getButtonLocation("fill_empty"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(4))),
              getOnHover(MekanismLang.FILL_EMPTY)));
        addButton(craftSingleButton = new MekanismImageButton(this, getGuiLeft() + 71, getGuiTop() + 75, 16, getButtonLocation("craft_single"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(2))),
              getOnHover(MekanismLang.CRAFT_SINGLE)));
        addButton(craftAvailableButton = new MekanismImageButton(this, getGuiLeft() + 89, getGuiTop() + 75, 16, getButtonLocation("craft_available"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(3))),
              getOnHover(MekanismLang.CRAFT_AVAILABLE)));
        addButton(autoModeButton = new MekanismImageButton(this, getGuiLeft() + 107, getGuiTop() + 75, 16, getButtonLocation("auto_toggle"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0))),
              getOnHover(() -> MekanismLang.AUTO_MODE.translate(OnOff.of(tile.autoMode)))));
        updateEnabledButtons();
    }

    @Override
    public void tick() {
        super.tick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        encodeFormulaButton.active = !tile.autoMode && tile.isRecipe && canEncode();
        stockControlButton.active = tile.formula != null;
        fillEmptyButton.active = !tile.autoMode;
        craftSingleButton.active = !tile.autoMode && tile.isRecipe;
        craftAvailableButton.active = !tile.autoMode && tile.isRecipe;
        autoModeButton.active = tile.formula != null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tile.operatingTicks > 0) {
            int display = (int) ((double) tile.operatingTicks * 22 / (double) tile.ticksRequired);
            drawTexturedRect(getGuiLeft() + 86, getGuiTop() + 43, 176, 48, display, 16);
        }

        minecraft.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "slot.png"));
        drawTexturedRect(getGuiLeft() + 90, getGuiTop() + 25, tile.isRecipe ? 2 : 20, 39, 14, 12);

        if (tile.formula != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = tile.formula.input.get(i);
                if (!stack.isEmpty()) {
                    Slot slot = container.inventorySlots.get(i + 20);
                    int guiX = getGuiLeft() + slot.xPos;
                    int guiY = getGuiTop() + slot.yPos;
                    if (slot.getStack().isEmpty() || !tile.formula.isIngredientInPos(tile.getWorld(), slot.getStack(), i)) {
                        drawColorIcon(guiX, guiY, EnumColor.DARK_RED, 0.8F);
                        //Only render the "correct" item in the gui slot if we don't already have that item there
                        renderItem(stack, guiX, guiY);
                    }
                }
            }
        }
    }

    private boolean canEncode() {
        if (tile.formula != null || tile.getFormulaSlot().isEmpty()) {
            return false;
        }
        ItemStack formulaStack = tile.getFormulaSlot().getStack();
        return formulaStack.getItem() instanceof ItemCraftingFormula && ((ItemCraftingFormula) formulaStack.getItem()).getInventory(formulaStack) == null;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "formulaic_assemblicator.png");
    }
}