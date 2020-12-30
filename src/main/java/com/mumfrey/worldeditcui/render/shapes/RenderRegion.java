package com.mumfrey.worldeditcui.render.shapes;

import com.mumfrey.worldeditcui.event.listeners.CUIRenderContext;
import com.mumfrey.worldeditcui.render.RenderStyle;
import com.mumfrey.worldeditcui.util.Observable;
import com.mumfrey.worldeditcui.util.Observer;

/**
 * Base class for region renderers
 * 
 * @author Adam Mummery-Smith
 */
public abstract class RenderRegion implements Observer
{
	protected static final double OFFSET = 0.001d; // to avoid z-fighting with blocks

	protected RenderStyle style;
	
	protected RenderRegion(RenderStyle style)
	{
		this.style = style;
	}

	public final void setStyle(RenderStyle style)
	{
		this.style = style;
	}
	
	public abstract void render(CUIRenderContext ctx);
	
	@Override
	public void notifyChanged(Observable<?> source)
	{
		
	}
}
