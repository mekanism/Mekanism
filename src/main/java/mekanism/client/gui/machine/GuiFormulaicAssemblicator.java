package mekanism.client.gui.machine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GuiFormulaicAssemblicator extends GuiConfigurableTile<TileEntityFormulaicAssemblicator, MekanismTileContainer<TileEntityFormulaicAssemblicator>> {

    private MekanismButton encodeFormulaButton;
    private MekanismButton stockControlButton;
    private MekanismButton fillEmptyButton;
    private MekanismButton craftSingleButton;
    private MekanismButton craftAvailableButton;
    private MekanismButton autoModeButton;

    public GuiFormulaicAssemblicator(MekanismTileContainer<TileEntityFormulaicAssemblicator> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 64;
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 159, 15));
        //Overwrite the output slots with a "combined" slot
        addRenderableWidget(new GuiSlot(SlotType.OUTPUT_LARGE, this, 115, 16));
        addRenderableWidget(new GuiProgress(() -> tile.getOperatingTicks() / (double) tile.getTicksRequired(), ProgressType.TALL_RIGHT, this, 86, 43).jeiCrafting());
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer()));
        encodeFormulaButton = addRenderableWidget(new MekanismImageButton(this, 7, 45, 14, getButtonLocation("encode_formula"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.ENCODE_FORMULA, tile)), getOnHover(MekanismLang.ENCODE_FORMULA)));
        stockControlButton = addRenderableWidget(new MekanismImageButton(this, 26, 75, 16, getButtonLocation("stock_control"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.STOCK_CONTROL_BUTTON, tile)),
              getOnHover(() -> MekanismLang.STOCK_CONTROL.translate(OnOff.of(tile.getStockControl())))));
        fillEmptyButton = addRenderableWidget(new MekanismImageButton(this, 44, 75, 16, getButtonLocation("fill_empty"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.MOVE_ITEMS, tile)), getOnHover(MekanismLang.FILL_EMPTY)));
        craftSingleButton = addRenderableWidget(new MekanismImageButton(this, 71, 75, 16, getButtonLocation("craft_single"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CRAFT_SINGLE, tile)), getOnHover(MekanismLang.CRAFT_SINGLE)));
        craftAvailableButton = addRenderableWidget(new MekanismImageButton(this, 89, 75, 16, getButtonLocation("craft_available"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CRAFT_ALL, tile)), getOnHover(MekanismLang.CRAFT_AVAILABLE)));
        autoModeButton = addRenderableWidget(new MekanismImageButton(this, 107, 75, 16, getButtonLocation("auto_toggle"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, tile)),
              getOnHover(() -> MekanismLang.AUTO_MODE.translate(OnOff.of(tile.getAutoMode())))));
        updateEnabledButtons();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateEnabledButtons();
    }

    private void updateEnabledButtons() {
        encodeFormulaButton.active = !tile.getAutoMode() && tile.hasRecipe() && canEncode();
        stockControlButton.active = tile.formula != null && tile.formula.isValidFormula();
        fillEmptyButton.active = !tile.getAutoMode();
        craftSingleButton.active = !tile.getAutoMode() && tile.hasRecipe();
        craftAvailableButton.active = !tile.getAutoMode() && tile.hasRecipe();
        autoModeButton.active = tile.formula != null && tile.formula.isValidFormula();
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    protected ItemStack checkValidity(int slotIndex) {
        int i = slotIndex - 21;
        if (i >= 0 && tile.formula != null && tile.formula.isValidFormula()) {
            ItemStack stack = tile.formula.input.get(i);
            if (!stack.isEmpty()) {
                Slot slot = menu.slots.get(slotIndex);
                //Only render the "correct" item in the gui slot if we don't already have that item there
                if (slot.getItem().isEmpty() || !tile.formula.isIngredientInPos(tile.getLevel(), slot.getItem(), i)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void renderBg(@Nonnull PoseStack matrix, float partialTick, int mouseX, int mouseY) {
        super.renderBg(matrix, partialTick, mouseX, mouseY);
        //TODO: Gui element
        SlotOverlay overlay = tile.hasRecipe() ? SlotOverlay.CHECK : SlotOverlay.X;
        RenderSystem.setShaderTexture(0, overlay.getTexture());
        blit(matrix, leftPos + 88, topPos + 22, 0, 0, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
    }

    private boolean canEncode() {
        if (tile.formula != null && tile.formula.isValidFormula() || tile.getFormulaSlot().isEmpty()) {
            return false;
        }
        ItemStack formulaStack = tile.getFormulaSlot().getStack();
        return formulaStack.getItem() instanceof ItemCraftingFormula formula && formula.getInventory(formulaStack) == null;
    }
}