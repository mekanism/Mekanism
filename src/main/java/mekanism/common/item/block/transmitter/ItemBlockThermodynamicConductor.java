package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.tier.ConductorTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockThermodynamicConductor extends ItemBlockMultipartAble<BlockThermodynamicConductor> {

    public ItemBlockThermodynamicConductor(BlockThermodynamicConductor block) {
        super(block);
    }

    @NotNull
    @Override
    public ConductorTier getTier() {
        return Attribute.getTier(getBlock(), ConductorTier.class);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
            tooltip.add(MekanismLang.HEAT.translateColored(EnumColor.PURPLE, MekanismLang.MEKANISM));
        } else {
            ConductorTier tier = getTier();
            tooltip.add(MekanismLang.CONDUCTION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getInverseConduction()));
            tooltip.add(MekanismLang.INSULATION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getBaseConductionInsulation()));
            tooltip.add(MekanismLang.HEAT_CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getHeatCapacity()));
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }
}