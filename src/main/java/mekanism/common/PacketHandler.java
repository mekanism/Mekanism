package mekanism.common;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class PacketHandler
{
	/**
	 * Encodes an Object[] of data into a DataOutputStream.
	 * @param dataValues - an Object[] of data to encode
	 * @param output - the output stream to write to
	 */
	public static void encode(Object[] dataValues, ByteBuf output)
	{
		try {
			for(Object data : dataValues)
			{
				if(data instanceof Integer)
				{
					output.writeInt((Integer)data);
				}
				else if(data instanceof Boolean)
				{
					output.writeBoolean((Boolean)data);
				}
				else if(data instanceof Double)
				{
					output.writeDouble((Double)data);
				}
				else if(data instanceof Float)
				{
					output.writeFloat((Float)data);
				}
				else if(data instanceof String)
				{
					writeString(output, (String)data);
				}
				else if(data instanceof Byte)
				{
					output.writeByte((Byte)data);
				}
				else if(data instanceof int[])
				{
					for(int i : (int[])data)
					{
						output.writeInt(i);
					}
				}
				else if(data instanceof byte[])
				{
					for(byte b : (byte[])data)
					{
						output.writeByte(b);
					}
				}
				else if(data instanceof ArrayList)
				{
					encode(((ArrayList)data).toArray(), output);
				}
			}
		} catch(Exception e) {
			Mekanism.logger.error("Error while encoding packet data.");
			e.printStackTrace();
		}
	}
	
	public static void writeString(ByteBuf output, String s)
	{
		output.writeInt(s.getBytes().length);
		output.writeBytes(s.getBytes());
	}
	
	public static String readString(ByteBuf input)
	{
		return new String(input.readBytes(input.readInt()).array());
	}
}
