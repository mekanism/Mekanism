package mekanism.common.integration.lookingat.hwyla;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mekanism.common.Mekanism;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

@WailaPlugin
public class MekanismHwylaPlugin implements IWailaPlugin {

    public static final ResourceLocation HWLYA_TOOLTIP = Mekanism.rl("hwlya_tooltip");
    public static final String TEXT = "text";
    public static final String CHEMICAL_STACK = "chemical";

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerBlockDataProvider(HwylaDataProvider.INSTANCE, TileEntity.class);
        registrar.registerComponentProvider(HwylaTooltipRenderer.INSTANCE, TooltipPosition.BODY, TileEntity.class);
        registrar.registerTooltipRenderer(HWLYA_TOOLTIP, HwylaTooltipRenderer.INSTANCE);
    }
}