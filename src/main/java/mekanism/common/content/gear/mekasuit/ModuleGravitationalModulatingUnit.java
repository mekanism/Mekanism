package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.listener.ConfigBasedCachedFLSupplier;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit.SprintBoost;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

@ParametersAreNotNullByDefault
public class ModuleGravitationalModulatingUnit implements ICustomModule<ModuleGravitationalModulatingUnit> {

    private static final ConfigBasedCachedFLSupplier BOOST_USAGE = new ConfigBasedCachedFLSupplier(
          () -> MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get().multiply(4),
          MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation
    );
    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "gravitational_modulation_unit.png");
    private static final Vec3 BOOST_VEC = new Vec3(0, 0, 1);

    // we share with locomotive boosting unit
    private IModuleConfigItem<SprintBoost> speedBoost;

    @Override
    public void init(IModule<ModuleGravitationalModulatingUnit> module, ModuleConfigItemCreator configItemCreator) {
        speedBoost = configItemCreator.createConfigItem("speed_boost", MekanismLang.MODULE_SPEED_BOOST, new ModuleEnumData<>(SprintBoost.LOW));
    }

    @Override
    public void addHUDElements(IModule<ModuleGravitationalModulatingUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementEnabled(icon, module.isEnabled()));
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleGravitationalModulatingUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleGravitationalModulatingUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(player, MekanismLang.MODULE_GRAVITATIONAL_MODULATION.translate());
    }

    public float getBoost() {
        return speedBoost.get().getBoost();
    }

    @Override
    public void tickClient(IModule<ModuleGravitationalModulatingUnit> module, Player player) {
        //Client side handling of boost as movement needs to be applied on both the server and the client
        if (shouldProcess(player) && MekanismKeyHandler.boostKey.isDown() && module.hasEnoughEnergy(BOOST_USAGE)) {
            float boost = getBoost();
            if (boost > 0) {
                player.moveRelative(boost, BOOST_VEC);
            }
        }
    }

    @Override
    public void tickServer(IModule<ModuleGravitationalModulatingUnit> module, Player player) {
        //If the player is actively flying (not just allowed to), they are using the grav unit, apply movement boost if active, and use energy
        // Note: If they don't have enough energy to use the grav unit, don't try to process the player, and assume another mod is providing flight
        if (shouldProcess(player) && module.hasEnoughEnergy(MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation)) {
            float boost = getBoost();
            if (boost > 0 && Mekanism.keyMap.has(player.getUUID(), KeySync.BOOST) && module.hasEnoughEnergy(BOOST_USAGE)) {
                player.moveRelative(boost, BOOST_VEC);
                module.useEnergy(player, BOOST_USAGE.get());
                gravUnitGameEvent(player, MekanismGameEvents.GRAVITY_MODULATE_BOOSTED);
            } else {
                module.useEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get());
                gravUnitGameEvent(player, MekanismGameEvents.GRAVITY_MODULATE);
            }
        }
    }

    private static void gravUnitGameEvent(Player player, Holder<GameEvent> gameEvent) {
        if (MekanismConfig.gear.mekaSuitGravitationalVibrations.get() && player.level().getGameTime() % MekanismUtils.TICKS_PER_HALF_SECOND == 0) {
            player.gameEvent(gameEvent.value());
        }
    }

    public static boolean shouldProcess(Player player) {
        //only process flying players that are not in creative or spectator
        return player.getAbilities().flying && MekanismUtils.isPlayingMode(player);
    }
}