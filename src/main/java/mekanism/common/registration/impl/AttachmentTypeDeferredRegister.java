package mekanism.common.registration.impl;

import java.util.Objects;
import java.util.function.IntFunction;
import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.nbt.IntTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@NothingNullByDefault
public class AttachmentTypeDeferredRegister extends MekanismDeferredRegister<AttachmentType<?>> {

    public AttachmentTypeDeferredRegister(String namespace) {
        super(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, namespace);
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
                  @Override
                  public IntTag write(ENUM attachment) {
                      return IntTag.valueOf(attachment.ordinal());
                  }

                  @Override
                  public ENUM read(IAttachmentHolder holder, IntTag tag) {
                      return reader.apply(tag.getAsInt());
                  }
              }).comparator(Objects::equals)
              .build());
    }
}