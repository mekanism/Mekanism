package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GameEventRegistryObject<GAME_EVENT extends GameEvent> extends WrappedRegistryObject<GameEvent, GAME_EVENT> {

    public GameEventRegistryObject(DeferredHolder<GameEvent, GAME_EVENT> registryObject) {
        super(registryObject);
    }
}