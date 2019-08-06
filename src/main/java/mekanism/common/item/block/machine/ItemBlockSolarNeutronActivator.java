package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Upgrade;
import mekanism.common.block.machine.BlockSolarNeutronActivator;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockSolarNeutronActivator extends ItemBlockAdvancedTooltip implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockSolarNeutronActivator(BlockSolarNeutronActivator block) {
        super(block);
        setMaxStackSize(1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(SecurityUtils.getOwnerDisplay(Minecraft.getInstance().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Dist.CLIENT));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
        }
        list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                 LangUtils.transYesNo(getInventory(itemstack) != null && !getInventory(itemstack).isEmpty()));
        if (ItemDataUtils.hasData(itemstack, "upgrades")) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemstack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                list.add(entry.getKey().getColor() + "- " + entry.getKey().getName() + (entry.getKey().canMultiply() ? ": " + EnumColor.GREY + "x" + entry.getValue() : ""));
            }
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull PlayerEntity player, World world, @Nonnull BlockPos pos, Direction side, float hitX, float hitY,
          float hitZ, @Nonnull BlockState state) {
        BlockPos abovePos = pos.up();
        if (!world.isValid(abovePos) || !world.getBlockState(abovePos).getBlock().isReplaceable(world, abovePos)) {
            //If there isn't room then fail
            return false;
        }
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);
    }
}