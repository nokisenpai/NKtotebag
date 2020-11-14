package be.noki_senpai.NKtotebag.data;

import org.bukkit.Chunk;

import java.util.Objects;

public class GrowArea
{
	private int x = 0;
	private int z = 0;
	private String world = null;

	public GrowArea(int x, int z, String world)
	{
		this.x = x;
		this.z = z;
		this.world = world;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getZ()
	{
		return z;
	}

	public void setZ(int z)
	{
		this.z = z;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		GrowArea growArea = (GrowArea) o;
		return x == growArea.x && z == growArea.z && Objects.equals(world, growArea.world);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(x, z);
	}
}
