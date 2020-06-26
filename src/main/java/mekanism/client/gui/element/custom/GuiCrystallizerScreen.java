package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.tags.MekanismTags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;

public class GuiCrystallizerScreen extends GuiTexturedElement {

    private static final SlotType SLOT = SlotType.ORE;

    private final GuiInnerScreen innerScreen;
    private final int slotX;
    private final List<ItemStack> iterStacks = new ArrayList<>();
    private ItemStack renderStack = ItemStack.EMPTY;
    private int stackSwitch = 0;
    private int stackIndex = 0;
    @Nonnull
    private Slurry prevSlurry = MekanismAPI.EMPTY_SLURRY;
    private final IOreInfo oreInfo;

    public GuiCrystallizerScreen(IGuiWrapper gui, int x, int y, IOreInfo oreInfo) {
        super(SLOT.getTexture(), gui, x, y, 115, 42);
        innerScreen = new GuiInnerScreen(gui, x, y, field_230688_j_, field_230689_k_);
        this.oreInfo = oreInfo;
        this.slotX = this.field_230690_l_ + 115 - SLOT.getWidth();
        field_230693_o_ = false;
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        BoxedChemicalStack boxedChemical = oreInfo.getInputChemical();
        if (!boxedChemical.isEmpty()) {
            drawString(TextComponentUtil.build(boxedChemical), 33, 15, screenTextColor());
            if (boxedChemical.getChemicalType() == ChemicalType.SLURRY && !renderStack.isEmpty()) {
                drawString(MekanismLang.GENERIC_PARENTHESIS.translate(renderStack), 33, 24, screenTextColor());
            } else {
                ChemicalCrystallizerRecipe recipe = oreInfo.getRecipe();
                if (recipe == null) {
                    drawString(MekanismLang.NO_RECIPE.translate(), 33, 24, screenTextColor());
                } else {
                    drawString(MekanismLang.GENERIC_PARENTHESIS.translate(recipe.getOutput(boxedChemical)), 33, 24, screenTextColor());
                }
            }
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        innerScreen.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(slotX, field_230691_m_, 0, 0, SLOT.getWidth(), SLOT.getHeight(), SLOT.getWidth(), SLOT.getHeight());
        if (!renderStack.isEmpty()) {
            guiObj.renderItem(renderStack, slotX + 1, field_230691_m_ + 1);
        }
    }

    //TODO: Come up with a better way to have this "tick"/update
    @Override
    public void tick() {
        BoxedChemicalStack boxedChemical = oreInfo.getInputChemical();
        if (boxedChemical.getChemicalType() == ChemicalType.SLURRY) {
            Slurry inputSlurry = (Slurry) boxedChemical.getChemicalStack().getType();
            if (prevSlurry != inputSlurry) {
                prevSlurry = inputSlurry;
                iterStacks.clear();
                if (!prevSlurry.isEmptyType() && !prevSlurry.isIn(MekanismTags.Slurries.DIRTY)) {
                    Tag<Item> oreTag = prevSlurry.getOreTag();
                    if (oreTag != null) {
                        for (Item ore : oreTag.func_230236_b_()) {
                            iterStacks.add(new ItemStack(ore));
                        }
                    }
                } else {
                    renderStack = ItemStack.EMPTY;
                }
                stackSwitch = 0;
                stackIndex = -1;
            }
            if (iterStacks.isEmpty()) {
                renderStack = ItemStack.EMPTY;
            } else {
                if (stackSwitch > 0) {
                    stackSwitch--;
                }
                if (stackSwitch == 0) {
                    stackSwitch = 20;
                    if (stackIndex == -1 || stackIndex == iterStacks.size() - 1) {
                        stackIndex = 0;
                    } else if (stackIndex < iterStacks.size() - 1) {
                        stackIndex++;
                    }
                    renderStack = iterStacks.get(stackIndex);
                }
            }
        }
    }

    public interface IOreInfo {

        @Nonnull
        BoxedChemicalStack getInputChemical();

        @Nullable
        ChemicalCrystallizerRecipe getRecipe();
    }
}