package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.listener.ConfigBasedCachedLongSupplier;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit.SprintBoost;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@ParametersAreNotNullByDefault
public record ModuleGravitationalModulatingUnit(SprintBoost speedBoost) implements ICustomModule<ModuleGravitationalModulatingUnit> {

    public static final int BOOST_ENERGY_MULTIPLIER = 4;

    private static final AttributeModifier CREATIVE_FLIGHT_MODIFIER = new AttributeModifier(Mekanism.rl("mekasuit_gravitational_modulation"), 1, Operation.ADD_VALUE);
    private static final ConfigBasedCachedLongSupplier BOOST_USAGE = new ConfigBasedCachedLongSupplier(
          () -> BOOST_ENERGY_MULTIPLIER * MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get(),
          MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation
    );
    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "gravitational_modulation_unit.png");
    private static final Vec3 BOOST_VEC = new Vec3(0, 0, 1);
    public static final ResourceLocation SPEED_BOOST = Mekanism.rl("speed_boost");

    public ModuleGravitationalModulatingUnit(IModule<ModuleGravitationalModulatingUnit> module) {
        this(module.<SprintBoost>getConfigOrThrow(SPEED_BOOST).get());
    }

    @Override
    public void addHUDElements(IModule<ModuleGravitationalModulatingUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementEnabled(icon, module.isEnabled()));
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleGravitationalModulatingUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleGravitationalModulatingUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(moduleContainer, stack, player, MekanismLang.MODULE_GRAVITATIONAL_MODULATION.translate());
    }

    @Override
    public void adjustAttributes(IModule<ModuleGravitationalModulatingUnit> module, ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(MekanismItems.MEKASUIT_BODYARMOR) && module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation)) {
            event.addModifier(NeoForgeMod.CREATIVE_FLIGHT, CREATIVE_FLIGHT_MODIFIER, EquipmentSlotGroup.CHEST);
        }
    }

    @Override
    public void tickClient(IModule<ModuleGravitationalModulatingUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        //Client side handling of boost as movement needs to be applied on both the server and the client
        if (shouldProcess(player) && MekanismKeyHandler.boostKey.isDown() && module.hasEnoughEnergy(stack, BOOST_USAGE)) {
            float boost = speedBoost.getBoost();
            if (boost > 0) {
                player.moveRelative(boost, BOOST_VEC);
            }
        }
    }

    @Override
    public void tickServer(IModule<ModuleGravitationalModulatingUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        //If the player is actively flying (not just allowed to), they are using the grav unit, apply movement boost if active, and use energy
        // Note: If they don't have enough energy to use the grav unit, don't try to process the player, and assume another mod is providing flight
        if (shouldProcess(player) && module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation)) {
            float boost = speedBoost.getBoost();
            if (boost > 0 && Mekanism.keyMap.has(player.getUUID(), KeySync.BOOST) && module.hasEnoughEnergy(stack, BOOST_USAGE)) {
                player.moveRelative(boost, BOOST_VEC);
                module.useEnergy(player, stack, BOOST_USAGE.getAsLong());
                gravUnitGameEvent(player, MekanismGameEvents.GRAVITY_MODULATE_BOOSTED);
            } else {
                module.useEnergy(player, stack, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get());
                gravUnitGameEvent(player, MekanismGameEvents.GRAVITY_MODULATE);
            }
        }
    }

    private static void gravUnitGameEvent(Player player, Holder<GameEvent> gameEvent) {
        if (MekanismConfig.gear.mekaSuitGravitationalVibrations.get() && player.level().getGameTime() % MekanismUtils.TICKS_PER_HALF_SECOND == 0) {
            player.gameEvent(gameEvent);
        }
    }

    public static boolean shouldProcess(Player player) {
        //only process flying players that are not in creative or spectator
        return player.getAbilities().flying && MekanismUtils.isPlayingMode(player);
    }
}