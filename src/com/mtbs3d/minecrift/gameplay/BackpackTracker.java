package com.mtbs3d.minecrift.gameplay;

import com.mtbs3d.minecrift.api.IRoomscaleAdapter;
import com.mtbs3d.minecrift.provider.MCOpenVR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import java.util.Random;

/**
 * Created by cincodenada on 13-May-17.
 */
public class BackpackTracker {
	public boolean[] wasIn = new boolean[2];
	public int previousSlot = -1;
	
	public boolean isActive(EntityPlayerSP p){
		if(Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(p == null) return false;
		if(p.isDead) return false;
		if(p.isPlayerSleeping()) return false;
		return true;
	}

	
	private Vec3 down = Vec3.createVectorHelper(0, -1, 0);
	
	public void doProcess(Minecraft minecraft, EntityPlayerSP player){
		if(!isActive(player)) {
			return;
		}
		IRoomscaleAdapter provider = minecraft.roomScale;

		Vec3 hmdPos=provider.getHMDPos_Room();

		for(int c=0; c<1; c++) { //just main for 1710, no dual wielding
			Vec3 controllerPos = provider.getControllerPos_Room(c);//.add(provider.getCustomControllerVector(c, new Vec3(0, 0, -0.1)));
			Vec3 controllerDir = minecraft.roomScale.getControllerDir_World(c);
			Vec3 hmddir = provider.getHMDDir_World();
			Vec3 hmdpos = provider.getHMDPos_Room();
			Vec3 delta = hmdPos.subtractProperly(controllerPos);
			double dot = controllerDir.dotProduct(down);
			double dotDelta = delta.dotProduct(hmddir);

			boolean zone = ((Math.abs(hmdPos.yCoord - controllerPos.yCoord)) < 0.25) && //controller below hmd
					(dotDelta > 0); // behind head
			
			if (zone){
				if(!wasIn[c]){
					if(player.inventory.currentItem != 0){
						previousSlot = player.inventory.currentItem;
						player.inventory.currentItem = 0;	
					} else {
						player.inventory.currentItem = previousSlot;
						previousSlot = -1;
					}
					provider.triggerHapticPulse(c, 1500);
					wasIn[c] = true;
				}
			} else {
				wasIn[c] = false;
			}
		}
}

}
