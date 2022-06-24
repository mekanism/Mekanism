package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostIngredientConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.text.InputValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiModIDFilter<FILTER extends IModIDFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiTextFilter<FILTER, TILE> {

    protected GuiModIDFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, @Nullable FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.MODID_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected CharPredicate getInputValidator() {
        return InputValidator.RL_NAMESPACE.or(InputValidator.WILDCARD_CHARS);
    }

    @Override
    protected List<Component> getScreenText() {
        List<Component> list = super.getScreenText();
        list.add(MekanismLang.MODID_FILTER_ID.translate(filter.getModID()));
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.MODID_FILTER_NO_ID;
    }

    @Override
    protected boolean setText() {
        return setFilterName(text.getText(), false);
    }

    @NotNull
    @Override
    protected List<ItemStack> getRenderStacks() {
        if (filter.hasFilter()) {
            return TagCache.getItemModIDStacks(filter.getModID());
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected IGhostIngredientConsumer getGhostHandler() {
        return new IGhostIngredientConsumer() {
            @Override
            public boolean supportsIngredient(Object ingredient) {
                if (ingredient instanceof ItemStack stack) {
                    return !stack.isEmpty();
                } else if (ingredient instanceof FluidStack stack) {
                    return !stack.isEmpty();
                } else if (ingredient instanceof ChemicalStack<?> stack) {
                    return !stack.isEmpty();
                }
                return RegistryUtils.getName(ingredient) != null;
            }

            @Override
            public void accept(Object ingredient) {
                if (ingredient instanceof ItemStack stack) {
                    setFilterName(stack);
                } else if (ingredient instanceof FluidStack stack) {
                    setFilterName(RegistryUtils.getName(stack.getFluid()));
                } else if (ingredient instanceof ChemicalStack<?> stack) {
                    setFilterName(stack.getTypeRegistryName());
                } else {
                    ResourceLocation registryName = RegistryUtils.getName(ingredient);
                    if (registryName != null) {
                        setFilterName(registryName);
                    }
                }
            }
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return mouseClickSlot(gui(), button, mouseX, mouseY, relativeX + 8, relativeY + getSlotOffset() + 1, NOT_EMPTY, this::setFilterName) ||
               super.mouseClicked(mouseX, mouseY, button);
    }

    private void setFilterName(ItemStack stack) {
        setFilterName(MekanismUtils.getModId(stack), true);
    }

    private void setFilterName(ResourceLocation registryName) {
        setFilterName(registryName.getNamespace(), true);
    }

    private boolean setFilterName(String name, boolean click) {
        boolean success = false;
        if (name.isEmpty()) {
            filterSaveFailed(getNoFilterSaveError());
        } else if (name.equals(filter.getModID())) {
            filterSaveFailed(MekanismLang.MODID_FILTER_SAME_ID);
        } else if (!hasMatchingTargets(name)) {
            //Even though we got the mod id from the target if it is not a click, there may not be any
            // matching elements if say a mod only adds fluids. and we are matching items
            filterSaveFailed(MekanismLang.TEXT_FILTER_NO_MATCHES);
        } else {
            filter.setModID(name);
            slotDisplay.updateStackList();
            text.setText("");
            filterSaveSuccess();
            success = true;
        }
        if (click) {
            playClickSound();
        }
        return success;
    }

    protected boolean hasMatchingTargets(String name) {
        return !TagCache.getItemModIDStacks(name).isEmpty();
    }
}