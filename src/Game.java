import java.awt.event.KeyEvent;
import java.util.*;

public class Game
{
	private final ArrayList<Entity>  m_entities;
	private final ArrayList<Entity>  m_toRemove;

	private final ArrayList<Player>  m_laserStarts;
	private final ArrayList<Player>  m_laserEnds;
	private final ArrayList<Player>  m_laserStartToRemove;
	private final ArrayList<Player>  m_laserEndToRemove;
	private final Stars3D            m_stars;
	private float                    m_delta;
	private final Entity             m_mainPlayer;

	public Game()
	{
		m_entities           = new ArrayList<Entity>();
		m_toRemove           = new ArrayList<Entity>();
		m_laserStarts        = new ArrayList<Player>();
		m_laserEnds          = new ArrayList<Player>();
		m_laserStartToRemove = new ArrayList<Player>();
		m_laserEndToRemove   = new ArrayList<Player>();
		m_stars = new Stars3D(4096, 64.0f, 4.0f);

		m_mainPlayer = new Player(true, -1.0f, -1.0f,
					1.0f, 1.0f);
		m_entities.add(m_mainPlayer);

		for(int i = 0; i < 10; i++)
		{
			float x = (float)Math.random();
			float y = (float)Math.random();
			m_entities.add(new Player(false, x, y,
						0.5f, 0.5f));
		}
		for(int i = 0; i < 10; i++)
		{
			float x = (float)Math.random();
			float y = (float)-Math.random();
			m_entities.add(new DestroyableObject(x, y,
						0.5f, 0.5f));
		}
		for(int i = 0; i < 10; i++)
		{
			float x = (float)-Math.random();
			float y = (float)Math.random();
			m_entities.add(new BlockingObject(x, y,
						0.5f, 0.5f));
		}
	}

	public void Update(Input input, float delta)
	{
		m_delta = delta;
		for(int i = 0; i < m_entities.size(); i++)
		{
			m_entities.get(i).Update(input, delta);
		}

		for(int i = 0; i < m_entities.size(); i++)
		{
			for(int j = i + 1; j < m_entities.size(); j++)
			{
				Entity current = m_entities.get(i);
				Entity other = m_entities.get(j);

				if(current instanceof Player &&
				   other instanceof Player)
				{
					Player currentPlayer = (Player)current;
					Player otherPlayer   = (Player)other;

					if(currentPlayer.GetIsActive() 
					   && !otherPlayer.GetIsActive()
					   && currentPlayer.SphereIntersect(otherPlayer))
					{
						m_laserStarts.add(currentPlayer);
						m_laserEnds.add(otherPlayer);
						otherPlayer.SetIsActive(true);
					}
					else if(otherPlayer.GetIsActive()
					   && !currentPlayer.GetIsActive()
					   && currentPlayer.SphereIntersect(otherPlayer))
					{
						m_laserStarts.add(otherPlayer);
						m_laserEnds.add(currentPlayer);
						currentPlayer.SetIsActive(true);
					}
				}
			}
		}

		for(int i = 0; i < m_entities.size(); i++)
		{
			if(m_entities.get(i) instanceof 
					DestroyableObject)
			{
				DestroyableObject current = 
					(DestroyableObject)m_entities.get(i);

				for(int j = 0; j < m_laserStarts.size();j++)
				{
					Entity start = m_laserStarts.get(j);
					Entity end   = m_laserEnds.get(j);

					if(current.LineIntersect(
						start.GetX(), start.GetY(), 
						end.GetX(), end.GetY()))
					{
						m_toRemove.add(current);
					}
				}
			}

			if(m_entities.get(i) instanceof BlockingObject)
			{
				BlockingObject current = 
					(BlockingObject)m_entities.get(i);

				for(int j = 0; j < m_laserStarts.size();j++)
				{
					Player start = m_laserStarts.get(j);
					Player end   = m_laserEnds.get(j);

					if(current.LineIntersect(
						start.GetX(), start.GetY(), 
						end.GetX(), end.GetY()))
					{
						m_laserStartToRemove.add(start);
						m_laserEndToRemove.add(end);
					}
				}
			}

		}

		for(int i = 0; i < m_toRemove.size(); i++)
		{
			m_entities.remove(m_toRemove.get(i));
		}
		m_toRemove.clear();

		if(m_laserStartToRemove.size() != m_laserEndToRemove.size())
		{
			System.err.println("Error: Laser start/end removal list has fallen "
					+ "out of alignment");
//			for(int i = 0; i < m_laserStarts.size(); i++)
//			{
//				System.out.println(m_laserStarts.get(i));
//			}
//			System.out.println();
//			for(int i = 0; i < m_laserEnds.size(); i++)
//			{
//				System.out.println(m_laserEnds.get(i));
//			}


			System.exit(1);
		}

		for(int i = 0; i < m_laserStartToRemove.size(); i++)
		{
			Player start = m_laserStartToRemove.get(i);
			Player end   = m_laserEndToRemove.get(i);

			start.SetIsActive(false);
			end.SetIsActive(false);
			
			m_laserStarts.remove(start);
			m_laserEnds.remove(end);

			int startIndexInLaserEnd = 
				m_laserEnds.indexOf(start);
			
			if(startIndexInLaserEnd != -1)
			{
				m_laserStarts.remove(startIndexInLaserEnd);					m_laserEnds.remove(startIndexInLaserEnd);				}

			int endIndexInLaserStart = 
				m_laserStarts.indexOf(end);

			if(endIndexInLaserStart != -1)
			{
				m_laserStarts.remove(endIndexInLaserStart);					m_laserEnds.remove(endIndexInLaserStart);
			}
		}
		m_laserStartToRemove.clear();
		m_laserEndToRemove.clear();
	}

	public void Render(RenderContext target)
	{
		m_stars.UpdateAndRender(target, m_delta);
		//target.Clear((byte)0x00);

		if(m_laserStarts.size() != m_laserEnds.size())
		{
			System.err.println("Error: Laser start/end has fallen "
					+ "out of alignment");
			System.exit(1);
		}

		for(int i = 0; i < m_laserStarts.size(); i++)
		{
			Entity start = m_laserStarts.get(i);
			Entity end   = m_laserEnds.get(i);

			target.DrawLine(start.GetX(), start.GetY(),
				   	end.GetX(), end.GetY(),
				(byte)0x00, (byte)0x79, (byte)0xbf, (byte)0x10);
		}

		for(int i = 0; i < m_entities.size(); i++)
		{
			m_entities.get(i).Render(target);
		}
		
		int clampEnd = target.GetWidth() < target.GetHeight() ? 
			target.GetWidth() :
			target.GetHeight();
		int clampStartY = (clampEnd - target.GetHeight()) / -2;
		int clampStartX = (clampEnd - target.GetWidth()) / -2;
		int clampEndX = clampEnd + clampStartX;
		int clampEndY = clampEnd + clampStartY;

		target.FillRect(0, 0, clampStartX, target.GetHeight(), 
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
		target.FillRect(clampEndX, 0, target.GetWidth() - clampEndX, target.GetHeight(), 
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
	}
}
