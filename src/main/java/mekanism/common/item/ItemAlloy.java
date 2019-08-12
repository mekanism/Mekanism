package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.tier.AlloyTier;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemAlloy extends ItemMekanism {

    private final AlloyTier tier;

    public ItemAlloy(AlloyTier tier) {
        super("alloy_" + tier.getName());
        this.tier = tier;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && MekanismConfigOld.current().general.allowTransmitterAlloyUpgrade.get()) {
            World world = context.getWorld();
            BlockPos pos = context.getPos();
            TileEntity tile = world.getTileEntity(pos);
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

    @Override
    public void registerOreDict() {
        OreDictionary.registerOre("alloy" + tier.getBaseTier().getSimpleName(), new ItemStack(this));
        if (tier == AlloyTier.ENRICHED) {
            OreDictionary.registerOre("itemEnrichedAlloy", new ItemStack(this));
        }
    }
}