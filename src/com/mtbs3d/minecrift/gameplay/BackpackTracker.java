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
	float mouthtoEyeDistance=0.0f;
	float threshold=0.25f;
	public Item[] items = new Item[2];
	public boolean[] wasIn = new boolean[2];
	public boolean[] wasPressed = new boolean[2];

	public boolean isActive(EntityPlayerSP p){
		if(Minecraft.getMinecraft().vrSettings.seated)
			return false;
		if(p == null) return false;
		if(p.isDead) return false;
		if(p.isPlayerSleeping()) return false;
		return true;
	}

private Random r = new Random();

	
public void doProcess(Minecraft minecraft, EntityPlayerSP player){
	if(!isActive(player)) {
		return;
	}
	IRoomscaleAdapter provider = minecraft.roomScale;

	Vec3 hmdPos=provider.getHMDPos_Room();

	for(int c=0; c<2; c++) {
		Vec3 controllerPos = MCOpenVR.controllerHistory[c].averagePosition(0.333).add(provider.getCustomControllerVector(c, new Vec3(0, 0, -0.1)));
		controllerPos = controllerPos.add(minecraft.roomScale.getControllerDir_Room(c).scale(0.1));

		if (
				(Math.abs(hmdPos.yCoord - controllerPos.yCoord) < 0.25)
						&& controllerPos.zCoord > hmdPos.zCoord
						&& ((controllerPos.zCoord - hmdPos.zCoord) < 0.5)
				) {
			wasIn[c] = true;
			if(!wasPressed[c]) {
				wasPressed[c] = Minecraft.getMinecraft().gameSettings.keyBindAttack.getIsKeyPressed();
			}
		} else {
			// Only run once per zone entrance
			if (wasIn[c]) {
				provider.triggerHapticPulse(c,1500);

				// If we came in pressing
				if (wasPressed[c]) {
					this.items[c] = player.getHeldItem().getItem();
				} else {
					player.inventory.setCurrentItem(this.items[c], 0, false, player.capabilities.isCreativeMode);
				}
			}
			// Reset state
			wasIn[c] = false;
			wasPressed[c] = false;
		}
	}
}

}
