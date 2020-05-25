package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
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
    private Gas prevGas = MekanismAPI.EMPTY_GAS;
    private IOreInfo oreInfo;

    public GuiCrystallizerScreen(IGuiWrapper gui, int x, int y, IOreInfo oreInfo) {
        super(SLOT.getTexture(), gui, x, y, 115, 42);
        innerScreen = new GuiInnerScreen(gui, x, y, width, height);
        this.oreInfo = oreInfo;
        this.slotX = this.x + 115 - SLOT.getWidth();
        active = false;
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        GasStack gasStack = oreInfo.getInputGas();
        if (!gasStack.isEmpty()) {
            drawString(TextComponentUtil.build(gasStack), 33, 15, screenTextColor());
            //TODO - V10: Figure out how to handle slurries for recipes
            /*if (gasStack.getType() instanceof Slurry && !renderStack.isEmpty()) {
                drawString(MekanismLang.GENERIC_PARENTHESIS.translate(renderStack), 33, 24, screenTextColor());
            } else {*/
                GasToItemStackRecipe recipe = oreInfo.getRecipe();
                if (recipe == null) {
                    drawString(MekanismLang.NO_RECIPE.translate(), 33, 24, screenTextColor());
                } else {
                    drawString(MekanismLang.GENERIC_PARENTHESIS.translate(recipe.getOutput(gasStack)), 33, 24, screenTextColor());
                }
            //}
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        innerScreen.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(slotX, y, 0, 0, SLOT.getWidth(), SLOT.getHeight(), SLOT.getWidth(), SLOT.getHeight());
        if (!renderStack.isEmpty()) {
            guiObj.renderItem(renderStack, slotX + 1, y + 1);
        }
    }

    //TODO: Come up with a better way to have this "tick"/update
    @Override
    public void tick() {
        Gas inputGas = oreInfo.getInputGas().getType();
        if (prevGas != inputGas) {
            prevGas = inputGas;
            //TODO - V10: Figure out how to handle slurries for recipes
            /*if (!prevGas.isEmptyType() && prevGas instanceof Slurry && !prevGas.isIn(MekanismTags.Slurries.DIRTY)) {
                updateStackList(((Slurry) prevGas).getOreTag());
            } else {*/
                resetStacks();
            //}
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

    private void resetStacks() {
        iterStacks.clear();
        renderStack = ItemStack.EMPTY;
        stackSwitch = 0;
        stackIndex = -1;
    }

    private void updateStackList(@Nullable Tag<Item> oreTag) {
        iterStacks.clear();
        if (oreTag != null) {
            for (Item ore : oreTag.getAllElements()) {
                iterStacks.add(new ItemStack(ore));
            }
        }
        stackSwitch = 0;
        stackIndex = -1;
    }

    public interface IOreInfo {

        @Nonnull
        GasStack getInputGas();

        @Nullable
        GasToItemStackRecipe getRecipe();
    }
}