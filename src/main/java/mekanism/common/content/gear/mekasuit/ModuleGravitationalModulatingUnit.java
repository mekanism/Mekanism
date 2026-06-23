package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
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
import mekanism.common.config.listener.ConfigBasedCachedIntSupplier;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit.SprintBoost;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public record ModuleGravitationalModulatingUnit(SprintBoost speedBoost) implements ICustomModule<ModuleGravitationalModulatingUnit> {

    public static final int BOOST_ENERGY_MULTIPLIER = 4;

    private static final AttributeModifier CREATIVE_FLIGHT_MODIFIER = new AttributeModifier(Mekanism.rl("mekasuit_gravitational_modulation"), 1, Operation.ADD_VALUE);
    private static final ConfigBasedCachedIntSupplier BOOST_USAGE = new ConfigBasedCachedIntSupplier(
          () -> BOOST_ENERGY_MULTIPLIER * MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get(),
          MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation
    );
    private static final Identifier icon = Mekanism.rl("hud/gravitational_modulation_unit");
    private static final Vec3 BOOST_VEC = new Vec3(0, 0, 1);
    public static final Identifier SPEED_BOOST = Mekanism.rl("speed_boost");

    public ModuleGravitationalModulatingUnit(IModule<ModuleGravitationalModulatingUnit> module) {
        this(module.<SprintBoost>getConfigOrThrow(SPEED_BOOST).get());
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(IModule<ModuleGravitationalModulatingUnit> module, IModuleContainer moduleContainer,
          ITEM instance, Player player, Consumer<IHUDElement> hudElementAdder) {
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementEnabled(icon, module.isEnabled()));
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleGravitationalModulatingUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleGravitationalModulatingUnit> module, Player player, ItemAccess itemAccess,
          int shift, boolean displayChangeMessage, @Nullable TransactionContext transaction) {
        module.toggleEnabled(itemAccess, player, MekanismLang.MODULE_GRAVITATIONAL_MODULATION.translate(), transaction);
    }

    @Override
    public void adjustAttributes(IModule<ModuleGravitationalModulatingUnit> module, IModuleContainer moduleContainer, ItemAttributeModifierEvent event) {
        if (module.hasEnoughEnergy(ItemAccessUtils.sideEffectFreeAccess(event.getItemStack()), MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation)) {
            event.addModifier(NeoForgeMod.CREATIVE_FLIGHT, CREATIVE_FLIGHT_MODIFIER, EquipmentSlotGroup.CHEST);
        }
    }

    @Override
    public void tickClient(IModule<ModuleGravitationalModulatingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        //Client side handling of boost as movement needs to be applied on both the server and the client
        if (shouldProcess(player) && MekanismKeyHandler.boostKey.isDown() && module.hasEnoughEnergy(player, itemAccess, BOOST_USAGE.getAsInt(), transaction)) {
            float boost = speedBoost.getBoost();
            if (boost > 0) {
                player.moveRelative(boost, BOOST_VEC);
            }
        }
    }

    @Override
    public void tickServer(IModule<ModuleGravitationalModulatingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        //If the player is actively flying (not just allowed to), they are using the grav unit, apply movement boost if active, and use energy
        // Note: If they don't have enough energy to use the grav unit, don't try to process the player, and assume another mod is providing flight
        if (shouldProcess(player)) {
            int energyUsage = MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get();
            if (module.useAllEnergy(player, itemAccess, energyUsage, transaction)) {
                float boost = speedBoost.getBoost();
                Holder<GameEvent> gameEvent = MekanismGameEvents.GRAVITY_MODULATE;
                if (boost > 0 && Mekanism.keyMap.has(player.getUUID(), KeySync.BOOST)) {
                    //Note: Boost usage is a multiplicative amount of our energy usage, as we have already extracted our energy once,
                    // we need to subtract it from our attempted boost handling
                    int energyToBoost = BOOST_USAGE.getAsInt() - energyUsage;
                    if (module.useAllEnergy(player, itemAccess, energyToBoost, transaction)) {
                        player.moveRelative(boost, BOOST_VEC);
                        gameEvent = MekanismGameEvents.GRAVITY_MODULATE_BOOSTED;
                    }
                }
                gravUnitGameEvent(player, gameEvent);
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