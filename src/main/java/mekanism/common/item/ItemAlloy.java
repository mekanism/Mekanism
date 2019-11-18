package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tier.AlloyTier;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAlloy extends Item {

    private final AlloyTier tier;

    public ItemAlloy(AlloyTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && MekanismConfig.general.allowTransmitterAlloyUpgrade.get()) {
            World world = context.getWorld();
            BlockPos pos = context.getPos();
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            return CapabilityUtils.getCapabilityHelper(tile, Capabilities.ALLOY_INTERACTION_CAPABILITY, context.getFace()).getIfPresentElse(
                  interaction -> {
                      if (!world.isRemote) {
                          Hand hand = context.getHand();
                          interaction.onAlloyInteraction(player, hand, player.getHeldItem(hand), tier.getBaseTier().ordinal());
                      }
                      return ActionResultType.SUCCESS;
                  },
                  ActionResultType.PASS
            );
        }
        return ActionResultType.PASS;
    }

    public AlloyTier getTier() {
        return tier;
    }
}