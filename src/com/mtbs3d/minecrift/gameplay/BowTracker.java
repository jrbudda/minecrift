package com.mtbs3d.minecrift.gameplay;

import java.nio.ByteBuffer;

import com.mtbs3d.minecrift.api.IRoomscaleAdapter;
import com.mtbs3d.minecrift.api.NetworkHelper;
import com.mtbs3d.minecrift.api.NetworkHelper.PacketDiscriminators;
import com.mtbs3d.minecrift.provider.MCOpenVR;

import de.fruitfly.ovr.structs.Matrix4f;
import de.fruitfly.ovr.structs.Vector3f;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

public class BowTracker {

	
	private double lastcontrollersDist;
	private double lastcontrollersDot;	
	private double controllersDist;
	private double controllersDot;
	private double currentDraw;
	private double lastDraw;
	public boolean isDrawing; 
	private boolean pressed, lastpressed;	
	
	private boolean canDraw, lastcanDraw;
	
	
	private Vec3 leftHandAim;
	
	private final double notchDotThreshold = 10;
	private double maxDraw = .7;

	private Vec3 aim;
	
	public Vec3 getAimVector(){
		return aim;
//		if(isDrawing)return aim;
//		return leftHandAim;
	}
		
	public double getDrawPercent(){
		return currentDraw / maxDraw;	
	}
	
	public boolean isNotched(){
		return canDraw || isDrawing;	
	}
	
	public boolean isActive(EntityPlayerSP p){
		if(p == null) return false;
		if(p.isDead) return false;
		if(p.isPlayerSleeping()) return false;
		if(p.inventory == null) return false;
		if(p.inventory.getCurrentItem() == null) return false;
		return	p.inventory.getCurrentItem().getItem() instanceof ItemBow;
	}
	
	float tsNotch = 0;
	
	int hapcounter = 0;
	
	public void doProcess(IRoomscaleAdapter provider, EntityPlayerSP player){

		if (!isActive(player)){			
			isDrawing = false;
			return;
		}

		if(Minecraft.getMinecraft().vrSettings.seated){
			aim = 	provider.getControllerMainDir_World();
			return;
		}
		
		ItemStack bow = player.inventory.getCurrentItem();

		lastcontrollersDist = controllersDist;
		lastcontrollersDot = controllersDot;
		lastpressed = pressed;
		lastDraw = currentDraw;
		lastcanDraw = canDraw;
		maxDraw = Minecraft.getMinecraft().thePlayer.height * 0.25;

		Vec3 rightPos = provider.getControllerPos_World(0);
		Vec3 leftPos = provider.getControllerPos_World(1);
		controllersDist = leftPos.distanceTo(rightPos);
		
		aim = rightPos.subtract(leftPos).normalize();

		Vector3f forward = new Vector3f(0,0,1);

		Vec3 rightaim3 = provider.getControllerMainDir_World();
		
		Vector3f rightAim = new Vector3f((float)rightaim3.xCoord, (float) rightaim3.yCoord, (float) rightaim3.zCoord);
		leftHandAim = provider.getControllerOffhandDir_World();
	 	Vec3 l4v3 = provider.getCustomControllerVector(1, Vec3.createVectorHelper(0, -1, 0));
		 
		Vector3f leftforeward = new Vector3f((float)l4v3.xCoord, (float) l4v3.yCoord, (float) l4v3.zCoord);

		controllersDot = 180 / Math.PI * Math.acos(leftforeward.dot(rightAim));

		pressed = Minecraft.getMinecraft().gameSettings.keyBindAttack.getIsKeyPressed();

		float notchDistThreshold = (float) (0.3 * Minecraft.getMinecraft().vrPlayer.worldScale);
		
		boolean infiniteAmmo = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) > 0;

		if( controllersDist <= notchDistThreshold && controllersDot <= notchDotThreshold && (infiniteAmmo || player.inventory.hasItem(Items.arrow)))
		{
			//can draw
			canDraw = true;
			tsNotch = Minecraft.getSystemTime();
			
			if(!isDrawing){
				player.setItemInUseClient(bow);
				player.setItemInUseCountClient(bow.getMaxItemUseDuration() - 1 );				
			}

		} else if((Minecraft.getSystemTime() - tsNotch) > 500) {
			canDraw = false;
			player.setItemInUseClient(null);//client draw only
		}
			
		if (!isDrawing && canDraw  && pressed && !lastpressed) {
			//draw     	    	
			isDrawing = true;
			Minecraft.getMinecraft().playerController.sendUseItem(player, player.worldObj, bow);//server
		}

		if(isDrawing && !pressed && lastpressed && getDrawPercent() > 0.0) {
			//fire!
			provider.triggerHapticPulse(0, 500); 	
			provider.triggerHapticPulse(1, 3000); 	
			NetworkHelper.getVivecraftClientPacket(PacketDiscriminators.DRAW, ByteBuffer.allocate(4).putFloat((float) getDrawPercent()).array());
			Minecraft.getMinecraft().playerController.onStoppedUsingItem(player); //server
			isDrawing = false;     	
		}
		
		if(!pressed){
			isDrawing = false;
		}
		
		if (!isDrawing && canDraw && !lastcanDraw) {
			provider.triggerHapticPulse(1, 800);
			provider.triggerHapticPulse(0, 800); 	
			//notch     	    	
		}
		
		if(isDrawing){
			currentDraw = controllersDist - notchDistThreshold ;
			if (currentDraw > maxDraw) currentDraw = maxDraw;		
			
			int hap = 0;
			if (getDrawPercent() > 0 ) hap = (int) (getDrawPercent() * 1000)+ 200;
		
			int use = (int) (bow.getMaxItemUseDuration() - getDrawPercent() * bow.getMaxItemUseDuration());
			if	(use >= bow.getMaxItemUseDuration()) use = bow.getMaxItemUseDuration() -1;
			player.setItemInUseClient(bow);//client draw only
			player.setItemInUseCountClient(use -1); //do this cause the above doesnt set the counts if same item.
			hapcounter ++ ;
			if (hapcounter % 4 == 0)
				provider.triggerHapticPulse(0, hap);     


		} else {
			hapcounter = 0;
		}


	}
	
	
	
	
	
}

