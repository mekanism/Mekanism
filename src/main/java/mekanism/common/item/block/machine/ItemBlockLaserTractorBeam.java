package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.machine.BlockLaserTractorBeam;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockLaserTractorBeam extends ItemBlockAdvancedTooltip<BlockLaserTractorBeam> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockLaserTractorBeam(BlockLaserTractorBeam block) {
        super(block, new Item.Properties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack)).getTextComponent());
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("mekanism.gui.security"), ": ", SecurityUtils.getSecurity(itemstack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(TextComponentUtil.build(EnumColor.RED, "(", Translation.of("mekanism.gui.overridden"), ")"));
        }
        ListNBT inventory = getInventory(itemstack);
        tooltip.add(TextComponentUtil.build(EnumColor.AQUA, Translation.of("mekanism.tooltip.inventory"), ": ", EnumColor.GRAY,
              BooleanStateDisplay.YesNo.of(inventory != null && !inventory.isEmpty())));
    }
}