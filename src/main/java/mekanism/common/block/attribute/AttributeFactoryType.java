package mekanism.common.block.attribute;

import javax.annotation.Nonnull;
import mekanism.common.content.blocktype.FactoryType;

public class AttributeFactoryType implements Attribute {

    private final FactoryType type;

    public AttributeFactoryType(FactoryType type) {
        this.type = type;
    }

    @Nonnull
    public FactoryType getFactoryType() {
        return type;
    }
}