package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Upgrade;
import mekanism.common.block.machine.BlockLogisticalSorter;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockLogisticalSorter extends ItemBlockAdvancedTooltip<BlockLogisticalSorter> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockLogisticalSorter(BlockLogisticalSorter block) {
        super(block, new Item.Properties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack))));
        tooltip.add(TextComponentUtil.build(EnumColor.GREY, Translation.of("mekanism.gui.security"), ": ", SecurityUtils.getSecurity(itemstack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(TextComponentUtil.build(EnumColor.RED, "(", Translation.of("mekanism.gui.overridden"), ")"));
        }
        if (ItemDataUtils.hasData(itemstack, "upgrades")) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemstack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                tooltip.add(TextComponentUtil.build(UpgradeDisplay.of(entry.getKey(), entry.getValue())));
            }
        }
    }
}