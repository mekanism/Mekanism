package mekanism.client.gui.machine;

import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.button.TooltipToggleButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.component.FormulaComponent;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class GuiFormulaicAssemblicator extends GuiConfigurableTile<TileEntityFormulaicAssemblicator, FormulaicAssemblicatorContainer> {

    public GuiFormulaicAssemblicator(FormulaicAssemblicatorContainer container, Inventory inv, Component title) {
        super(container, inv, title, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT + 64);
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.energyContainer(), 159, 15)).warning(WarningType.NOT_ENOUGH_ENERGY, () -> {
            if (tile.getAutoMode() && tile.hasRecipe()) {
                MachineEnergyContainer<TileEntityFormulaicAssemblicator> energyContainer = tile.energyContainer();
                return energyContainer.getEnergyPerTick() > energyContainer.getAmountAsLong();
            }
            return false;
        });
        //Overwrite the output slots with a "combined" slot
        addRenderableWidget(new GuiSlot(SlotType.OUTPUT_LARGE, this, 115, 16));
        addRenderableWidget(new GuiProgress(() -> tile.getOperatingTicks() / (float) tile.getTicksRequired(), ProgressType.TALL_RIGHT, this, 86, 43).recipeViewerCrafting());
        addRenderableWidget(new GuiEnergyTab(this, tile.energyContainer(), tile::usedEnergy));
        addRenderableWidget(new MekanismImageButton(this, 7, 45, 14, getButtonLocation("encode_formula"),
              (element, _, _) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.ENCODE_FORMULA, ((GuiFormulaicAssemblicator) element.gui()).tile))))
              .setCheckActive(() -> {
                  if (!tile.getAutoMode() && tile.hasRecipe() && !tile.hasValidFormula()) {
                      //Validate if we can encode it
                      ItemResource resource = tile.getFormulaSlot().resource();
                      if (!resource.isEmpty() && MekanismItems.CRAFTING_FORMULA.is(resource)) {
                          return resource.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY).isEmpty();
                      }
                  }
                  return false;
              }).setTooltip(MekanismLang.ENCODE_FORMULA);
        addRenderableWidget(new TooltipToggleButton(this, 26, 75, 16, getButtonLocation("stock_control"), tile::getStockControl,
              (element, _, _) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.STOCK_CONTROL_BUTTON, ((GuiFormulaicAssemblicator) element.gui()).tile)),
              MekanismLang.STOCK_CONTROL.translate(OnOff.ON), MekanismLang.STOCK_CONTROL.translate(OnOff.OFF)))
              .setCheckActive(tile::hasValidFormula);
        addRenderableWidget(new ToggleButton(this, 44, 75, 16, 16, getButtonLocation("empty"),
              getButtonLocation("fill"), () -> tile.formula.isEmpty(), (element, _, _) -> {
            TileEntityFormulaicAssemblicator tile = ((GuiFormulaicAssemblicator) element.gui()).tile;
            GuiInteraction interaction = tile.formula.isEmpty() ? GuiInteraction.EMPTY_GRID : GuiInteraction.FILL_GRID;
            return PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile));
        }, MekanismLang.EMPTY_ASSEMBLICATOR.translate(), MekanismLang.FILL_ASSEMBLICATOR.translate()))
              .setCheckActive(() -> !tile.getAutoMode());
        addRenderableWidget(new MekanismImageButton(this, 71, 75, 16, getButtonLocation("craft_single"),
              (element, _, _) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CRAFT_SINGLE, ((GuiFormulaicAssemblicator) element.gui()).tile))))
              .setTooltip(MekanismLang.CRAFT_SINGLE)
              .setCheckActive(() -> !tile.getAutoMode() && tile.hasRecipe());
        addRenderableWidget(new MekanismImageButton(this, 89, 75, 16, getButtonLocation("craft_available"),
              (element, _, _) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CRAFT_ALL, ((GuiFormulaicAssemblicator) element.gui()).tile))))
              .setTooltip(MekanismLang.CRAFT_AVAILABLE)
              .setCheckActive(() -> !tile.getAutoMode() && tile.hasRecipe());
        addRenderableWidget(new TooltipToggleButton(this, 107, 75, 16, getButtonLocation("auto_toggle"), tile::getAutoMode,
              (element, _, _) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.NEXT_MODE, ((GuiFormulaicAssemblicator) element.gui()).tile)),
              MekanismLang.AUTO_MODE.translate(OnOff.ON), MekanismLang.AUTO_MODE.translate(OnOff.OFF)))
              .setCheckActive(tile::hasValidFormula);
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected ItemStack checkValidity(int slotIndex) {
        int i = slotIndex - 21;
        if (i >= 0 && tile.hasValidFormula()) {
            ItemStack stack = tile.formula.getInputStack(i);
            if (!stack.isEmpty()) {
                Slot slot = menu.slots.get(slotIndex);
                //Only render the "correct" item in the gui slot if we don't already have that item there
                if (slot.getItem().isEmpty() || !tile.formula.isIngredientInPos(getLevel(), ItemResource.of(slot.getItem()), i)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);
        //TODO: Gui element
        SlotOverlay overlay = tile.hasRecipe() ? SlotOverlay.CHECK : SlotOverlay.X;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, overlay.getTexture(), leftPos + 88, topPos + 22, 0, 0, overlay.getWidth(), overlay.getHeight(),
              overlay.getWidth(), overlay.getHeight());
    }
}