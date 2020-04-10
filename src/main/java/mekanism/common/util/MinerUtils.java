package mekanism.common.util;

import java.util.Collections;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

public final class MinerUtils {

    public static List<ItemStack> getDrops(ServerWorld world, BlockPos pos, boolean silk, BlockPos minerPosition) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir(world, pos)) {
            return Collections.emptyList();
        }
        ItemStack stack = MekanismItems.ATOMIC_DISASSEMBLER.getItemStack();
        if (silk) {
            stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
        }
        LootContext.Builder lootContextBuilder = new LootContext.Builder(world)
              .withRandom(world.rand)
              .withParameter(LootParameters.POSITION, pos)
              .withParameter(LootParameters.TOOL, stack)
              .withNullableParameter(LootParameters.THIS_ENTITY, Mekanism.proxy.getDummyPlayer(world, minerPosition).get())
              .withNullableParameter(LootParameters.BLOCK_ENTITY, MekanismUtils.getTileEntity(world, pos));
        return state.getDrops(lootContextBuilder);
    }
}