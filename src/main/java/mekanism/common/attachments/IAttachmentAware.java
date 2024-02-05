package mekanism.common.attachments;

import net.neoforged.bus.api.IEventBus;

@FunctionalInterface
public interface IAttachmentAware {

    void attachAttachments(IEventBus eventBus);
}