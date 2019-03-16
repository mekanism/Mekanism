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
public class IntOption extends Option
{
	private int value;
	private final int defaultValue;
	private boolean hasRange = false;
	private int min;
	private int max;

	IntOption(BaseConfig owner, String category, String key, int defaultValue, @Nullable String comment)
	{
		super(owner, category, key, comment);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	IntOption(BaseConfig owner, String category, String key, int defaultValue){
		this(owner, category, key, defaultValue, null);
	}

	IntOption(BaseConfig owner, String category, String key){
		this(owner, category, key, 0, null);
	}

	IntOption(BaseConfig owner, String category, String key, int defaultValue, @Nullable String comment, int min, int max)
	{
		this(owner, category, key, defaultValue, comment);
		this.hasRange = true;
		this.min = min;
		this.max = max;
	}

	public int val()
	{
		return value;
	}

	public void set(int value)
	{
		this.value = value;
	}

	@SuppressWarnings("Duplicates")//types are different
	@Override
	protected void load(Configuration config)
	{
		Property prop;

		if (hasRange)
		{
			prop = config.get(this.category, this.key, this.defaultValue, this.comment, this.min, this.max);
		} else {
			prop = config.get(this.category, this.key, this.defaultValue, this.comment);
		}

		prop.setRequiresMcRestart(this.requiresGameRestart);
		prop.setRequiresWorldRestart(this.requiresWorldRestart);

		this.value = prop.getInt();
	}

	@Override
	protected void write(ByteBuf buf)
	{
		buf.writeInt(this.value);
	}

	@Override
	protected void read(ByteBuf buf)
	{
		this.value = buf.readInt();
	}
}
