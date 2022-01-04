package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.merged.GaugeDropperContentsHandler;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemGaugeDropper extends Item {

    public ItemGaugeDropper(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isBarVisible(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@Nonnull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@Nonnull ItemStack stack) {
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluidStack.isEmpty()) {
            //TODO: Technically doesn't support things where the color is part of the texture such as lava
            // for chemicals it is supported via allowing people to override getColorRepresentation in their
            // chemicals
            if (fluidStack.getFluid().isSame(Fluids.LAVA)) {
                //Special case lava
                return 0xFFDB6B19;
            }
            return fluidStack.getFluid().getAttributes().getColor(fluidStack);
        }
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                Optional<IFluidHandlerItem> fluidCapability = FluidUtil.getFluidHandler(stack).resolve();
                if (fluidCapability.isPresent()) {
                    IFluidHandlerItem fluidHandler = fluidCapability.get();
                    if (fluidHandler instanceof IExtendedFluidHandler) {
                        IExtendedFluidHandler fluidHandlerItem = (IExtendedFluidHandler) fluidHandler;
                        for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                            fluidHandlerItem.setFluidInTank(tank, FluidStack.EMPTY);
                        }
                    }
                }
                clearChemicalTanks(stack, GasStack.EMPTY);
                clearChemicalTanks(stack, InfusionStack.EMPTY);
                clearChemicalTanks(stack, PigmentStack.EMPTY);
                clearChemicalTanks(stack, SlurryStack.EMPTY);
                //TODO - 1.18: Re-evaluate usages of this and if we have to override this method in any of our containers
                player.containerMenu.sendAllDataToRemote();
            }
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void clearChemicalTanks(ItemStack stack, STACK empty) {
        Optional<IChemicalHandler<CHEMICAL, STACK>> cap = stack.getCapability(ChemicalUtil.getCapabilityForChemical(empty)).resolve();
        if (cap.isPresent()) {
            IChemicalHandler<CHEMICAL, STACK> handler = cap.get();
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                handler.setChemicalInTank(tank, empty);
            }
        }
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        StorageUtils.addStoredSubstance(stack, tooltip, false);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemCapabilityWrapper(stack, GaugeDropperContentsHandler.create());
    }
}