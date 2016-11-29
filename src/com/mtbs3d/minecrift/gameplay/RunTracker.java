package com.mtbs3d.minecrift.gameplay;

import com.mtbs3d.minecrift.api.IRoomscaleAdapter;
import com.mtbs3d.minecrift.provider.MCOpenVR;
import com.mtbs3d.minecrift.settings.VRSettings;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.Vec3;


public class RunTracker {


	public boolean isActive(EntityPlayerSP p){
		if(!Minecraft.getMinecraft().vrSettings.vrFreeMove || Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(Minecraft.getMinecraft().vrSettings.vrFreeMoveMode != VRSettings.FREEMOVE_RUNINPLACE)
			return false;
		if(p==null || p.isDead)
			return false;
		if(!p.onGround && (p.isInWater())) 
			return false;
		if(p.isOnLadder()) 
			return false;
		return true;
	}

	public double direction = 0;
	private double speed = 0;
	private Vec3 movedir;
	
	public double getYaw(){
		return direction;
	}
	
	public double getSpeed(){
		return speed;
	}
	public void doProcess(Minecraft minecraft, EntityPlayerSP player){
		if(!isActive(player)) {
			speed = 0;
			return;
		}

		Vec3 controllerR= minecraft.roomScale.getControllerPos_World(0);
		Vec3 controllerL= minecraft.roomScale.getControllerPos_World(1);

		//Vec3d middle= controllerL.subtract(controllerR).scale(0.5).add(controllerR);

		double c0move = MCOpenVR.controllerHistory[0].averageSpeed(.5);
		double c1move = MCOpenVR.controllerHistory[1].averageSpeed(.5);

		if(speed > 0) {
			if(c0move < 0.1 && c1move < 0.1){
				speed = 0;
				return;
			}
		}else {
			if(c0move < 0.6 && c1move < 0.6){
				speed = 0;
				return;
			}
		}
		
		if(Math.abs(c0move - c1move) > .5){
			speed = 0;
			return;
		} //2 hands plz.
			
		
//		Vec3d hmdPos = minecraft.roomScale.getHMDPos_World();
//
//		Vec3d Hmddir = minecraft.roomScale.getHMDDir_World();
//		Hmddir= new Vec3d(Hmddir.xCoord,0, Hmddir.zCoord).normalize();	
//		
//		if(speed < 0) Hmddir = movedir; //maybe?
//		
//		Vec3d r = MCOpenVR.controllerHistory[0].netMovement(0.33).rotateYaw(minecraft.vrPlayer.worldRotationRadians);
//		Vec3d l = MCOpenVR.controllerHistory[1].netMovement(0.33).rotateYaw(minecraft.vrPlayer.worldRotationRadians);
//
//		r = new Vec3d(r.xCoord,0, r.zCoord).normalize();		
//		l = new Vec3d(l.xCoord,0, l.zCoord).normalize();	
//		
//		double dotr = r.dotProduct(Hmddir);
//		double dotl = l.dotProduct(Hmddir);
//		
//		if(dotr <0) r = r.scale(-1);
//		if(dotl <0) l = l.scale(-1);
//		
//	//	if(Math.abs(dotr) < 0.5 || Math.abs(dotl) < 0.5) return;
//		
//		movedir = new Vec3d((r.xCoord + l.xCoord) / 2, 0, (r.zCoord + l.zCoord) / 2);
//		
//		Vec3d movedir= new  Vec3d(middle.xCoord - hmdPos.xCoord, 0, middle.zCoord - hmdPos.zCoord);
//		//TODO: do this betterer. Use actual controller movement dir in x-z plane.
//		movedir = movedir.normalize();
		
		//todo: skip entries? is this computationally expensive?
		Vec3 r = MCOpenVR.controllerHistory[0].averagePosition(.5);
		Vec3 l = MCOpenVR.controllerHistory[1].averagePosition(.5);
		
		Vec3 diff;
		
		if(Minecraft.getMinecraft().vrSettings.vrReverseHands){
			diff = l.subtract(r).rotateYaw(minecraft.getMinecraft().vrPlayer.worldRotationRadians);			
		} else {
			diff = r.subtract(l).rotateYaw(minecraft.getMinecraft().vrPlayer.worldRotationRadians);	
		}

		
		double ltor = Math.toDegrees(Math.atan2(-diff.xCoord, diff.zCoord));   
		
		direction = ltor + (Minecraft.getMinecraft().vrSettings.vrReverseHands ? -1 : 1) * 90;   
	
		double spd = (c0move + c1move) / 2;	
		this.speed = spd * 1 * 1.3;
		if(this.speed > 1.3) this.speed = 1.3f;
		
	}

}
