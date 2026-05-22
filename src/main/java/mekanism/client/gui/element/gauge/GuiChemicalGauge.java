package mekanism.client.gui.element.gauge;

import com.google.common.primitives.Ints;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalGauge extends GuiTankGauge<ChemicalResource, IChemicalTank> {

    public static GuiChemicalGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiChemicalGauge gauge = new GuiChemicalGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

    public GuiChemicalGauge(@Nullable ITankInfoHandler<IChemicalTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY, handler, TankType.CHEMICAL_TANK, ChemicalResource.EMPTY);
    }

    public GuiChemicalGauge(Supplier<IChemicalTank> tankSupplier, Supplier<List<IChemicalTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(tankSupplier, tanksSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiChemicalGauge(Supplier<IChemicalTank> tankSupplier, Supplier<List<IChemicalTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        this(getInfoHandler(tankSupplier, tanksSupplier), type, gui, x, y, sizeX, sizeY);
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        ChemicalResource type = getTypeOrDummy();
        return type.isEmpty() ? null : MekanismRenderer.getChemicalTexture(type);
    }

    @Override
    protected List<Component> getContentsTooltips(ChemicalResource resource, long amount, TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag) {
        List<Component> tooltip = super.getContentsTooltips(resource, amount, context, player, tooltipFlag);
        ChemicalStack stack = resource.toStack(Ints.saturatedCast(amount));
        if (!stack.isEmpty()) {
            stack.appendHoverText(context, tooltip, tooltipFlag);
        }
        return tooltip;
    }

    @Override
    protected int getRenderColor() {
        return MekanismRenderer.color(getTypeOrDummy());
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        IChemicalTank tank = getContainer();
        return tank == null || tank.isEmpty() ? Optional.empty() : Optional.of(tank.resource().toStack(tank.amountAsInt()));
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.CHEMICAL;
    }
}