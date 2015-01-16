/**
 * 
 */
package cn.academy.misc.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cn.liutils.api.entityx.EntityX;
import cn.liutils.api.entityx.MotionHandler;
import cn.liutils.api.entityx.motion.CollisionCheck;
import cn.liutils.api.entityx.motion.VelocityUpdate;
import cn.liutils.util.GenericUtils;
import cn.liutils.util.space.Motion3D;

/**
 * EntityRay is a entity class representing 'ray' like entities. You can specify the position, length, facing direction of
 * the ray. Also you can make the ray follow a creator EntityLivingBase(usually EntityPlayer)'s position and orientation.
 * For its associated renderer, see RenderRay.
 * @author WeathFolD
 */
public class EntityRay extends EntityX {
	
	Motion3D motion;
	
	EntityLivingBase thrower;
	boolean follow = false;
	
	boolean peformTrace = false;
	double traceDist;
	
	public EntityRay(EntityLivingBase creator) {
		this(creator, true);
	}
	
	/**
	 * Use this ctor if the ray has a creator.
	 * @param world
	 * @param creator
	 * @param follow If the ray's position and heading will be calculate every tick according to the player.
	 */
	public EntityRay(EntityLivingBase creator, boolean follow) {
		this(creator.worldObj, new Motion3D(creator, true));
		thrower = creator;
		this.follow = follow;
	}
	
	/**
	 * Init an ray with fixed position and facing direction.
	 */
	public EntityRay(World world, Motion3D pos) {
		super(world);
		motion = pos;
		motion.applyToEntity(this);
		this.setCurMotion(new RayUpdate());
		setup();
	}

	/**
	 * Client-side ctor.
	 */
	@SideOnly(Side.CLIENT)
	public EntityRay(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		traceDist = getMaxDistance();
		setup();
	}
	
	@Override
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(10, Float.valueOf(0));
	}
	
	private void setup() {
		this.removeDaemonHandler(VelocityUpdate.ID);
		this.removeDaemonHandler(CollisionCheck.ID);
		this.addDaemonHandler(new Sync());
	}
	
	public double getMaxDistance() {
		return 20.0;
	}
	
	public double getTraceDistance() {
		return traceDist;
	}
	
	public void setPeformTrace(boolean is) {
		peformTrace = is;
	}
	
	private MovingObjectPosition peformTrace() {
		Motion3D tmp = motion.clone();
		Vec3 v1 = tmp.getPosVec(worldObj), v2 = tmp.move(getMaxDistance()).getPosVec(worldObj);
		return GenericUtils.rayTraceBlocksAndEntities(null, worldObj, v1, v2, this);
	}
	
	private class Sync extends MotionHandler<EntityRay> {

		public Sync() {
			super(EntityRay.this);
		}

		@Override
		public void onSpawnedInWorld() {}

		@Override
		public void onUpdate() {
			if(!worldObj.isRemote) {
				dataWatcher.updateObject(10, Float.valueOf((float)traceDist));
			} else {
				traceDist = dataWatcher.getWatchableObjectFloat(10);
			}
		}

		@Override
		public String getID() {
			return "sync";
		}
		
	}
	
	private class RayUpdate extends MotionHandler<EntityRay> {

		public RayUpdate() {
			super(EntityRay.this);
		}

		@Override
		public void onSpawnedInWorld() {}

		@Override
		public void onUpdate() {
			if(worldObj.isRemote) return;
			if(follow)
				motion.init(thrower, 0, true);
			motion.applyToEntity(EntityRay.this);
			
			if(peformTrace) {
				MovingObjectPosition mop = peformTrace();
				if(mop != null) {
					traceDist = mop.hitVec.distanceTo(motion.getPosVec(worldObj));
				} else {
					traceDist = getMaxDistance();
				}
			} else {
				traceDist = getMaxDistance();
			}
		}

		@Override
		public String getID() {
			return "posmanip";
		}
		
	}

}
