package mekanism.common.attachments;

import net.neoforged.bus.api.IEventBus;

@FunctionalInterface
public interface IAttachmentAware {//TODO - 1.20.4: Do we want this named something like container aware instead?

    void attachAttachments(IEventBus eventBus);
}