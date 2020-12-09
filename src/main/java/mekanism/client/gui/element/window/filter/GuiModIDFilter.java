package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostIngredientConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class GuiModIDFilter<FILTER extends IModIDFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiTextFilter<FILTER, TILE> {

    protected GuiModIDFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.MODID_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = super.getScreenText();
        list.add(MekanismLang.MODID_FILTER_ID.translate(filter.getModID()));
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.MODID_FILTER_NO_ID;
    }

    @Override
    protected void setText() {
        setFilterName(text.getText(), false);
    }

    @Nonnull
    @Override
    protected List<ItemStack> getRenderStacks() {
        if (filter.hasFilter()) {
            return TagCache.getModIDStacks(filter.getModID(), false);
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected IGhostIngredientConsumer getGhostHandler() {
        return new IGhostIngredientConsumer() {
            @Override
            public boolean supportsIngredient(Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    return !((ItemStack) ingredient).isEmpty();
                } else if (ingredient instanceof FluidStack) {
                    return !((FluidStack) ingredient).isEmpty();
                } else if (ingredient instanceof ChemicalStack) {
                    return !((ChemicalStack<?>) ingredient).isEmpty();
                }
                return ingredient instanceof IForgeRegistryEntry;
            }

            @Override
            public void accept(Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    setFilterName((ItemStack) ingredient);
                } else if (ingredient instanceof FluidStack) {
                    setFilterName(((FluidStack) ingredient).getFluid());
                } else if (ingredient instanceof ChemicalStack) {
                    setFilterName(((ChemicalStack<?>) ingredient).getType());
                } else if (ingredient instanceof IForgeRegistryEntry) {
                    setFilterName((IForgeRegistryEntry<?>) ingredient);
                }
            }
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return mouseClickSlot(getGuiObj(), button, mouseX, mouseY, relativeX + 8, relativeY + getSlotOffset() + 1, NOT_EMPTY, this::setFilterName) ||
               super.mouseClicked(mouseX, mouseY, button);
    }

    private void setFilterName(ItemStack stack) {
        setFilterName(MekanismUtils.getModId(stack), true);
    }

    private void setFilterName(IForgeRegistryEntry<?> registryEntry) {
        setFilterName(registryEntry.getRegistryName().getNamespace(), true);
    }

    private void setFilterName(String name, boolean click) {
        if (name.isEmpty()) {
            filterSaveFailed(getNoFilterSaveError());
        } else if (name.equals(filter.getModID())) {
            filterSaveFailed(MekanismLang.MODID_FILTER_SAME_ID);
        } else {
            filter.setModID(name);
            slotDisplay.updateStackList();
            text.setText("");
        }
        if (click) {
            playClickSound();
        }
    }
}