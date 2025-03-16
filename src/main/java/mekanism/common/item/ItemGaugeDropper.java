package mekanism.common.item;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.chemical.merged.MergedTankCreator;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class ItemGaugeDropper extends Item {

    public static final MergedTankCreator MERGED_TANK_CREATOR = new MergedTankCreator(
          (type, attachedTo, containerIndex) -> new ComponentBackedChemicalTank(attachedTo, containerIndex,
                ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity, null
          ),
          (type, attachedTo, containerIndex) -> new ComponentBackedFluidTank(attachedTo, containerIndex,
                ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity
          )
    );

    public ItemGaugeDropper(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        FluidStack fluid = StorageUtils.getFirstFluidFromAttachment(stack);
        if (!fluid.isEmpty()) {
            return FluidUtils.getRGBDurabilityForDisplay(stack);
        }
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                IFluidHandlerItem fluidHandler = Capabilities.FLUID.getCapability(stack);
                if (fluidHandler instanceof IExtendedFluidHandler fluidHandlerItem) {
                    for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                        fluidHandlerItem.setFluidInTank(tank, FluidStack.EMPTY);
                    }
                }
                IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
                if (handler != null) {
                    for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                        handler.setChemicalInTank(tank, ChemicalStack.EMPTY);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredSubstance(stack, tooltip, false);
    }
}