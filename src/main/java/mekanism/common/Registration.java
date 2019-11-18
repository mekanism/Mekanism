package mekanism.common;

import mekanism.api.gas.Gas;
import mekanism.api.infuse.InfuseType;
import mekanism.common.block.PortalHelper;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemFreeRunners.FreeRunnerMode;
import mekanism.common.item.gear.ItemGasMask;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.RecipeCacheManager;
import mekanism.common.tags.MekanismTagManager;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void buildRegistry(RegistryEvent.NewRegistry event) {
        //TODO: Come up with a better way than just doing it on low to make sure this happens AFTER the registries are initialized?
        Mekanism.instance.setTagManager(new MekanismTagManager());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(PortalHelper.BlockPortalOverride.instance);
    }

    @SubscribeEvent
    public static void registerGases(RegistryEvent.Register<Gas> event) {
        MekanismGases.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerInfuseTypes(RegistryEvent.Register<InfuseType> event) {
        MekanismInfuseTypes.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerFluids(RegistryEvent.Register<Fluid> event) {
        MekanismGases.registerFluids(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        MekanismRecipeType.registerRecipeTypes(event.getRegistry());
        //TODO: Register a custom shaped crafting recipe serializer if needed

        Mekanism.instance.setRecipeCacheManager(new RecipeCacheManager());
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading configEvent) {
        //TODO: Handle reloading
    }

    @SubscribeEvent
    public static void onEntityAttacked(LivingAttackEvent event) {
        LivingEntity base = event.getEntityLiving();
        //Gas Mask checks
        ItemStack headStack = base.getItemStackFromSlot(EquipmentSlotType.HEAD);
        ItemStack chestStack = base.getItemStackFromSlot(EquipmentSlotType.CHEST);
        if (!headStack.isEmpty() && headStack.getItem() instanceof ItemGasMask) {
            if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemScubaTank) {
                ItemScubaTank tank = (ItemScubaTank) chestStack.getItem();
                if (tank.getFlowing(chestStack) && !tank.getGas(chestStack).isEmpty()) {
                    if (event.getSource() == DamageSource.MAGIC) {
                        event.setCanceled(true);
                    }
                }
            }
        }
        //Free runner checks
        ItemStack feetStack = base.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!feetStack.isEmpty() && feetStack.getItem() instanceof ItemFreeRunners) {
            ItemFreeRunners boots = (ItemFreeRunners) feetStack.getItem();
            if (boots.getMode(feetStack) == FreeRunnerMode.NORMAL && boots.getEnergy(feetStack) > 0
                && event.getSource() == DamageSource.FALL) {
                boots.setEnergy(feetStack, boots.getEnergy(feetStack) - event.getAmount() * 50);
                event.setCanceled(true);
            }
        }
    }
}