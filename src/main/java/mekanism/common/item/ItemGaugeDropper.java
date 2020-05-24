package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.gas.GasHandlerWrapper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionHandlerWrapper;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentHandlerWrapper;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryHandlerWrapper;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GaugeDropperContentsHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemGaugeDropper extends Item {

    public ItemGaugeDropper(Properties properties) {
        super(properties.maxStackSize(1));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return super.getDisplayName(stack).applyTextStyle(EnumColor.AQUA.textFormatting);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        //TODO: Technically doesn't support things where the color is part of the texture such as lava
        GasStack gasStack = StorageUtils.getStoredGasFromNBT(stack);
        if (!gasStack.isEmpty()) {
            return gasStack.getChemicalTint();
        }
        InfusionStack infusionStack = StorageUtils.getStoredInfusionFromNBT(stack);
        if (!infusionStack.isEmpty()) {
            return infusionStack.getChemicalTint();
        }
        PigmentStack pigmentStack = StorageUtils.getStoredPigmentFromNBT(stack);
        if (!pigmentStack.isEmpty()) {
            return pigmentStack.getChemicalTint();
        }
        SlurryStack slurryStack = StorageUtils.getStoredSlurryFromNBT(stack);
        if (!slurryStack.isEmpty()) {
            return slurryStack.getChemicalTint();
        }
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluidStack.isEmpty()) {
            return fluidStack.getFluid().getAttributes().getColor(fluidStack);
        }
        return 0;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && !world.isRemote) {
            Optional<IFluidHandlerItem> fluidCapability = MekanismUtils.toOptional(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
            if (fluidCapability.isPresent()) {
                IFluidHandlerItem fluidHandler = fluidCapability.get();
                if (fluidHandler instanceof IExtendedFluidHandler) {
                    IExtendedFluidHandler fluidHandlerItem = (IExtendedFluidHandler) fluidHandler;
                    for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                        fluidHandlerItem.setFluidInTank(tank, FluidStack.EMPTY);
                    }
                }
            }
            clearChemicalTanks(stack, GasStack.EMPTY, Capabilities.GAS_HANDLER_CAPABILITY, GasHandlerWrapper::new);
            clearChemicalTanks(stack, InfusionStack.EMPTY, Capabilities.INFUSION_HANDLER_CAPABILITY, InfusionHandlerWrapper::new);
            clearChemicalTanks(stack, PigmentStack.EMPTY, Capabilities.PIGMENT_HANDLER_CAPABILITY, PigmentHandlerWrapper::new);
            clearChemicalTanks(stack, SlurryStack.EMPTY, Capabilities.SLURRY_HANDLER_CAPABILITY, SlurryHandlerWrapper::new);
            ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private static <HANDLER, CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void clearChemicalTanks(ItemStack stack, STACK empty,
          Capability<HANDLER> capability, Function<HANDLER, IChemicalHandlerWrapper<CHEMICAL, STACK>> wrapperCreator) {
        Optional<HANDLER> cap = MekanismUtils.toOptional(stack.getCapability(capability));
        if (cap.isPresent()) {
            IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper = wrapperCreator.apply(cap.get());
            for (int tank = 0; tank < wrapper.getTanks(); tank++) {
                wrapper.setChemicalInTank(tank, empty);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        GasStack gasStack = StorageUtils.getStoredGasFromNBT(stack);
        InfusionStack infusionStack = StorageUtils.getStoredInfusionFromNBT(stack);
        PigmentStack pigmentStack = StorageUtils.getStoredPigmentFromNBT(stack);
        SlurryStack slurryStack = StorageUtils.getStoredSlurryFromNBT(stack);
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (gasStack.isEmpty() && infusionStack.isEmpty() && pigmentStack.isEmpty() && slurryStack.isEmpty() && fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translate());
        } else if (!gasStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(gasStack, gasStack.getAmount()));
        } else if (!infusionStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(infusionStack, infusionStack.getAmount()));
        } else if (!pigmentStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(pigmentStack, pigmentStack.getAmount()));
        } else if (!slurryStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(slurryStack, slurryStack.getAmount()));
        } else if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.STORED.translate(fluidStack, fluidStack.getAmount()));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, GaugeDropperContentsHandler.create());
    }
}