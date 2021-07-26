package com.mumfrey.worldeditcui.render.shapes;

import com.mumfrey.worldeditcui.event.listeners.CUIRenderContext;
import com.mumfrey.worldeditcui.render.LineStyle;
import com.mumfrey.worldeditcui.render.RenderStyle;
import com.mumfrey.worldeditcui.render.points.PointRectangle;
import com.mumfrey.worldeditcui.util.Vector2;

import java.util.List;

/**
 * Draws the top and bottom rings of a polygon region
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class Render2DBox extends RenderRegion
{
	private final List<PointRectangle> points;
	private final int min, max;
	
	public Render2DBox(RenderStyle style, List<PointRectangle> points, int min, int max)
	{
		super(style);
		this.points = points;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		double off = 0.03 - ctx.cameraPos().getY();
		for (LineStyle line : this.style.getLines())
		{
			if (!ctx.apply(line, this.style.getRenderType()))
			{
				continue;
			}

			ctx.beginLines();
			ctx.color(line);

			for (PointRectangle point : this.points)
			{
				if (point != null)
				{
					Vector2 pos = point.getPoint();
					double x = pos.getX() - ctx.cameraPos().getX();
					double z = pos.getY() - ctx.cameraPos().getZ();
					ctx.vertex(x + 0.5, this.min + off, z + 0.5)
						.vertex(x + 0.5, this.max + 1 + off, z + 0.5);
				}
			}
			ctx.endLines();
		}
	}
}
