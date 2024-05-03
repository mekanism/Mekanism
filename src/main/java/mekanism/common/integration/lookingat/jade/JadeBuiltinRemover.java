package mekanism.common.integration.lookingat.jade;

import mekanism.api.NBTConstants;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

public class JadeBuiltinRemover<ACCESSOR extends Accessor<?>> implements IComponentProvider<ACCESSOR> {

    static final JadeBuiltinRemover<?> INSTANCE = new JadeBuiltinRemover<>();

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.REMOVE_BUILTIN;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, ACCESSOR accessor, IPluginConfig config) {
        if (accessor.getServerData().contains(NBTConstants.MEK_DATA, Tag.TAG_LIST)) {
            tooltip.remove(Identifiers.UNIVERSAL_ENERGY_STORAGE);
            tooltip.remove(Identifiers.UNIVERSAL_FLUID_STORAGE);
        }
    }

    @Override
    public int getDefaultPriority() {
        //Run in tail to ensure we are after the provider adding forge energy and fluid
        // so that we can remove it if we are adding our own
        return TooltipPosition.TAIL;
    }
}