package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GuiQIOCrystallizerScreen extends GuiInnerScreen {

    @Nullable
    private final GuiSequencedSlotDisplay slotDisplay;
    private final List<ItemStack> iterStacks;
    private final IOreInfo oreInfo;
    private final GuiSlot slot;

    private ChemicalResource prevSlurry = ChemicalResource.EMPTY;

    public GuiQIOCrystallizerScreen(IGuiWrapper gui, int x, int y, int width, int height, IOreInfo oreInfo) {
        super(gui, x, y, width, height);
        this.oreInfo = oreInfo;
        int slotX = relativeX + this.width - 18;
        this.slot = addChild(new GuiSlot(SlotType.DARK, gui, slotX, relativeY));
        if (this.oreInfo.usesSequencedDisplay()) {
            this.iterStacks = new ArrayList<>();
            this.slotDisplay = addChild(new GuiSequencedSlotDisplay(gui, slotX + 1, relativeY + 1, () -> this.iterStacks));
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
            ChemicalResource chemical = oreInfo.getInputChemical();
            if (!chemical.isEmpty()) {
                if (!chemical.equals(prevSlurry)) {
                    prevSlurry = chemical;
                    iterStacks.clear();
                    if (!prevSlurry.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                        RegistryAccess registryAccess = gui().registryAccess();
                        ChemicalSolidTag tag = chemical.getSolidTag(registryAccess);
                        if (tag != null) {
                            Named<Item> tagContents = tag.lookupTag(registryAccess).orElse(null);
                            if (tagContents != null) {
                                for (Holder<Item> tagContent : tagContents) {
                                    iterStacks.add(new ItemStack(tagContent));
                                }
                            }
                        }
                    }
                    slotDisplay.updateStackList();
                }
            } else if (!prevSlurry.isEmpty()) {
                prevSlurry = ChemicalResource.EMPTY;
                iterStacks.clear();
                slotDisplay.updateStackList();
            }
        }
    }

    @Override
    protected List<Component> getRenderStrings() {
        ChemicalResource chemical = oreInfo.getInputChemical();
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
    }
}
