package mekanism.client.gui.element.gauge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalGauge extends GuiTankGauge<ChemicalStack, IChemicalTank> {

    public static GuiChemicalGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiChemicalGauge gauge = new GuiChemicalGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

    protected Component label;

    public GuiChemicalGauge(ITankInfoHandler<IChemicalTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY, handler, TankType.CHEMICAL_TANK);
        //Ensure it isn't null
        setDummyType(ChemicalStack.EMPTY);
    }

    public GuiChemicalGauge(Supplier<IChemicalTank> tankSupplier, Supplier<List<IChemicalTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiChemicalGauge(Supplier<IChemicalTank> tankSupplier, Supplier<List<IChemicalTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        this(new ITankInfoHandler<>() {
            @Override
            public IChemicalTank getTank() {
                return tankSupplier.get();
            }

            @Override
            public int getTankIndex() {
                IChemicalTank tank = getTank();
                return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
            }
        }, type, gui, x, y, sizeX, sizeY);
    }

    public GuiChemicalGauge setLabel(Component label) {
        this.label = label;
        return this;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        IChemicalTank tank = getTank();
        if (tank == null || tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        double scale = tank.getStored() / (double) tank.getCapacity();
        return MathUtils.clampToInt(Math.max(1, Math.round(scale * (height - 2))));
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        ChemicalStack stack = getStackOrDummy();
        return stack.isEmpty() ? null : MekanismRenderer.getChemicalTexture(stack);
    }

    @Override
    public Component getLabel() {
        return label;
    }

    private ChemicalStack getStackOrDummy() {
        if (dummy) {
            return dummyType;
        }
        IChemicalTank tank = getTank();
        return tank == null ? dummyType : tank.getStack();
    }

    @Override
    public List<Component> getTooltipText() {
        ChemicalStack stack = getStackOrDummy();
        if (stack.isEmpty()) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        List<Component> list = new ArrayList<>();
        long amount = stack.getAmount();
        if (amount == Long.MAX_VALUE) {
            list.add(MekanismLang.GENERIC_STORED.translate(stack, MekanismLang.INFINITE));
        } else {
            list.add(MekanismLang.GENERIC_STORED_MB.translate(stack, TextUtils.format(amount)));
        }
        stack.appendHoverText(TooltipContext.of(minecraft.level), list, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        return list;
    }

    @Override
    protected void applyRenderColor(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, getStackOrDummy());
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        return getTank().isEmpty() ? Optional.empty() : Optional.of(getTank().getStack());
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX() + 1, getY() + 1, width - 2, height - 2);
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.CHEMICAL;
    }
}