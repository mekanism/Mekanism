package mekanism.client.gui;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
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
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
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
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 159, 15));
        //Overwrite the output slots with a "combined" slot
        addButton(new GuiSlot(SlotType.OUTPUT_LARGE, this, 115, 16));
        addButton(new GuiProgress(() -> tile.operatingTicks / (double) tile.ticksRequired, ProgressType.TALL_RIGHT, this, 86, 43));
        addButton(new GuiEnergyInfo(tile.getEnergyContainer(), this));
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
        stockControlButton.active = tile.formula != null && tile.formula.isValidFormula();
        fillEmptyButton.active = !tile.autoMode;
        craftSingleButton.active = !tile.autoMode && tile.isRecipe;
        craftAvailableButton.active = !tile.autoMode && tile.isRecipe;
        autoModeButton.active = tile.formula != null && tile.formula.isValidFormula();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ItemStack checkValidity(int slotIndex) {
        int i = slotIndex - 19;
        if (i >= 0 && tile.formula != null && tile.formula.isValidFormula()) {
            ItemStack stack = tile.formula.input.get(i);
            if (!stack.isEmpty()) {
                Slot slot = container.inventorySlots.get(slotIndex);
                //Only render the "correct" item in the gui slot if we don't already have that item there
                if (slot.getStack().isEmpty() || !tile.formula.isIngredientInPos(tile.getWorld(), slot.getStack(), i)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        //TODO: Gui element
        SlotOverlay overlay = tile.isRecipe ? SlotOverlay.CHECK : SlotOverlay.X;
        minecraft.textureManager.bindTexture(overlay.getTexture());
        blit(getGuiLeft() + 88, getGuiTop() + 22, 0, 0, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
    }

    private boolean canEncode() {
        if (tile.formula != null && tile.formula.isValidFormula() || tile.getFormulaSlot().isEmpty()) {
            return false;
        }
        ItemStack formulaStack = tile.getFormulaSlot().getStack();
        return formulaStack.getItem() instanceof ItemCraftingFormula && ((ItemCraftingFormula) formulaStack.getItem()).getInventory(formulaStack) == null;
    }
}