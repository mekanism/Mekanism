package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.bar.GuiMergedChemicalBar;
import mekanism.client.gui.element.button.GuiGasMode;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiChemicalTank extends GuiConfigurableTile<TileEntityChemicalTank, MekanismTileContainer<TileEntityChemicalTank>> {

    public GuiChemicalTank(MekanismTileContainer<TileEntityChemicalTank> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        //Add the side holder before the slots, as it holds a couple of the slots
        addRenderableWidget(GuiSideHolder.armorHolder(this));
        super.addGuiElements();
        addRenderableWidget(new GuiMergedChemicalBar<>(this, tile, tile.getChemicalTank(), 42, 16, 116, 10, true));
        addRenderableWidget(new GuiInnerScreen(this, 42, 37, 118, 28, () -> {
            List<Component> ret = new ArrayList<>();
            switch (tile.getChemicalTank().getCurrent()) {
                case EMPTY -> {
                    ret.add(MekanismLang.CHEMICAL.translate(MekanismLang.NONE));
                    ret.add(MekanismLang.GENERIC_FRACTION.translate(0, tile.getTier() == ChemicalTankTier.CREATIVE ? MekanismLang.INFINITE :
                                                                       TextUtils.format(tile.getTier().getStorage())));
                }
                case GAS -> addStored(ret, tile.getChemicalTank().getGasTank(), MekanismLang.GAS);
                case INFUSION -> addStored(ret, tile.getChemicalTank().getInfusionTank(), MekanismLang.INFUSE_TYPE);
                case PIGMENT -> addStored(ret, tile.getChemicalTank().getPigmentTank(), MekanismLang.PIGMENT);
                case SLURRY -> addStored(ret, tile.getChemicalTank().getSlurryTank(), MekanismLang.SLURRY);
                default -> throw new IllegalStateException("Unknown current type");
            }
            return ret;
        }));
        addRenderableWidget(new GuiGasMode(this, 159, 72, true, () -> tile.dumping, tile.getBlockPos(), 0));
    }

    private void addStored(List<Component> ret, IChemicalTank<?, ?> tank, ILangEntry langKey) {
        ret.add(langKey.translate(tank.getStack()));
        if (!tank.isEmpty() && tile.getTier() == ChemicalTankTier.CREATIVE) {
            ret.add(MekanismLang.INFINITE.translate());
        } else {
            ret.add(MekanismLang.GENERIC_FRACTION.translate(TextUtils.format(tank.getStored()),
                  tile.getTier() == ChemicalTankTier.CREATIVE ? MekanismLang.INFINITE : TextUtils.format(tank.getCapacity())));
        }
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}