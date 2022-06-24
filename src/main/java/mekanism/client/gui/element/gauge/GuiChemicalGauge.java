package mekanism.client.gui.element.gauge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.math.MathUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class GuiChemicalGauge<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends GuiTankGauge<CHEMICAL, TANK> {

    protected Component label;

    public GuiChemicalGauge(ITankInfoHandler<TANK> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY, TankType tankType) {
        super(type, gui, x, y, sizeX, sizeY, handler, tankType);
    }

    public GuiChemicalGauge(Supplier<TANK> tankSupplier, Supplier<List<TANK>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, TankType tankType) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2, tankType);
    }

    public GuiChemicalGauge(Supplier<TANK> tankSupplier, Supplier<List<TANK>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY,
          TankType tankType) {
        this(new ITankInfoHandler<>() {
            @Nullable
            @Override
            public TANK getTank() {
                return tankSupplier.get();
            }

            @Override
            public int getTankIndex() {
                TANK tank = getTank();
                return tank == null ? -1 : tanksSupplier.get().indexOf(tank);
            }
        }, type, gui, x, y, sizeX, sizeY, tankType);
    }

    public GuiChemicalGauge<CHEMICAL, STACK, TANK> setLabel(Component label) {
        this.label = label;
        return this;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        TANK tank = getTank();
        if (tank == null || tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        double scale = tank.getStored() / (double) tank.getCapacity();
        return MathUtils.clampToInt(Math.round(scale * (height - 2)));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getChemicalTexture(dummyType);
        }
        return getTank() == null || getTank().isEmpty() ? null : MekanismRenderer.getChemicalTexture(getTank().getType());
    }

    @Override
    public Component getLabel() {
        return label;
    }

    @Override
    public List<Component> getTooltipText() {
        if (dummy) {
            return Collections.singletonList(TextComponentUtil.build(dummyType));
        }
        TANK tank = getTank();
        if (tank == null || tank.isEmpty()) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        List<Component> list = new ArrayList<>();
        long amount = tank.getStored();
        if (amount == Long.MAX_VALUE) {
            list.add(MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE));
        } else {
            list.add(MekanismLang.GENERIC_STORED_MB.translate(tank.getType(), TextUtils.format(amount)));
        }
        ChemicalUtil.addChemicalDataToTooltip(list, tank.getType(), Minecraft.getInstance().options.advancedItemTooltips);
        return list;
    }

    @Override
    protected void applyRenderColor() {
        if (dummy || getTank() == null) {
            MekanismRenderer.color(dummyType);
        } else {
            MekanismRenderer.color(getTank().getStack());
        }
    }

    @Nullable
    @Override
    public Object getIngredient(double mouseX, double mouseY) {
        return getTank().isEmpty() ? null : getTank().getStack();
    }
}