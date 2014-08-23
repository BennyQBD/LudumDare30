import java.awt.event.KeyEvent;

public class BlockingObject extends SpriteObject
{
	public BlockingObject(float x, float y, 
			float width, float height)
	{
		super(x, y, width, height, 
				new Bitmap(100, 100)
				.ClearScreen((byte)0x00, (byte)0x00, 
					(byte)0xFF, (byte)0x00));
	}

}
