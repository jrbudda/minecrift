package com.mtbs3d.minecrift.gameplay;

import java.util.Random;

import com.mtbs3d.minecrift.api.IRoomscaleAdapter;
import com.mtbs3d.minecrift.provider.MCOpenVR;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

/**
 * Created by Hendrik on 02-Aug-16.
 */
public class EatingTracker {
	float mouthtoEyeDistance=0.0f;
	float threshold=0.25f;
	public boolean[] eating= new boolean[2];
	int eattime=2100;
	long eatStart;

	public boolean isEating(){
		return eating[0] || eating[1];
	}
	
	public boolean isActive(EntityPlayerSP p){
		if(Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(p == null) return false;
		if(p.isDead) return false;
		if(p.isPlayerSleeping()) return false;
		if(p.getHeldItem()!= null){
			EnumAction action=p.getHeldItem().getItemUseAction();
			if(	action == EnumAction.eat || action == EnumAction.drink) return true;
		}
		return false;
	}

private Random r = new Random();

	
public void doProcess(Minecraft minecraft, EntityPlayerSP player){
	if(!isActive(player)) {
		eating[0]=false;
		eating[1]=false;
		return;
	}
	IRoomscaleAdapter provider = minecraft.roomScale;

	Vec3 hmdPos=provider.getHMDPos_Room();
	Vec3 mouthPos=provider.getCustomHMDVector(new Vec3(0,-mouthtoEyeDistance,0)).add(hmdPos);

	int c = 0;

	Vec3 controllerPos=MCOpenVR.controllerHistory[c].averagePosition(0.333).add(provider.getCustomControllerVector(c,new Vec3(0,0,-0.1)));
	controllerPos = controllerPos.add(minecraft.roomScale.getControllerDir_Room(c).scale(0.1));

	if(mouthPos.distanceTo(controllerPos)<threshold){
		ItemStack is = c==0?player.getHeldItem():player.getHeldItem();
		if(is == null) return;

		if(is.getItemUseAction() == EnumAction.drink){ //thats how liquid works.
			if(minecraft.roomScale.getCustomControllerVector(c, new Vec3(0,1,0)).yCoord > 0) return;
		}

		if(!eating[c]){
			Minecraft.getMinecraft().playerController.sendUseItem(player, player.worldObj, is);//server
			eating[c]=true;
			eatStart=Minecraft.getSystemTime();			
			
//			if(	Minecraft.getMinecraft().playerController.onPlayerRightClick(player, player.worldObj,is,0,0,0,0, new Vec3(0,0,0))){
//				//minecraft.entityRenderer.itemRenderer.resetEquippedProgress();
//			}
		}
		int crunchiness;
		if(is.getItemUseAction() == EnumAction.drink){
			crunchiness=0;
		}else
			crunchiness=2;

		long t = player.getItemInUseCount();
		if(t>0)
			if(t%5 <= crunchiness)
				minecraft.vrPlayer.triggerHapticPulse(c, 700 );

		if(Minecraft.getSystemTime()-eatStart > eattime)
			eating[c]=false;

	}else {
		eating[c]=false;
	}
}

}
