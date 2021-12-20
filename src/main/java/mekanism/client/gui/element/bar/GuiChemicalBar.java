package mekanism.client.gui.element.bar;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalBar<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends GuiTankBar<STACK> {

    public GuiChemicalBar(IGuiWrapper gui, TankInfoProvider<STACK> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    @Override
    protected boolean isEmpty(STACK stack) {
        return stack.isEmpty();
    }

    @Nullable
    @Override
    protected TankType getType(STACK stack) {
        CHEMICAL type = getHandler().getStack().getType();
        if (type instanceof Gas) {
            return TankType.GAS_TANK;
        } else if (type instanceof InfuseType) {
            return TankType.INFUSION_TANK;
        } else if (type instanceof Pigment) {
            return TankType.PIGMENT_TANK;
        } else if (type instanceof Slurry) {
            return TankType.SLURRY_TANK;
        }
        return null;
    }

    @Override
    protected List<ITextComponent> getTooltip(STACK stack) {
        List<ITextComponent> tooltips = super.getTooltip(stack);
        ChemicalUtil.addChemicalDataToTooltip(tooltips, stack.getType(), Minecraft.getInstance().options.advancedItemTooltips);
        return tooltips;
    }

    @Override
    protected void applyRenderColor(STACK stack) {
        MekanismRenderer.color(stack);
    }

    @Override
    protected TextureAtlasSprite getIcon(STACK stack) {
        return MekanismRenderer.getChemicalTexture(stack.getType());
    }

    public static <STACK extends ChemicalStack<?>, TANK extends IChemicalTank<?, STACK>> TankInfoProvider<STACK> getProvider(TANK tank, List<TANK> tanks) {
        return new TankInfoProvider<STACK>() {
            @Nonnull
            @Override
            public STACK getStack() {
                return tank.getStack();
            }

            @Override
            public int getTankIndex() {
                return tanks.indexOf(tank);
            }

            @Override
            public ITextComponent getTooltip() {
                if (tank.isEmpty()) {
                    return MekanismLang.EMPTY.translate();
                } else if (tank.getStored() == Long.MAX_VALUE) {
                    return MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE);
                }
                return MekanismLang.GENERIC_STORED_MB.translate(tank.getType(), TextUtils.format(tank.getStored()));
            }

            @Override
            public double getLevel() {
                return tank.getStored() / (double) tank.getCapacity();
            }
        };
    }
}