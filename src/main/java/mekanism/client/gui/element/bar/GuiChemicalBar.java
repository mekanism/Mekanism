package mekanism.client.gui.element.bar;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalBar extends GuiTankBar<ChemicalStack> {

    public GuiChemicalBar(IGuiWrapper gui, TankInfoProvider<ChemicalStack> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    @Override
    protected boolean isEmpty(ChemicalStack stack) {
        return stack.isEmpty();
    }

    @Nullable
    @Override
    protected TankType getType(ChemicalStack stack) {
        return TankType.CHEMICAL_TANK;
    }

    @Override
    protected List<Component> getTooltip(ChemicalStack stack) {
        List<Component> tooltips = super.getTooltip(stack);
        stack.appendHoverText(TooltipContext.of(minecraft.level), tooltips, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        return tooltips;
    }

    @Override
    protected void applyRenderColor(GuiGraphics guiGraphics, ChemicalStack stack) {
        MekanismRenderer.color(guiGraphics, stack);
    }

    @Override
    protected TextureAtlasSprite getIcon(ChemicalStack stack) {
        return MekanismRenderer.getChemicalTexture(stack);
    }

    public static TankInfoProvider<ChemicalStack> getProvider(IChemicalTank tank, List<IChemicalTank> tanks) {
        return new TankInfoProvider<>() {
            @NotNull
            @Override
            public ChemicalStack getStack() {
                return tank.getStack();
            }

            @Override
            public int getTankIndex() {
                return tanks.indexOf(tank);
            }

            @Override
            public Component getTooltip() {
                if (tank.isEmpty()) {
                    return MekanismLang.EMPTY.translate();
                } else if (tank.getStored() == Long.MAX_VALUE) {
                    return MekanismLang.GENERIC_STORED.translate(tank.getStack(), MekanismLang.INFINITE);
                }
                return MekanismLang.GENERIC_STORED_MB.translate(tank.getStack(), TextUtils.format(tank.getStored()));
            }

            @Override
            public double getLevel() {
                return MathUtils.divideToLevel(tank.getStored(), tank.getCapacity());
            }
        };
    }
}