package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.IAlloyInteraction;
import mekanism.api.tier.AlloyTier;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

public class ItemAlloy extends Item {

    private final AlloyTier tier;

    public ItemAlloy(AlloyTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && MekanismConfig.general.transmitterAlloyUpgrade.get()) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            LazyOptional<IAlloyInteraction> capability = CapabilityUtils.getCapability(tile, Capabilities.ALLOY_INTERACTION_CAPABILITY, context.getClickedFace());
            if (capability.isPresent()) {
                if (!world.isClientSide) {
                    capability.resolve().get().onAlloyInteraction(player, context.getItemInHand(), tier);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public AlloyTier getTier() {
        return tier;
    }
}