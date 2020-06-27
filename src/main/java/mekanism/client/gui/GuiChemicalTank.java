package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiMergedChemicalBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalTank extends GuiConfigurableTile<TileEntityChemicalTank, MekanismTileContainer<TileEntityChemicalTank>> {

    public GuiChemicalTank(MekanismTileContainer<TileEntityChemicalTank> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiMergedChemicalBar<>(this, tile, tile.getChemicalTank(), 42, 16, 116, 10, true));
        func_230480_a_(new GuiInnerScreen(this, 42, 37, 118, 28, () -> {
            List<ITextComponent> ret = new ArrayList<>();
            Current current = tile.getChemicalTank().getCurrent();
            if (current == Current.EMPTY) {
                ret.add(MekanismLang.CHEMICAL.translate(MekanismLang.NONE));
                ret.add(MekanismLang.GENERIC_FRACTION.translate(0, tile.getTier() == ChemicalTankTier.CREATIVE ? MekanismLang.INFINITE : formatInt(tile.getTier().getStorage())));
            } else if (current == Current.GAS) {
                addStored(ret, tile.getChemicalTank().getGasTank(), MekanismLang.GAS);
            } else if (current == Current.INFUSION) {
                addStored(ret, tile.getChemicalTank().getInfusionTank(), MekanismLang.INFUSE_TYPE);
            } else if (current == Current.PIGMENT) {
                addStored(ret, tile.getChemicalTank().getPigmentTank(), MekanismLang.PIGMENT);
            } else if (current == Current.SLURRY) {
                addStored(ret, tile.getChemicalTank().getSlurryTank(), MekanismLang.SLURRY);
            } else {
                throw new IllegalStateException("Unknown current type");
            }
            return ret;
        }));
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiGasMode(this, getGuiLeft() + 159, getGuiTop() + 72, true, () -> tile.dumping, tile.getPos(), 0));
    }

    private void addStored(List<ITextComponent> ret, IChemicalTank<?, ?> tank, ILangEntry langKey) {
        ret.add(langKey.translate(tank.getStack()));
        if (!tank.isEmpty() && tile.getTier() == ChemicalTankTier.CREATIVE) {
            ret.add(MekanismLang.INFINITE.translate());
        } else {
            ret.add(MekanismLang.GENERIC_FRACTION.translate(formatInt(tank.getStored()),
                  tile.getTier() == ChemicalTankTier.CREATIVE ? MekanismLang.INFINITE : formatInt(tank.getCapacity())));
        }
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, getYSize() - 96 + 2, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}