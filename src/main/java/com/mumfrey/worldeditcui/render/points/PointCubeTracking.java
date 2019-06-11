package com.mumfrey.worldeditcui.render.points;

import com.mumfrey.worldeditcui.util.Vector3;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

/**
 * A PointCube which tracks the specified entity location
 * 
 * @author Adam Mummery-Smith
 */
public class PointCubeTracking extends PointCube
{
	private final Entity entity;
	private final double traceDistance;
	private int lastX, lastY, lastZ;
	
	public PointCubeTracking(Entity entity, double traceDistance)
	{
		super(0, 0, 0);
		this.entity = entity;
		this.traceDistance = traceDistance;
	}
	
	@Override
	public boolean isDynamic()
	{
		return true;
	}
	
	@Override
	public Vector3 getPoint()
	{
		return this.point;
	}

	@Override
	public void updatePoint(float partialTicks)
	{
		BlockPos pos = rayTraceFromEntity(this.entity, this.traceDistance, partialTicks).getBlockPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if (this.lastX != x || this.lastY != y || this.lastZ != z)
		{
			this.lastX = x;
			this.lastY = y;
			this.lastZ = z;
			this.point = new Vector3(x, y, z);
			this.box.setPosition(this.point.subtract(PointCube.MIN_VEC), this.point.add(PointCube.MAX_VEC));
			this.notifyObservers();
		}
	}


	// Taken from LiteLoader.
	private static BlockHitResult rayTraceFromEntity(Entity source, double traceDistance, float partialTicks) {
		Vec3d traceStart = getPositionEyes(source, partialTicks);
		Vec3d lookDir = source.getCameraPosVec(partialTicks).multiply(traceDistance);
		Vec3d traceEnd = traceStart.add(lookDir);

		RayTraceContext ctx = new RayTraceContext(traceStart, traceEnd, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.ANY, source);
		return source.world.rayTrace(ctx);
	}

	private static Vec3d getPositionEyes(Entity entity, float partialTicks) {
		if (partialTicks == 1.0F) {
			return new Vec3d(entity.x, entity.y + entity.getEyeHeight(entity.getPose()), entity.z);
		}

		double interpX = entity.prevX + (entity.x - entity.prevX) * partialTicks;
		double interpY = entity.prevY + (entity.y - entity.prevY) * partialTicks + entity.getEyeHeight(entity.getPose());
		double interpZ = entity.prevZ + (entity.z - entity.prevZ) * partialTicks;
		return new Vec3d(interpX, interpY, interpZ);
	}
}
