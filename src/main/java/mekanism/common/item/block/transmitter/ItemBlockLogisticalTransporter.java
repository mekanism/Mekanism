package mekanism.common.item.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.block.transmitter.BlockLogisticalTransporter;
import mekanism.common.item.ITieredItem;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.tier.TransporterTier;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockLogisticalTransporter extends ItemBlockMultipartAble<BlockLogisticalTransporter> implements ITieredItem<TransporterTier> {

    public ItemBlockLogisticalTransporter(BlockLogisticalTransporter block) {
        super(block);
    }

    @Nullable
    @Override
    public TransporterTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockLogisticalTransporter) {
            return ((ItemBlockLogisticalTransporter) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            TransporterTier tier = getTier(stack);
            if (tier != null) {
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.speed"), ": ", EnumColor.GRAY, (tier.getSpeed() / (100 / 20)) + " m/s"));
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.pumpRate"), ": ", EnumColor.GRAY, tier.getPullAmount() * 2 + "/s"));
            }
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.hold"), " ", EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getKey(),
                  EnumColor.GRAY, " ", Translation.of("tooltip.mekanism.for_details"), "."));
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_GRAY, Translation.of("tooltip.mekanism.capableTrans"), ":"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.PURPLE, Translation.of("tooltip.mekanism.items"), " (",
                  Translation.of("tooltip.mekanism.universal"), ")"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.PURPLE, Translation.of("tooltip.mekanism.blocks"), " (",
                  Translation.of("tooltip.mekanism.universal"), ")"));
        }
    }
}