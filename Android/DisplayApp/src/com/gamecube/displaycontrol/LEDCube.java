package com.gamecube.displaycontrol;

/*** LED Cube - Austin Garrard

-LED's are accessed in (x,y,z) form
-bitmap representation: 
	-index into array = (z*8) + y
	-offset into index = x%8;
****/
public class LEDCube
{
	int DIM;
	byte[] cube;

	public LEDCube(int dim)
	{
		DIM = dim;
		cube = new byte[DIM*DIM];
	}

	private int index(int y, int z)
	{
		return (z*8) + y;
	}

	public void setLED(int x, int y, int z)
	{
		//XOR the needed bit
		cube[index(y,z)] |= (0x1 << (x%8));
	}

	public void clearLED(int x, int y, int z)
	{
		//AND the needed bit
		cube[index(y,z)] &= ~(0x1 << (x%8));
	}

	public boolean testLED(int x, int y, int z)
	{
		//AND the needed bit but DON'T assign
		return (cube[index(y,z)] & (0x1 << (x%8))) > 0; 
	}

	public byte[] getCube()
	{
		return cube;
	}

	public void clear() {
		// get new cube, use automatic Java garbage disposal
		cube = new byte[DIM*DIM];
	}

	public String toString()
	{
		String out = "";
		for(int z = 0; z < DIM; z++) {
			out += "--LAYER: " + z + "\n";
			for(int y = 0; y < DIM; y++) {
				for(byte x = 0; x < DIM; x++) {
					byte mask = (byte)(1 << x);
					byte val = (byte)((cube[index(y,z)] & mask) >> x);
					out += (val == -1 ? 1:val);
				}
				out += "\n";
			}
		}
		return out;
	}

	/*
	public static void main(String[] args)
	{
		LEDCube c = new LEDCube(8);

		//set some LED's
		c.setLED(0,0,0);
		c.setLED(1,0,1);
		c.setLED(7,7,0);
		c.setLED(4,4,6);
		c.setLED(3,3,6);
		//show results
		System.out.println(c.toString());
		
		//test/modify a specific LED and show results
		System.out.println(c.testLED(7,1,2) ? "Y":"N");
		c.setLED(7,1,2);
		System.out.println(c.testLED(7,1,2) ? "Y":"N");
		c.clearLED(7,1,2);
		System.out.println(c.testLED(7,1,2) ? "Y":"N");

	}
	*/
}
