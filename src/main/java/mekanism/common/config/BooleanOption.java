package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by Thiakil on 15/03/2019.
 */
@ParametersAreNonnullByDefault
public class BooleanOption extends Option<BooleanOption>
{
	private boolean value;
	private final boolean defaultValue;

	BooleanOption(BaseConfig owner, String category, String key, boolean defaultValue, @Nullable String comment)
	{
		super(owner, category, key, comment);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	BooleanOption(BaseConfig owner, String category, String key, boolean defaultValue){
		this(owner, category, key, defaultValue, null);
	}

	BooleanOption(BaseConfig owner, String category, String key){
		this(owner, category, key, false, null);
	}

	public boolean val()
	{
		return value;
	}

	public void set(boolean value)
	{
		this.value = value;
	}

	@SuppressWarnings("Duplicates")//types are different
	@Override
	protected void load(Configuration config)
	{
		Property prop = config.get(this.category, this.key, this.defaultValue, this.comment);

		prop.setRequiresMcRestart(this.requiresGameRestart);
		prop.setRequiresWorldRestart(this.requiresWorldRestart);

		this.value = prop.getBoolean();
	}

	@Override
	protected void write(ByteBuf buf)
	{
		buf.writeBoolean(this.value);
	}

	@Override
	protected void read(ByteBuf buf)
	{
		this.value = buf.readBoolean();
	}
}
