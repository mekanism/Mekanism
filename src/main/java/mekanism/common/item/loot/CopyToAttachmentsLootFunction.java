package mekanism.common.item.loot;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.TransporterUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Loot function which copies nbt to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyToAttachmentsLootFunction implements LootItemFunction {

    public static final Codec<CopyToAttachmentsLootFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                NbtProviders.CODEC.fieldOf("source").forGetter(function -> function.source),
                CopyOperation.CODEC.listOf().fieldOf("ops").forGetter(function -> function.operations)
          ).apply(instance, CopyToAttachmentsLootFunction::new)
    );

    private final NbtProvider source;
    private final List<CopyOperation<?>> operations;

    private CopyToAttachmentsLootFunction(NbtProvider source, List<CopyOperation<?>> operations) {
        this.source = source;
        this.operations = operations;
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_TO_ATTACHMENTS.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        Tag tag = this.source.get(lootContext);
        if (tag != null) {
            for (CopyOperation<?> operation : this.operations) {
                operation.apply(stack, tag);
            }
        }
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.source.getReferencedContextParams();
    }

    public static Builder copyData(NbtProvider source) {
        return new Builder(source);
    }

    public static Builder copyData(LootContext.EntityTarget entityTarget) {
        return copyData(ContextNbtProvider.forContextEntity(entityTarget));
    }

    public static class Builder implements LootItemFunction.Builder {

        private final List<CopyOperation<?>> operations = new ArrayList<>();
        private final NbtProvider source;

        protected Builder(NbtProvider source) {
            this.source = source;
        }

        public Builder copy(String sourcePath, Holder<AttachmentType<?>> target) {
            return copy(sourcePath, target.value());
        }

        public Builder copy(String sourcePath, AttachmentType<?> target) {
            try {
                this.operations.add(new CopyOperation<>(CopyNbtFunction.Path.of(sourcePath), target));
                return this;
            } catch (CommandSyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public LootItemFunction build() {
            //Ensure the operations are always saved in the same order
            operations.sort(Comparator.<CopyOperation<?>, String>comparing(operation -> operation.sourcePath().string())
                  .thenComparing(operation -> NeoForgeRegistries.ATTACHMENT_TYPES.getKey(operation.target())));
            return new CopyToAttachmentsLootFunction(this.source, this.operations);
        }
    }

    private record CopyOperation<ATTACHMENT>(CopyNbtFunction.Path sourcePath, AttachmentType<ATTACHMENT> target) {

        public static final Codec<CopyOperation<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              CopyNbtFunction.Path.CODEC.fieldOf("source").forGetter(CopyOperation::sourcePath),
              NeoForgeRegistries.ATTACHMENT_TYPES.byNameCodec().fieldOf("target").forGetter(CopyOperation::target)
        ).apply(instance, CopyOperation::new));

        public void apply(ItemStack targetStack, Tag sourceTag) {
            try {
                List<Tag> list = this.sourcePath.path().get(sourceTag);
                if (!list.isEmpty()) {
                    Tag source = Iterables.getLast(list);
                    if (handleSpecialCased(targetStack, target, source)) {
                        return;
                    }
                    ATTACHMENT data = targetStack.getData(target);
                    if (data instanceof Boolean) {
                        if (source == ByteTag.ONE) {
                            setBoolean(targetStack, true);
                        } else if (source == ByteTag.ZERO) {
                            setBoolean(targetStack, false);
                        }
                    } else if (data instanceof Integer) {
                        if (source instanceof IntTag intTag) {
                            setInteger(targetStack, intTag.getAsInt());
                        }
                    } else if (data instanceof Long) {
                        if (source instanceof LongTag longTag) {
                            setLong(targetStack, longTag.getAsLong());
                        }
                    } else if (data instanceof FloatingLong) {
                        if (source instanceof StringTag stringTag) {
                            setFloatingLong(targetStack, FloatingLong.parseFloatingLong(stringTag.getAsString()));
                        }
                    } else if (data instanceof boolean[]) {
                        if (source instanceof ByteArrayTag byteArrayTag) {
                            setBooleanArray(targetStack, byteArrayTag.getAsByteArray());
                        }
                    } else if (data instanceof Item) {
                        if (source instanceof StringTag stringTag) {
                            setItem(targetStack, ResourceLocation.tryParse(stringTag.getAsString()));
                        }
                    } else if (data instanceof ItemStack) {
                        if (source instanceof CompoundTag compoundTag) {
                            setItem(targetStack, ItemStack.of(compoundTag));
                        }
                    } else if (data instanceof Optional<?> optional) {
                        if (optional.isPresent() && optional.get() instanceof Enum<?> enumData) {
                            if (source instanceof IntTag intTag) {
                                setOptionalEnum(targetStack, enumData.getDeclaringClass().getEnumConstants(), intTag.getAsInt());
                            }
                        }
                    } else if (data instanceof Enum<?> enumData) {
                        if (source instanceof IntTag intTag) {
                            setEnum(targetStack, enumData.getDeclaringClass().getEnumConstants(), intTag.getAsInt());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        //TODO: Try to come up with a better way of handling this
        private static boolean handleSpecialCased(ItemStack targetStack, AttachmentType<?> target, Tag source) {
            if (target == MekanismAttachmentTypes.TRANSPORTER_COLOR.value()) {
                if (source instanceof IntTag intTag) {
                    targetStack.setData(MekanismAttachmentTypes.TRANSPORTER_COLOR, Optional.ofNullable(TransporterUtils.readColor(intTag.getAsInt())));
                }
            } else if (target == MekanismAttachmentTypes.OVERFLOW_AWARE.value()) {
                if (source instanceof ListTag listTag) {
                    targetStack.getData(MekanismAttachmentTypes.OVERFLOW_AWARE).deserializeNBT(listTag);
                }
            } else {
                return false;
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private void setBoolean(ItemStack stack, boolean value) {
            stack.setData((AttachmentType<Boolean>) target, value);
        }

        @SuppressWarnings("unchecked")
        private void setInteger(ItemStack stack, int value) {
            stack.setData((AttachmentType<Integer>) target, value);
        }

        @SuppressWarnings("unchecked")
        private void setLong(ItemStack stack, long value) {
            stack.setData((AttachmentType<Long>) target, value);
        }

        @SuppressWarnings("unchecked")
        private void setFloatingLong(ItemStack stack, FloatingLong value) {
            stack.setData((AttachmentType<FloatingLong>) target, value);
        }

        @SuppressWarnings("unchecked")
        private void setBooleanArray(ItemStack stack, byte[] value) {
            boolean[] booleans = new boolean[value.length];
            for (int i = 0; i < value.length; i++) {
                booleans[i] = value[i] == 1;
            }
            stack.setData((AttachmentType<boolean[]>) target, booleans);
        }

        @SuppressWarnings("unchecked")
        private void setItem(ItemStack stack, @Nullable ResourceLocation registryName) {
            if (registryName != null) {
                stack.setData((AttachmentType<Item>) target, BuiltInRegistries.ITEM.get(registryName));
            }
        }

        @SuppressWarnings("unchecked")
        private void setItem(ItemStack stack, ItemStack item) {
            stack.setData((AttachmentType<ItemStack>) target, item);
        }

        @SuppressWarnings("unchecked")
        private <ENUM> void setEnum(ItemStack stack, ENUM[] values, int index) {
            ENUM value = MathUtils.getByIndexMod(values, index);
            if (value instanceof IDisableableEnum<?> disableableEnum && !disableableEnum.isEnabled()) {
                value = values[0];
            }
            stack.setData((AttachmentType<ENUM>) target, value);
        }

        @SuppressWarnings("unchecked")
        private <ENUM> void setOptionalEnum(ItemStack stack, ENUM[] values, int index) {
            Optional<ENUM> optionalValue = Optional.of(MathUtils.getByIndexMod(values, index))
                  .filter(value -> !(value instanceof IDisableableEnum<?> disableableEnum) || disableableEnum.isEnabled());
            stack.setData((AttachmentType<Optional<ENUM>>) target, optionalValue);
        }
    }
}