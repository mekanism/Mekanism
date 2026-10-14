package mekanism.common.item;

import mekanism.api.chemical.ChemicalResource;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.component.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.component.containers.creator.IBasicContainerCreator;
import mekanism.common.component.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class ItemGaugeDropper extends Item {

    public static final IBasicContainerCreator<MergedTank> MERGED_TANK_CREATOR = (attachedAccess, containerIndex) -> MergedTank.create(
          new ComponentBackedFluidTank(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDropperCapacity, MekanismConfig.gear.gaugeDroppedTransferRate
          ),
          new ComponentBackedChemicalTank(attachedAccess, containerIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDropperCapacity, MekanismConfig.gear.gaugeDroppedTransferRate
          )
    );

    public ItemGaugeDropper(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (StorageUtils.shouldDisplayBar(stack)) {
            //We also don't display the bar if there is nothing stored in any of the containers
            ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
            if (handler != null && !ResourceHandlerUtil.isEmpty(handler)) {
                return true;
            }
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
            return fluidHandler != null && !ResourceHandlerUtil.isEmpty(fluidHandler);
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
        return Math.max(ContainerType.CHEMICAL.getBarWidth(itemAccess), ContainerType.FLUID.getBarWidth(itemAccess));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
        FluidResource fluid = ContainerType.FLUID.getFirstResourceFromAttachment(itemAccess);
        if (!fluid.isEmpty()) {
            return ContainerType.FLUID.getRGBDurabilityForDisplay(fluid);
        }
        return ContainerType.CHEMICAL.getRGBDurabilityForDisplay(itemAccess);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        } else if (level.isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        }
        BlockPos pos = player.blockPosition();
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        ContainerType.FLUID.tryDumpContents(level, pos, itemAccess, null);
        ContainerType.CHEMICAL.tryDumpContents(level, pos, itemAccess, null);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
    }
}