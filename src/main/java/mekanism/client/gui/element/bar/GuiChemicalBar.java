package mekanism.client.gui.element.bar;

import com.google.common.primitives.Ints;
import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalBar extends GuiTankBar<ChemicalResource, IChemicalTank> {

    public GuiChemicalBar(IGuiWrapper gui, ResourceTankInfoProvider<ChemicalResource, IChemicalTank> infoProvider, int x, int y, int width, int height, boolean horizontal) {
        super(gui, infoProvider, x, y, width, height, horizontal);
    }

    @Override
    protected TankType getType() {
        return TankType.CHEMICAL_TANK;
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
    protected int getRenderColor(ChemicalResource resource, long amount) {
        return MekanismRenderer.color(resource);
    }

    @Override
    protected TextureAtlasSprite getIcon(ChemicalResource resource) {
        return MekanismRenderer.getChemicalTexture(resource);
    }

    @Override
    protected Object toIngredientStack(ChemicalResource resource, long amount) {
        return resource.toStack(Ints.saturatedCast(amount));
    }
}