package mekanism.common.registration.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.containers.AttachedContainers;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentComparator;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AttachmentTypeDeferredRegister extends MekanismDeferredRegister<AttachmentType<?>> {

    public AttachmentTypeDeferredRegister(String namespace) {
        super(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, namespace);
    }

    public <CONTAINER extends INBTSerializable<CompoundTag>, ATTACHMENT extends AttachedContainers<CONTAINER>>
    MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ATTACHMENT>> registerContainer(String name, Supplier<ContainerType<CONTAINER, ATTACHMENT, ?>> typeSupplier) {
        return register(name, () -> AttachmentType.serializable(holder -> typeSupplier.get().getDefaultWithLegacy(holder))
              .comparator(AttachedContainers::isCompatible)
              .build());
    }

    public MekanismDeferredHolder<AttachmentType<?>, AttachmentType<FrequencyAware<?>>> registerFrequencyAware(String name,
          Function<IAttachmentHolder, FrequencyAware<?>> defaultValueConstructor) {
        return register(name, () -> AttachmentType.serializable(defaultValueConstructor)
              .comparator(FrequencyAware::isCompatible)
              .build());
    }

    public MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> registerBoolean(String name, boolean defaultValue) {
        return register(name, () -> AttachmentType.builder(() -> defaultValue)
              //If we are true by default we only care about serializing the value when it is false
              .serialize(defaultValue ? FALSE_SERIALIZER : TRUE_SERIALIZER)
              .comparator(Boolean::equals)
              .build());
    }

    public MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Integer>> registerNonNegativeInt(String name, int defaultValue) {
        return registerInt(name, defaultValue, 0, Integer.MAX_VALUE);
    }

    public MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Integer>> registerInt(String name, int defaultValue, int min, int max) {
        return register(name, () -> AttachmentType.builder(() -> defaultValue)
              .serialize(new IAttachmentSerializer<IntTag, Integer>() {
                  @Nullable
                  @Override
                  public IntTag write(Integer value) {
                      if (value == defaultValue || value < min || value > max) {
                          //If it is the default or invalid value that was manually set then don't save it
                          return null;
                      }
                      return IntTag.valueOf(value);
                  }

                  @Override
                  public Integer read(IAttachmentHolder holder, IntTag tag) {
                      return Mth.clamp(tag.getAsInt(), min, max);
                  }
              }).comparator(Integer::equals)
              .build());
    }

    public MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Long>> registerNonNegativeLong(String name, long defaultValue) {
        return registerLong(name, defaultValue, 0, Long.MAX_VALUE);
    }

    public MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Long>> registerLong(String name, long defaultValue, long min, long max) {
        return register(name, () -> AttachmentType.builder(() -> defaultValue)
              .serialize(new IAttachmentSerializer<LongTag, Long>() {
                  @Nullable
                  @Override
                  public LongTag write(Long value) {
                      if (value == defaultValue || value < min || value > max) {
                          //If it is the default or invalid value that was manually set then don't save it
                          return null;
                      }
                      return LongTag.valueOf(value);
                  }

                  @Override
                  public Long read(IAttachmentHolder holder, LongTag tag) {
                      return Mth.clamp(tag.getAsLong(), min, max);
                  }
              }).comparator(Long::equals)
              .build());
    }

    public <ENUM extends Enum<ENUM>> MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ENUM>> register(String name, Class<ENUM> clazz) {
        ENUM[] values = clazz.getEnumConstants();
        ENUM defaultValue = values[0];
        IntFunction<ENUM> reader;
        if (clazz.isAssignableFrom(IDisableableEnum.class)) {
            reader = index -> {
                ENUM value = MathUtils.getByIndexMod(values, index);
                return ((IDisableableEnum<?>) value).isEnabled() ? value : defaultValue;
            };
        } else {
            reader = index -> MathUtils.getByIndexMod(values, index);
        }
        return register(name, () -> AttachmentType.builder(() -> defaultValue)
              .serialize(new IAttachmentSerializer<IntTag, ENUM>() {
                  @Nullable
                  @Override
                  public IntTag write(ENUM value) {
                      if (value == defaultValue) {
                          return null;
                      }
                      return IntTag.valueOf(value.ordinal());
                  }

                  @Override
                  public ENUM read(IAttachmentHolder holder, IntTag tag) {
                      return reader.apply(tag.getAsInt());
                  }
              }).comparator(Objects::equals)
              .build());
    }

    public <ENUM extends Enum<ENUM>> MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Optional<ENUM>>> registerOptional(String name, Class<ENUM> clazz) {
        ENUM[] values = clazz.getEnumConstants();
        IntFunction<Optional<ENUM>> reader;
        if (clazz.isAssignableFrom(IDisableableEnum.class)) {
            reader = index -> Optional.of(MathUtils.getByIndexMod(values, index)).filter(value -> ((IDisableableEnum<?>) value).isEnabled());
        } else {
            reader = index -> Optional.of(MathUtils.getByIndexMod(values, index));
        }
        return register(name, () -> AttachmentType.<Optional<ENUM>>builder(Optional::empty)
              .serialize(new IAttachmentSerializer<IntTag, Optional<ENUM>>() {
                  @Nullable
                  @Override
                  public IntTag write(Optional<ENUM> value) {
                      return value.map(val -> IntTag.valueOf(val.ordinal())).orElse(null);
                  }

                  @Override
                  public Optional<ENUM> read(IAttachmentHolder holder, IntTag tag) {
                      return reader.apply(tag.getAsInt());
                  }
              }).comparator(optionalComparator(Objects::equals))
              .build());
    }

    public MekanismDeferredHolder<AttachmentType<?>, AttachmentType<Component>> registerComponent(String name, Supplier<Component> defaultValueSupplier) {
        return register(name, () -> AttachmentType.builder(defaultValueSupplier)
              .serialize(new IAttachmentSerializer<>() {
                  @Nullable
                  @Override
                  public Tag write(Component value) {
                      if (value.equals(defaultValueSupplier.get())) {
                          return null;
                      }
                      return ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, value)
                            .result()
                            .orElse(null);
                  }

                  @Override
                  public Component read(IAttachmentHolder holder, Tag tag) {
                      return ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, tag)
                            .result()
                            .orElseGet(defaultValueSupplier);
                  }
              }).comparator(Objects::equals)
              .build());
    }

    public <TYPE> MekanismDeferredHolder<AttachmentType<?>, AttachmentType<ResourceKey<TYPE>>> registerResourceKey(String name, ResourceKey<? extends Registry<TYPE>> registryKey,
          Supplier<ResourceKey<TYPE>> defaultValueSupplier) {
        return register(name, () -> AttachmentType.builder(defaultValueSupplier)
              .serialize(new IAttachmentSerializer<StringTag, ResourceKey<TYPE>>() {
                  @Nullable
                  @Override
                  public StringTag write(ResourceKey<TYPE> value) {
                      //Note: ResourceKeys are interned so can be compared directly
                      if (value == defaultValueSupplier.get()) {
                          return null;
                      }
                      return StringTag.valueOf(value.location().toString());
                  }

                  @Override
                  public ResourceKey<TYPE> read(IAttachmentHolder holder, StringTag tag) {
                      ResourceLocation rl = ResourceLocation.tryParse(tag.getAsString());
                      return rl == null ? defaultValueSupplier.get() : ResourceKey.create(registryKey, rl);
                  }
              }).comparator(Objects::equals)
              .build());
    }

    public static <TYPE> IAttachmentComparator<Optional<TYPE>> optionalComparator(IAttachmentComparator<TYPE> innerComparator) {
        return (a, b) -> a.map(aVal -> b.filter(bVal -> innerComparator.areCompatible(aVal, bVal)).isPresent()).orElseGet(b::isEmpty);
    }

    private static final IAttachmentSerializer<ByteTag, Boolean> TRUE_SERIALIZER = new IAttachmentSerializer<>() {
        @Nullable
        @Override
        public ByteTag write(Boolean attachment) {
            return attachment ? ByteTag.ONE : null;
        }

        @Override
        public Boolean read(IAttachmentHolder holder, ByteTag tag) {
            return tag.getAsByte() != 0;
        }
    };

    private static final IAttachmentSerializer<ByteTag, Boolean> FALSE_SERIALIZER = new IAttachmentSerializer<>() {
        @Nullable
        @Override
        public ByteTag write(Boolean attachment) {
            return attachment ? null : ByteTag.ZERO;
        }

        @Override
        public Boolean read(IAttachmentHolder holder, ByteTag tag) {
            return tag.getAsByte() != 0;
        }
    };
}