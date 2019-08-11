package mekanism.common.item.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import mcmultipart.api.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.block.transmitter.BlockRestrictiveTransporter;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Optional;

public class ItemBlockRestrictiveTransporter extends ItemBlockMultipartAble<BlockRestrictiveTransporter> {

    public ItemBlockRestrictiveTransporter(BlockRestrictiveTransporter block) {
        super(block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hold"), " ", EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getKey(),
                  EnumColor.GREY, " ", Translation.of("mekanism.tooltip.for_details"), "."));
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_GREY, Translation.of("mekanism.tooltip.capableTrans"), ":"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.PURPLE, Translation.of("mekanism.tooltip.items"), " (",
                  Translation.of("mekanism.tooltip.universal"), ")"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.PURPLE, Translation.of("mekanism.tooltip.blocks"), " (",
                  Translation.of("mekanism.tooltip.universal"), ")"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.DARK_RED, Translation.of("mekanism.tooltip.restrictiveDesc")));
        }
    }

    @Override
    @Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected IMultipart getMultiPart() {
        //TODO
        return MultipartMekanism.TRANSMITTER_MP;
    }
}