package mekanism.client.gui.element.custom;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class GuiCrystallizerScreen extends GuiInnerScreen {

    @Nullable
    private final GuiSequencedSlotDisplay slotDisplay;
    private final IOreInfo oreInfo;
    private final GuiSlot slot;

    private ChemicalResource prevSlurry = ChemicalResource.EMPTY;

    public GuiCrystallizerScreen(IGuiWrapper gui, int x, int y, int width, int height, IOreInfo oreInfo) {
        super(gui, x, y, width, height);
        this.oreInfo = oreInfo;
        int slotX = relativeX + this.width - 18;
        this.slot = addChild(new GuiSlot(SlotType.DARK, gui, slotX, relativeY));
        if (this.oreInfo.usesSequencedDisplay()) {
            this.slotDisplay = addChild(new GuiSequencedSlotDisplay(gui, slotX + 1, relativeY + 1, this.oreInfo::slotDisplay));
            updateSlotContents();
        } else {
            this.slotDisplay = null;
        }
        defaultFormat();
    }

    public int getSlotX() {
        return this.slot.getX();
    }

    public int getSlotY() {
        return this.slot.getY();
    }

    @Override
    public void tick() {
        updateSlotContents();
        super.tick();
    }

    private void updateSlotContents() {
        if (slotDisplay != null) {
            ChemicalResource chemical = oreInfo.getInputChemical();
            if (!prevSlurry.equals(chemical)) {
                prevSlurry = chemical;
                slotDisplay.updateStackList();
            }
        }
    }

    @Override
    protected List<Component> getRenderStrings() {
        ChemicalResource chemical = oreInfo.getInputChemical();
        if (!chemical.isEmpty()) {
            Component recipeComponent;
            ItemStack renderStack = slotDisplay == null ? oreInfo.getRenderStack() : slotDisplay.getRenderStack();
            if (!renderStack.isEmpty()) {
                recipeComponent = MekanismLang.GENERIC_PARENTHESIS.translate(renderStack);
            } else {
                ChemicalCrystallizerRecipe recipe = oreInfo.getRecipe();
                if (recipe == null) {
                    recipeComponent = MekanismLang.NO_RECIPE.translate();
                } else {
                    recipeComponent = MekanismLang.GENERIC_PARENTHESIS.translate(recipe.getOutput(chemical));
                }
            }
            return List.of(TextComponentUtil.build(chemical), recipeComponent);
        }
        return Collections.emptyList();
    }

    @Override
    protected int getMaxTextWidth(int row) {
        if (row == 0) {//Don't allow the first line of text to intersect with the slot we draw
            return width - slot.getWidth();
        }
        return super.getMaxTextWidth(row);
    }

    public interface IOreInfo {

        ChemicalResource getInputChemical();

        @Nullable
        ChemicalCrystallizerRecipe getRecipe();

        default ItemStack getRenderStack() {
            return ItemStack.EMPTY;
        }

        default boolean usesSequencedDisplay() {
            return true;
        }

        default SlotDisplay slotDisplay() {
            ChemicalCrystallizerRecipe recipe = getRecipe();
            return recipe == null ? SlotDisplay.Empty.INSTANCE : recipe.getTypeDisplay();
        }
    }
}
