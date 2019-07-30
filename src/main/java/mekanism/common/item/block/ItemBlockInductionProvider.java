package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.block.basic.BlockInductionProvider;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.item.ITieredItem;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockInductionProvider extends ItemBlockMekanism implements ITieredItem<InductionProviderTier> {

    public ItemBlockInductionProvider(BlockInductionProvider block) {
        super(block);
    }

    @Nullable
    @Override
    public InductionProviderTier getTier(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockInductionProvider) {
            return ((BlockInductionProvider) ((ItemBlockInductionProvider) item).block).getTier();
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        if (block instanceof IBlockDescriptive) {
            if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
                InductionProviderTier tier = getTier(itemstack);
                if (tier != null) {
                    list.add(tier.getBaseTier().getColor() + LangUtils.localize("tooltip.outputRate") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(tier.getOutput()));
                }
                list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                         EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
            } else {
                list.addAll(MekanismUtils.splitTooltip(((IBlockDescriptive) block).getDescription(), itemstack));
            }
        }
    }
}