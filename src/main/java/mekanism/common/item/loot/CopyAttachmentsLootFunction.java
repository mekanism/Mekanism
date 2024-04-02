package mekanism.common.item.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.util.RegistryUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Loot function which copies nbt to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyAttachmentsLootFunction implements LootItemFunction {

    public static final Codec<CopyAttachmentsLootFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                NeoForgeRegistries.ATTACHMENT_TYPES.byNameCodec().listOf().fieldOf("attachments").forGetter(function -> function.attachments)
          ).apply(instance, CopyAttachmentsLootFunction::new)
    );

    private final List<AttachmentType<?>> attachments;

    private CopyAttachmentsLootFunction(List<AttachmentType<?>> attachments) {
        this.attachments = attachments;
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_ATTACHMENTS.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity != null) {
            for (AttachmentType<?> attachment : attachments) {
                try {
                    copyAttachment(blockEntity, stack, attachment);
                } catch (Exception e) {
                    //Note: We probably will never actually have a case of an exception being thrown, but in case any of our attachments
                    // can't actually be constructed on the stack we pre-emptively catch it
                    Mekanism.logger.error("Could not copy attachment '{}' to item: {}", NeoForgeRegistries.ATTACHMENT_TYPES.getKey(attachment),
                          RegistryUtils.getName(stack.getItem()));
                }
            }
        }
        return stack;
    }

    private <ATTACHMENT> void copyAttachment(IAttachmentHolder source, IAttachmentHolder target, AttachmentType<ATTACHMENT> attachmentType) {
        //TODO: Is this fine or do we need a better way of copying this as a new object? For BlockData it doesn't matter
        // but it might for some types we add in the future?
        Optional<ATTACHMENT> existingData = source.getExistingData(attachmentType);
        //noinspection OptionalIsPresent - Capturing lambda
        if (existingData.isPresent()) {
            target.setData(attachmentType, existingData.get());
        }
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return MekanismLootFunctions.BLOCK_ENTITY_LOOT_CONTEXT;
    }

    public static class Builder implements LootItemFunction.Builder {

        private final List<AttachmentType<?>> attachmentTypes = new ArrayList<>();

        protected Builder() {
        }

        public Builder copy(Holder<AttachmentType<?>> attachment) {
            return copy(attachment.value());
        }

        public Builder copy(AttachmentType<?> attachment) {
            this.attachmentTypes.add(attachment);
            return this;
        }

        @Override
        public LootItemFunction build() {
            //Ensure the operations are always saved in the same order
            attachmentTypes.sort(Comparator.comparing(NeoForgeRegistries.ATTACHMENT_TYPES::getKey));
            return new CopyAttachmentsLootFunction(attachmentTypes);
        }
    }
}