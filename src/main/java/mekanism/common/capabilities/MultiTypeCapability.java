package mekanism.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;

public record MultiTypeCapability<HANDLER>(BlockCapability<HANDLER, @Nullable Direction> block,
                                           ItemCapability<HANDLER, ItemAccess> item,
                                           EntityCapability<HANDLER, ?> entity) implements IMultiTypeCapability<HANDLER, HANDLER> {

    public MultiTypeCapability(Identifier name, Class<HANDLER> handlerClass) {
        this(
              BlockCapability.createSided(name, handlerClass),
              ItemCapability.create(name, handlerClass, ItemAccess.class),
              EntityCapability.createVoid(name, handlerClass)
        );
    }
}