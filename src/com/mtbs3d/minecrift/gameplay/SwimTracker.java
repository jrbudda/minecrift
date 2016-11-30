package com.mtbs3d.minecrift.gameplay;

import com.mtbs3d.minecrift.provider.MCOpenVR;
import com.mtbs3d.minecrift.provider.OpenVRPlayer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * Created by Hendrik on 02-Aug-16.
 */
public class SwimTracker {

	Vec3 motion = Vec3.createVectorHelper(0, 0, 0);
	double friction=0.9f;

	double lastDist;

	final double riseSpeed=0.005f;
	double swimspeed=0.8f;

	public boolean isActive(EntityPlayerSP p){
		if(!Minecraft.getMinecraft().vrSettings.vrFreeMove || Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(p==null || p.isDead)
			return false;
		if(!p.isInWater()) //) && !p.isInLava())
			return false;
		
		//Block block=p.worldObj.getBlockState(p.getPosition().add(0,0.7,0)).getBlock();
		return true;
	}

	public void doProcess(Minecraft minecraft, EntityPlayerSP player){
		if(!isActive(player)) {
			return;
		}

		Vec3 face = minecraft.roomScale.getHMDPos_World();
		Vec3 off = Vec3.createVectorHelper(0,minecraft.roomScale.getHMDPos_Room().yCoord * 0.9, 0);
		Vec3 feets = off.subtract(face);
		double waterLine=256;

        int bx = (int) MathHelper.floor_double(feets.xCoord);
        int by = (int) MathHelper.floor_double(feets.yCoord);
        int bz = (int) MathHelper.floor_double(feets.zCoord);
		
		Block bp = player.worldObj.getBlock(bx, by, bz);
		for (int i = 0; i < 4; i++) {
			Material mat=bp.getMaterial();
			
			if(!mat.isLiquid())
			{
				waterLine=by;
				break;
			}
			by++;
			bp = player.worldObj.getBlock(bx, by, bz);
		}

		double percent = (waterLine - feets.yCoord) / (face.yCoord - feets.yCoord);

		if(percent < 0){
			//how did u get here, drybones?
			return;
		}

		if(percent < 0.5 && player.onGround){
			return;
			//no diving in the kiddie pool.
		}
		
		player.addVelocity(0, 0.018D , 0); //counteract most gravity.
		
		double neutal = player.isCollidedHorizontally? 0.5 : 1;
		
		if(percent > neutal && percent < 2){ //between halfway submerged and 1 body length under.
			//rise!
			double buoyancy = 2 - percent;
			if(player.isCollidedHorizontally)  player.addVelocity(0, 00.03f, 0);	
	        player.addVelocity(0, 0.0015 + buoyancy/100 , 0);		
		}


//		gravityOverride=true;
//		player.setNoGravity(true);
//
//		Vec3d playerpos=player.getPositionVector();
//
//		double swimHeight= MCOpenVR.hmdPivotHistory.latest().yCoord;//new Vec3d(0,1.5,0);
//		double maxSwim= swimHeight/2;
//
//		double depth=2;
//
//		for (int i = 0; i < 4; i++) {
//			BlockPos blockpos=new BlockPos(playerpos.add(new Vec3d(0,i+0.5,0)));
//			Material block=player.worldObj.getBlockState(blockpos).getMaterial();
//			if(!block.isLiquid())
//			{
//				depth=blockpos.getY()-playerpos.yCoord-2;
//				break;
//			}
//		}
//
//		if (depth > 2)
//				depth = 2;
//
//		double buoyancy=(1-depth);
//		
//		Material block1=player.worldObj.getBlockState(new BlockPos(playerpos.addVector(0,swimHeight,0))).getMaterial();
//		if(!block1.isLiquid()){
//			//we are at the surface
//			Material block2=player.worldObj.getBlockState(new BlockPos(playerpos.addVector(0,maxSwim,0))).getMaterial();
//			if(!block2.isLiquid()){
//				//Too high
//				player.setNoGravity(false);
//			}
//
//		}else{
//			player.addVelocity(0, buoyancy<0 ? sinkspeed*buoyancy : riseSpeed*buoyancy, 0);
//		}

		Vec3 controllerR= minecraft.roomScale.getControllerPos_World(0);
		Vec3 controllerL= minecraft.roomScale.getControllerPos_World(1);

		Vec3 middle= (controllerL.subtractProperly(controllerR).scale(0.5)).add(controllerR);

		Vec3 hmdPos=minecraft.roomScale.getHMDPos_World().subtractProperly(0,0.3,0);

		Vec3 movedir=middle.subtractProperly(hmdPos).normalize().add(
				minecraft.roomScale.getHMDDir_World()).scale(0.5);

		Vec3 contollerDir= minecraft.roomScale.getCustomControllerVector(0,new Vec3(0,0,-1)).add(
				minecraft.roomScale.getCustomControllerVector(1,new Vec3(0,0,-1))).scale(0.5);
		double dirfactor=contollerDir.add(movedir).lengthVector()/2;

		double distance= hmdPos.distanceTo(middle);
		double distDelta=lastDist-distance;

		if(distDelta>0){
			Vec3 velo= movedir.scale(distDelta*swimspeed*dirfactor);	
			motion=motion.add(velo.scale(0.15));
		}

		lastDist=distance;
		player.addVelocity(motion.xCoord,motion.yCoord,motion.zCoord);
		motion=motion.scale(friction);

	}

}
