package com.mtbs3d.minecrift.gameplay;

import com.mtbs3d.minecrift.api.IRoomscaleAdapter;
import com.mtbs3d.minecrift.provider.MCOpenVR;

import com.mtbs3d.minecrift.settings.AutoCalibration;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class JumpTracker {


	public boolean isActive(EntityPlayerSP p){
		if(Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(!Minecraft.getMinecraft().vrSettings.vrFreeMove && !Minecraft.getMinecraft().vrSettings.simulateFalling)
			return false;
		if(!Minecraft.getMinecraft().vrSettings.realisticJumpEnabled)
			return false;
		if(p==null || p.isDead || !p.onGround)
			return false;
		if(p.isInWater())
			return false;
		if(p.isSneaking() || p.isRiding())
			return false;
		return true;
	}

	public void doProcess(Minecraft minecraft, EntityPlayerSP player){
		if(!isActive(player)) {
			return;
		}

		if(MCOpenVR.hmdPivotHistory.netMovement(0.25).yCoord > 0.1 &&
				MCOpenVR.hmdPivotHistory.latest().yCoord-AutoCalibration.getPlayerHeight() > minecraft.vrSettings.jumpThreshold
				){
			player.jump();
		}
	}
}
