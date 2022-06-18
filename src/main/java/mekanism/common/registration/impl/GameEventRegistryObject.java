package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.RegistryObject;

public class GameEventRegistryObject<GAME_EVENT extends GameEvent> extends WrappedRegistryObject<GAME_EVENT> {

    public GameEventRegistryObject(RegistryObject<GAME_EVENT> registryObject) {
        super(registryObject);
    }
}