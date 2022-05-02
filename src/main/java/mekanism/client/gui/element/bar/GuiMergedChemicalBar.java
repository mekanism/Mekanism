package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;

public class GuiMergedChemicalBar<HANDLER extends IGasTracker & IInfusionTracker & IPigmentTracker & ISlurryTracker> extends GuiBar<IBarInfoHandler> implements
      IJEIIngredientHelper {

    private final MergedChemicalTank chemicalTank;
    private final GuiChemicalBar<Gas, GasStack> gasBar;
    private final GuiChemicalBar<InfuseType, InfusionStack> infusionBar;
    private final GuiChemicalBar<Pigment, PigmentStack> pigmentBar;
    private final GuiChemicalBar<Slurry, SlurryStack> slurryBar;

    public GuiMergedChemicalBar(IGuiWrapper gui, HANDLER handler, MergedChemicalTank chemicalTank, int x, int y, int width, int height, boolean horizontal) {
        super(TextureAtlas.LOCATION_BLOCKS, gui, new IBarInfoHandler() {
            @Nullable
            private IChemicalTank<?, ?> getCurrentTank() {
                return switch (chemicalTank.getCurrent()) {
                    case EMPTY -> null;
                    case GAS -> chemicalTank.getGasTank();
                    case INFUSION -> chemicalTank.getInfusionTank();
                    case PIGMENT -> chemicalTank.getPigmentTank();
                    case SLURRY -> chemicalTank.getSlurryTank();
                };
            }

            @Override
            public Component getTooltip() {
                IChemicalTank<?, ?> currentTank = getCurrentTank();
                if (currentTank == null) {
                    return MekanismLang.EMPTY.translate();
                } else if (currentTank.getStored() == Long.MAX_VALUE) {
                    return MekanismLang.GENERIC_STORED.translate(currentTank.getType(), MekanismLang.INFINITE);
                }
                return MekanismLang.GENERIC_STORED_MB.translate(currentTank.getType(), TextUtils.format(currentTank.getStored()));
            }

            @Override
            public double getLevel() {
                IChemicalTank<?, ?> currentTank = getCurrentTank();
                return currentTank == null ? 0 : currentTank.getStored() / (double) currentTank.getCapacity();
            }
        }, x, y, width, height, horizontal);
        this.chemicalTank = chemicalTank;
        gasBar = addPositionOnlyChild(new GuiChemicalBar<>(gui, GuiChemicalBar.getProvider(this.chemicalTank.getGasTank(), handler.getGasTanks(null)), x, y, width, height, horizontal));
        infusionBar = addPositionOnlyChild(new GuiChemicalBar<>(gui, GuiChemicalBar.getProvider(this.chemicalTank.getInfusionTank(), handler.getInfusionTanks(null)), x, y, width, height, horizontal));
        pigmentBar = addPositionOnlyChild(new GuiChemicalBar<>(gui, GuiChemicalBar.getProvider(this.chemicalTank.getPigmentTank(), handler.getPigmentTanks(null)), x, y, width, height, horizontal));
        slurryBar = addPositionOnlyChild(new GuiChemicalBar<>(gui, GuiChemicalBar.getProvider(this.chemicalTank.getSlurryTank(), handler.getSlurryTanks(null)), x, y, width, height, horizontal));
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        GuiChemicalBar<?, ?> currentBar = getCurrentBarNoFallback();
        if (currentBar == null) {
            super.renderToolTip(matrix, mouseX, mouseY);
        } else {
            currentBar.renderToolTip(matrix, mouseX, mouseY);
        }
    }

    @Override
    void drawContentsChecked(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks, double handlerLevel, boolean warning) {
        GuiChemicalBar<?, ?> currentBar = getCurrentBarNoFallback();
        if (currentBar != null) {
            currentBar.drawContentsChecked(matrix, mouseX, mouseY, partialTicks, handlerLevel, warning);
        }
    }

    @Override
    protected void renderBarOverlay(PoseStack matrix, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        //Rendering is redirected in drawContentsChecked
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GuiChemicalBar<?, ?> currentBar = getCurrentBarNoFallback();
        if (currentBar == null) {
            //If all the tanks are currently empty, pass the click event to all of them;
            // if multiple types are somehow stored in the dropper, insertion checks should prevent them from being inserted at the same time
            return gasBar.mouseClicked(mouseX, mouseY, button) | infusionBar.mouseClicked(mouseX, mouseY, button) |
                   pigmentBar.mouseClicked(mouseX, mouseY, button) | slurryBar.mouseClicked(mouseX, mouseY, button);
        }
        //Otherwise, just send the click event to the corresponding bar
        return currentBar.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    @Override
    public Object getIngredient(double mouseX, double mouseY) {
        GuiChemicalBar<?, ?> currentBar = getCurrentBarNoFallback();
        return currentBar == null ? null : currentBar.getIngredient(mouseX, mouseY);
    }

    @Nullable
    private GuiChemicalBar<?, ?> getCurrentBarNoFallback() {
        return switch (chemicalTank.getCurrent()) {
            case GAS -> gasBar;
            case INFUSION -> infusionBar;
            case PIGMENT -> pigmentBar;
            case SLURRY -> slurryBar;
            default -> null;
        };
    }
}