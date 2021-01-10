package com.mumfrey.worldeditcui;

import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.debug.CUIDebug;
import com.mumfrey.worldeditcui.event.CUIEventDispatcher;
import com.mumfrey.worldeditcui.event.listeners.CUIRenderContext;
import com.mumfrey.worldeditcui.exceptions.InitialisationException;
import com.mumfrey.worldeditcui.render.CUISelectionProvider;
import com.mumfrey.worldeditcui.render.ConfiguredColour;
import com.mumfrey.worldeditcui.render.region.CuboidRegion;
import com.mumfrey.worldeditcui.render.region.Region;
import com.mumfrey.worldeditcui.render.shapes.RenderChunkBoundary;
import com.mumfrey.worldeditcui.util.Vector3;
import net.minecraft.client.MinecraftClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main controller class. Uses a pseudo-JavaBeans paradigm. The only real
 * logic here is listener registration.
 * 
 * TODO: Preview mode
 * TODO: Command transactions
 * TODO: Add ability to flash selection
 *  
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class WorldEditCUI
{
	public static final int PROTOCOL_VERSION = 4;
	
	private final Map<UUID, Region> regions = new LinkedHashMap<>();
	private Region selection, activeRegion;
	private CUIDebug debugger;
	private CUIConfiguration configuration;
	private CUIEventDispatcher dispatcher;
	private CUISelectionProvider selectionProvider;
	private RenderChunkBoundary chunkBorderRenderer;
	private boolean chunkBorders;
	
	public void initialise(MinecraftClient minecraft)
	{
		this.selection = new CuboidRegion(this);
		this.configuration = CUIConfiguration.create();
		this.debugger = new CUIDebug(this);
		this.dispatcher = new CUIEventDispatcher(this);
		this.selectionProvider = new CUISelectionProvider(this);

		try
		{
			this.selection.initialise();
			this.configuration.initialise();
			this.debugger.initialise();
			this.dispatcher.initialise();
			this.selectionProvider.initialise();
		}
		catch (InitialisationException e)
		{
			e.printStackTrace();
			return;
		}
		
		this.chunkBorderRenderer = new RenderChunkBoundary(ConfiguredColour.CHUNKBOUNDARY.style(), ConfiguredColour.CHUNKGRID.style(), minecraft);
	}

	public CUIEventDispatcher getDispatcher()
	{
		return this.dispatcher;
	}
	
	public CUISelectionProvider getSelectionProvider()
	{
		return this.selectionProvider;
	}

	public CUIConfiguration getConfiguration()
	{
		return this.configuration;
	}
	
	public CUIDebug getDebugger()
	{
		return this.debugger;
	}
	
	public void clear()
	{
		this.clearSelection();
		this.clearRegions();
	}
	
	public void clearSelection()
	{
		this.selection = new CuboidRegion(this);
	}
	
	public void clearRegions()
	{
		this.activeRegion = null;
		this.regions.clear();
	}
	
	public Region getSelection(boolean multi)
	{
		return multi ? this.activeRegion : this.selection;
	}
	
	public void setSelection(UUID id, Region region)
	{
		if (id == null)
		{
			this.selection = region;
			return;
		}

		if (region == null)
		{
			this.regions.remove(id);
			this.activeRegion = null;
			return;
		}
		
		this.regions.put(id, region);
		this.activeRegion = region;
	}

	public void renderSelections(final CUIRenderContext ctx)
	{
		if (this.selection != null)
		{
			this.selection.render(ctx);
		}
		
		for (Region region : this.regions.values())
		{
			region.render(ctx);
		}
		
		if (this.chunkBorders)
		{
			this.chunkBorderRenderer.render(ctx);
		}
	}

	public void toggleChunkBorders()
	{
		this.chunkBorders = !this.chunkBorders;
	}
}
