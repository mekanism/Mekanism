package mekanism.client.gui.element.bar;

import com.google.common.primitives.Ints;
import java.util.List;
import mekanism.api.fluid.IFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: I think neo might have added a way to declare tooltips for fluids, if so we should gather those here
public class GuiFluidBar extends GuiTankBar<FluidResource, IFluidTank> {

    public GuiFluidBar(IGuiWrapper gui, ResourceTankInfoProvider<FluidResource, IFluidTank> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    @Override
    protected TankType getType() {
        return TankType.FLUID_TANK;
    }

    @Override
    protected int getRenderColor(FluidResource resource, long amount) {
    //todo: not this?
        return MekanismRenderer.color(resource.toStack(Ints.saturatedCast(amount)));
    }

    @Override
    protected TextureAtlasSprite getIcon(FluidResource resource) {
        return MekanismRenderer.getFluidTexture(resource, FluidTextureType.STILL);
    }

    @Override
    protected List<Component> getContentsTooltips(FluidResource type, long amount, TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag) {
        List<Component> tooltip = super.getContentsTooltips(type, amount, context, player, tooltipFlag);
        FluidStack stack = type.toStack(Ints.saturatedCast(amount));
        if (!stack.isEmpty()) {
            //TODO - 26.1: Re-evaluate how tooltips are done for fluids and see if this is correct (especially in relation to it potentially showing the name twice)
            // We might also want to update chemicals to be similar to this in that there is a getter on the stack rather than a method that appends to list on the stack
            tooltip.addAll(stack.getTooltipLines(context, player, tooltipFlag));
        }
        return tooltip;
    }

    @Override
    protected Object toIngredientStack(FluidResource resource, long amount) {
        return resource.toStack(Ints.saturatedCast(amount));
    }
}