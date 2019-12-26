package mekanism.common.item.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.item.ITieredItem;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.tier.ConductorTier;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockThermodynamicConductor extends ItemBlockMultipartAble<BlockThermodynamicConductor> implements ITieredItem<ConductorTier> {

    public ItemBlockThermodynamicConductor(BlockThermodynamicConductor block) {
        super(block);
    }

    @Nullable
    @Override
    public ConductorTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockThermodynamicConductor) {
            return ((ItemBlockThermodynamicConductor) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            ConductorTier tier = getTier(stack);
            if (tier != null) {
                tooltip.add(MekanismLang.CONDUCTION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getInverseConduction()));
                tooltip.add(MekanismLang.INSULATION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getBaseConductionInsulation()));
                tooltip.add(MekanismLang.HEAT_CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getInverseHeatCapacity()));
            }
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getLocalizedName()));
        } else {
            tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
            tooltip.add(MekanismLang.HEAT.translateColored(EnumColor.PURPLE, MekanismLang.MEKANISM));
        }
    }
}