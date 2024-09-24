package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiQIOCrystallizerScreen extends GuiInnerScreen {

    @Nullable
    private final GuiSequencedSlotDisplay slotDisplay;
    private final List<ItemStack> iterStacks;
    private final IOreInfo oreInfo;
    private final GuiSlot slot;

    @NotNull
    private Chemical prevSlurry = MekanismAPI.EMPTY_CHEMICAL;

    public GuiQIOCrystallizerScreen(IGuiWrapper gui, int x, int y, int width, int height, IOreInfo oreInfo) {
        super(gui, x, y, width, height);
        this.oreInfo = oreInfo;
        int slotX = relativeX + this.width - SlotType.ORE.getWidth();
        this.slot = addChild(new GuiSlot(SlotType.ORE, gui, slotX, 13));
        if (this.oreInfo.usesSequencedDisplay()) {
            this.iterStacks = new ArrayList<>();
            this.slotDisplay = addChild(new GuiSequencedSlotDisplay(gui, slotX + 1, 14, () -> this.iterStacks));
            updateSlotContents();
        } else {
            this.iterStacks = Collections.emptyList();
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
        if (oreInfo.usesSequencedDisplay() && slotDisplay != null) {//Note: If we use the sequenced display, slotDisplay should never be null
            ChemicalStack chemical = oreInfo.getInputChemical();
            if (!chemical.isEmpty()) {
                Chemical inputSlurry = chemical.getChemical();
                if (prevSlurry != inputSlurry) {
                    prevSlurry = inputSlurry;
                    iterStacks.clear();
                    if (!prevSlurry.isEmptyType() && !prevSlurry.is(MekanismAPITags.Chemicals.DIRTY)) {
                        TagKey<Item> oreTag = prevSlurry.getOreTag();
                        if (oreTag != null) {
                            for (Holder<Item> ore : BuiltInRegistries.ITEM.getTagOrEmpty(oreTag)) {
                                iterStacks.add(new ItemStack(ore));
                            }
                        }
                    }
                    slotDisplay.updateStackList();
                }
            } else if (!prevSlurry.isEmptyType()) {
                prevSlurry = MekanismAPI.EMPTY_CHEMICAL;
                iterStacks.clear();
                slotDisplay.updateStackList();
            }
        }
    }

    @Override
    protected List<Component> getRenderStrings() {
        ChemicalStack chemical = oreInfo.getInputChemical();
        if (!chemical.isEmpty()) {
            Component recipeComponent;
            //Note: If we use the sequenced display, slotDisplay should never be null
            ItemStack renderStack = oreInfo.usesSequencedDisplay() && slotDisplay != null ? slotDisplay.getRenderStack() : oreInfo.getRenderStack();
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
            return List.of(chemical.getTextComponent(), recipeComponent);
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

        @NotNull
        ChemicalStack getInputChemical();

        @Nullable
        ChemicalCrystallizerRecipe getRecipe();

        @NotNull
        default ItemStack getRenderStack() {
            return ItemStack.EMPTY;
        }

        default boolean usesSequencedDisplay() {
            return true;
        }
    }
}
