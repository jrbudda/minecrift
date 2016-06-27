package com.mtbs3d.minecrift.provider;


import de.fruitfly.ovr.enums.EyeType;
import de.fruitfly.ovr.structs.EulerOrient;
import de.fruitfly.ovr.structs.Matrix4f;
import de.fruitfly.ovr.structs.Quatf;
import de.fruitfly.ovr.structs.Vector3f;
import de.fruitfly.ovr.util.BufferUtil;
import io.netty.util.concurrent.GenericFutureListener;
import jopenvr.OpenVRUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import javax.swing.plaf.multi.MultiViewportUI;

import org.lwjgl.util.vector.Quaternion;

import com.google.common.base.Charsets;
import com.mtbs3d.minecrift.api.IRoomscaleAdapter;
import com.mtbs3d.minecrift.gameplay.EntityVRTeleportFX;
import com.mtbs3d.minecrift.gameplay.VRMovementStyle;
import com.mtbs3d.minecrift.render.QuaternionHelper;

// VIVE
public class OpenVRPlayer implements IRoomscaleAdapter
{
    public double lastRoomUpdateTime = 0;
    public Vec3 movementTeleportDestination = Vec3.createVectorHelper(0.0,0.0,0.0);
    public int movementTeleportDestinationSideHit;
    public double movementTeleportProgress;
    public double movementTeleportDistance;
        
    private Vec3 roomOrigin = Vec3.createVectorHelper(0,0,0);
    private Vec3 lastroomOrigin = Vec3.createVectorHelper(0,0,0);
    private Vec3 interPolatedRoomOrigin = Vec3.createVectorHelper(0,0,0);
    
    public VRMovementStyle vrMovementStyle = new VRMovementStyle();
    public Vec3[] movementTeleportArc = new Vec3[50];
    public int movementTeleportArcSteps = 0;
    private boolean freeMoveMode = true;        // true when connected to another server that doesn't have this mod
	public boolean useLControllerForRestricedMovement = true;
    public double lastTeleportArcDisplayOffset = 0;
    public boolean noTeleportClient = true;
    
    private float teleportEnergy;
    
    public static OpenVRPlayer get()
    {
        return Minecraft.getMinecraft().vrPlayer;
    }

    public OpenVRPlayer()
    {
        for (int i=0;i<50;i++)
        {
            movementTeleportArc[i] = Vec3.createVectorHelper(0,0,0);
        }
    }
   
    public void onFrameUpdate(float nano){
     	interPolatedRoomOrigin = getInterpolatedRoomOriginPos_World(nano);
    }
    
    public void setRoomOrigin(double x, double y, double z, boolean reset ) { 
    	if (reset){
    		interPolatedRoomOrigin = Vec3.createVectorHelper(x, y, z);
    		lastroomOrigin = Vec3.createVectorHelper(x, y, z);
    	}
    	    	
    	this.roomOrigin.xCoord = x;
    	this.roomOrigin.yCoord = y;
    	this.roomOrigin.zCoord = z;
        lastRoomUpdateTime = Minecraft.getMinecraft().stereoProvider.getCurrentTimeSecs();
    }
    
    //set room 
    public void snapRoomOriginToPlayerEntity(Entity player, boolean reset)
    {
        if (Thread.currentThread().getName().equals("Server thread"))
            return;

        if(player.posX == 0 && player.posY == 0 &&player.posZ == 0) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        
        Vec3 campos = mc.roomScale.getHMDPos_Room();
        
        campos.rotateAroundY(worldRotationRadians);
                
        double x = player.posX - campos.xCoord;
        double y = player.boundingBox.minY;
        double z = player.posZ - campos.zCoord;

        setRoomOrigin(x, y, z, reset);

    }
    
    public  double topofhead = 1.62;
    
    
    private float lastworldRotation= 0f;
	private float lastWorldScale;
    
	public void checkandUpdateRotateScale(){
		Minecraft mc = Minecraft.getMinecraft();
	    this.worldScale =  mc.vrSettings.vrWorldScale;
	    this.worldRotationRadians = (float) Math.toRadians(mc.vrSettings.vrWorldRotation);
	    
	    if (worldRotationRadians!= lastworldRotation || worldScale != lastWorldScale) {
	    	if(mc.thePlayer!=null)snapRoomOriginToPlayerEntity(mc.thePlayer, true);
	    	MCOpenVR.onGuiScreenChanged(mc.currentScreen, mc.currentScreen);
	    }
	    lastworldRotation = worldRotationRadians;
	    lastWorldScale = worldScale;		
	}

	
	
	
    public void onLivingUpdate(EntityPlayerSP player, Minecraft mc, Random rand)
    {
    	if(!player.initFromServer) return;
    	
    	this.lastroomOrigin.xCoord = roomOrigin.xCoord ;
    	this.lastroomOrigin.yCoord = roomOrigin.yCoord ;
    	this.lastroomOrigin.zCoord = roomOrigin.zCoord ;
        updateSwingAttack();
        
        this.checkandUpdateRotateScale();

        
       if(false){         //experimental - EXPERIMENT FAILED
           topofhead = (double) (mc.roomScale.getHMDPos_Room().yCoord + .05);
           
           if(topofhead < .5) {topofhead = 0.5f;}
           if(topofhead > 1.8) {topofhead = 1.8f;}
           
           player.height = (float) topofhead - 0.05f;
           player.boundingBox.maxY = player.boundingBox.minY +  topofhead;  	   
       } else {
    	  // player.height = 1.8f;
       }
      
        // don't do teleport movement if on a server that doesn't have this mod installed
        if (getFreeMoveMode()) {
        	
        		if(player.movementInput.moveForward ==0) doPlayerMoveInRoom(player);
        	
			  return; //let mc handle look direction movement
			// controller vs gaze movement is handled in Entity.java > moveFlying
          }
				
        mc.mcProfiler.startSection("VRPlayerOnLivingUpdate");

        if (teleportEnergy < 100) { teleportEnergy++;}
        
        boolean doTeleport = false;
        Vec3 dest = null;

        if (player.movementInput.moveForward != 0 && !player.isRiding()) //holding down Ltrigger
        {
            dest = movementTeleportDestination;

            if (vrMovementStyle.teleportOnRelease)
            {
                if (player.movementTeleportTimer==0)
                {
                    String sound = vrMovementStyle.startTeleportingSound;
                    if (sound != null)
                    {
                        player.playSound(sound, vrMovementStyle.startTeleportingSoundVolume,
                                1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 1.0f * 0.5F);
                    }
                }
                player.movementTeleportTimer++;
                if (player.movementTeleportTimer > 0)
                {
                    movementTeleportProgress = (float) player.movementTeleportTimer / 4.0f;
                    if (movementTeleportProgress>=1.0f)
                    {
                        movementTeleportProgress = 1.0f;
                    }

                    if (dest.xCoord != 0 || dest.yCoord != 0 || dest.zCoord != 0)
                    {
                        Vec3 eyeCenterPos = getHMDPos_World();

                        // cloud of sparks moving past you
                        Vec3 motionDir = dest.addVector(-eyeCenterPos.xCoord, -eyeCenterPos.yCoord, -eyeCenterPos.zCoord).normalize();
                        Vec3 forward;
						
						forward	= player.getLookVec();

                        Vec3 right = forward.crossProduct(Vec3.createVectorHelper(0, 1, 0));
                        Vec3 up = right.crossProduct(forward);

                        if (vrMovementStyle.airSparkles)
                        {
                            for (int iParticle = 0; iParticle < 3; iParticle++)
                            {
                                double forwardDist = rand.nextDouble() * 1.0 + 3.5;
                                double upDist = rand.nextDouble() * 2.5;
                                double rightDist = rand.nextDouble() * 4.0 - 2.0;

                                Vec3 sparkPos = Vec3.createVectorHelper(eyeCenterPos.xCoord + forward.xCoord * forwardDist,
                                        eyeCenterPos.yCoord + forward.yCoord * forwardDist,
                                        eyeCenterPos.zCoord + forward.zCoord * forwardDist);
                                sparkPos = sparkPos.addVector(right.xCoord * rightDist, right.yCoord * rightDist, right.zCoord * rightDist);
                                sparkPos = sparkPos.addVector(up.xCoord * upDist, up.yCoord * upDist, up.zCoord * upDist);

                                double speed = -0.6;
                                EntityFX particle = new EntityVRTeleportFX(
                                        player.worldObj,
                                        sparkPos.xCoord, sparkPos.yCoord, sparkPos.zCoord,
                                        motionDir.xCoord * speed, motionDir.yCoord * speed, motionDir.zCoord * speed,
                                        1.0f);
                                mc.effectRenderer.addEffect(particle);
                            }
                        }
                    }
                }
            }
            else
            {
                if (player.movementTeleportTimer >= 0 && (dest.xCoord != 0 || dest.yCoord != 0 || dest.zCoord != 0))
                {
                    if (player.movementTeleportTimer == 0)
                    {
                        String sound = vrMovementStyle.startTeleportingSound;
                        if (sound != null)
                        {
                            player.playSound(sound, vrMovementStyle.startTeleportingSoundVolume,
                                    1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 1.0f * 0.5F);
                        }
                    }
                    player.movementTeleportTimer++;

                    Vec3 playerPos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
                    double dist = dest.distanceTo(playerPos);
                    double progress = (player.movementTeleportTimer * 1.0) / (dist + 3.0);

                    if (player.movementTeleportTimer > 0)
                    {
                        movementTeleportProgress = progress;

                        // spark at dest point
                        if (vrMovementStyle.destinationSparkles)
                        {
                            player.worldObj.spawnParticle("instantSpell", dest.xCoord, dest.yCoord, dest.zCoord, 0, 1.0, 0);
                        }

                        // cloud of sparks moving past you
                        Vec3 motionDir = dest.addVector(-player.posX, -player.posY, -player.posZ).normalize();
                        Vec3 forward = player.getLookVec();
                        Vec3 right = forward.crossProduct(Vec3.createVectorHelper(0, 1, 0));
                        Vec3 up = right.crossProduct(forward);

                        if (vrMovementStyle.airSparkles)
                        {
                            for (int iParticle = 0; iParticle < 3; iParticle++)
                            {
                                double forwardDist = rand.nextDouble() * 1.0 + 3.5;
                                double upDist = rand.nextDouble() * 2.5;
                                double rightDist = rand.nextDouble() * 4.0 - 2.0;
                                Vec3 sparkPos = Vec3.createVectorHelper(player.posX + forward.xCoord * forwardDist,
                                        player.posY + forward.yCoord * forwardDist,
                                        player.posZ + forward.zCoord * forwardDist);
                                sparkPos = sparkPos.addVector(right.xCoord * rightDist, right.yCoord * rightDist, right.zCoord * rightDist);
                                sparkPos = sparkPos.addVector(up.xCoord * upDist, up.yCoord * upDist, up.zCoord * upDist);

                                double speed = -0.6;
                                EntityFX particle = new EntityVRTeleportFX(
                                        player.worldObj,
                                        sparkPos.xCoord, sparkPos.yCoord, sparkPos.zCoord,
                                        motionDir.xCoord * speed, motionDir.yCoord * speed, motionDir.zCoord * speed,
                                        1.0f);
                                mc.effectRenderer.addEffect(particle);
                            }
                        }
                    } else
                    {
                        movementTeleportProgress = 0;
                    }

                    if (progress >= 1.0)
                    {
                        doTeleport = true;
                    }
                }
            }
        }
        else //not holding down Ltrigger
        {
            if (vrMovementStyle.teleportOnRelease && movementTeleportProgress>=1.0f)
            {
                dest = movementTeleportDestination;
                doTeleport = true;
            }
            player.movementTeleportTimer = 0;
            movementTeleportProgress = 0;
        }

        if (doTeleport && dest!=null && (dest.xCoord != 0 || dest.yCoord !=0 || dest.zCoord != 0)) //execute teleport
        {
            movementTeleportDistance = (float)MathHelper.sqrt_double(dest.squareDistanceTo(player.posX, player.posY, player.posZ));
            boolean playTeleportSound = movementTeleportDistance > 0.0f && vrMovementStyle.endTeleportingSound != null;
            Block block = null;

            if (playTeleportSound)
            {
                String sound = vrMovementStyle.endTeleportingSound;
                if (sound != null)
                {
                    player.playSound(sound, vrMovementStyle.endTeleportingSoundVolume, 1.0F);
                }
            }
            else
            {
                playFootstepSound(mc, dest.xCoord, dest.yCoord, dest.zCoord);
            }

            this.disableSwing = 3;
            
            //execute teleport      
            player.setPositionAndUpdate(dest.xCoord, dest.yCoord, dest.zCoord);         
            snapRoomOriginToPlayerEntity(player, false);
            roomScaleMovementDelay = 3;
            
            if(mc.vrSettings.vrLimitedSurvivalTeleport){
              player.addExhaustion((float) (movementTeleportDistance / 16 * 1.2f));    
              
              if (!mc.vrPlayer.getFreeMoveMode() && mc.playerController.isNotCreative() && mc.vrPlayer.vrMovementStyle.arcAiming){
              	teleportEnergy -= movementTeleportDistance * 4;	
              }              
            }
               
            
          //  System.out.println("teleport " + dest.toString());
            player.fallDistance = 0.0F;

            if (playTeleportSound)
            {
                String sound = vrMovementStyle.endTeleportingSound;
                if (sound != null)
                {
                    player.playSound(sound, vrMovementStyle.endTeleportingSoundVolume, 1.0F);
                }
            }
            else
            {
                playFootstepSound(mc, dest.xCoord, dest.yCoord, dest.zCoord);
            }

            player.movementTeleportTimer = -1;
            
        }
        else //standing still
        {
			doPlayerMoveInRoom(player);
        }
        mc.mcProfiler.endSection();
    }

    private boolean wasYMoving;
    private int roomScaleMovementDelay = 0;
    
    private void doPlayerMoveInRoom(EntityPlayerSP player){
    	// this needs... work...
    //	if(true) return;
    	if(roomScaleMovementDelay > 0){
    		roomScaleMovementDelay--;
    		return;
    	}
    	if(player.isSneaking()) {return;} //jrbudda : prevent falling off things or walking up blocks while moving in room scale.
    	if(player.isRiding()) return; //dont fall off the tracks man
    	if(player.isDead) return; //
    	if(player.isPlayerSleeping()) return; //
    	if(this.interPolatedRoomOrigin.xCoord == 0 && this.interPolatedRoomOrigin.yCoord ==0 && this.interPolatedRoomOrigin.zCoord == 0) return;
    	
    	if(Math.abs(player.motionX) > 0.01) return;
    	if(Math.abs(player.motionZ) > 0.01) return;
    	
    	Minecraft mc = Minecraft.getMinecraft();
    	float playerHalfWidth = player.width / 2.0F;

    	// move player's X/Z coords as the HMD moves around the room

    	Vec3 eyePos = getHMDPos_World();

    	double x = eyePos.xCoord;
    	double y = player.posY;
    	double z = eyePos.zCoord;

    	// create bounding box at dest position
    	AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
    			x - (double) playerHalfWidth,
    			y - (double) player.yOffset + (double) player.yOffset2,
    			z - (double) playerHalfWidth,
    			x + (double) playerHalfWidth,
    			y - (double) player.yOffset + (double) player.yOffset2 + (double) player.height,
    			z + (double) playerHalfWidth);

    	Vec3 torso = null;

    	// valid place to move player to?
    	float var27 = 0.0625F;
    	boolean emptySpot = mc.theWorld.getCollidingBoundingBoxes(player, bb).isEmpty();

    	if (emptySpot)
    	{
    		// don't call setPosition style functions to avoid shifting room origin
    		player.lastTickPosX = player.prevPosX = player.posX = x;
    		if (!mc.vrSettings.simulateFalling)	{
    			player.lastTickPosY = player.prevPosY = player.posY = y;                	
    		}
    		player.lastTickPosZ = player.prevPosZ = player.posZ = z;
    
    		 if(player.ridingEntity!=null){ //you're coming with me, horse! //TODO: use mount's bounding box.
    				player.ridingEntity.lastTickPosX = player.ridingEntity.prevPosX =  player.ridingEntity.posX = x;
    				if (!mc.vrSettings.simulateFalling)	{
    					player.ridingEntity.lastTickPosY = player.ridingEntity.prevPosY =  	 player.ridingEntity.posY = y;                	
    	    		}
    				player.ridingEntity.lastTickPosZ = player.ridingEntity.prevPosZ =  	 player.ridingEntity.posZ = z;
    		 }
    		 
    		player.boundingBox.setBounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY + player.height, bb.maxZ);
    		player.fallDistance = 0.0F;

    		torso = getEstimatedTorsoPosition(x, y, z);


    	}

    	//             test for climbing up a block
    	else if (mc.vrSettings.walkUpBlocks && player.fallDistance == 0)
    	{
    		if (torso == null)
    		{
    			torso = getEstimatedTorsoPosition(x, y, z);
    		}

    		// is the player significantly inside a block?
    		float climbShrink = player.width * 0.45f;
    		double shrunkClimbHalfWidth = playerHalfWidth - climbShrink;
    		AxisAlignedBB bbClimb = AxisAlignedBB.getBoundingBox(
    				torso.xCoord - shrunkClimbHalfWidth,
    				bb.minY,
    				torso.zCoord - shrunkClimbHalfWidth,
    				torso.xCoord + shrunkClimbHalfWidth,
    				bb.maxY,
    				torso.zCoord + shrunkClimbHalfWidth);

    		boolean notyet = mc.theWorld.getCollidingBoundingBoxes(player, bbClimb).isEmpty();

    		if(!notyet){
    			double xOffset = torso.xCoord - x;
    			double zOffset = torso.zCoord - z;
    			bb.minX += xOffset;
    			bb.maxX += xOffset;                	 
    			bb.minZ += zOffset;
    			bb.maxZ += zOffset;              	 
    			for (int i = 0; i <=10 ; i++)
    			{
    				bb.minY += 0.1f;
    				bb.maxY += 0.1f;

    				emptySpot = mc.theWorld.getCollidingBoundingBoxes(player, bb).isEmpty();
    				if (emptySpot)
    				{
    	    			x += xOffset;  	
    	    			z += zOffset;
    					y += 0.1f*i;
    					
    					player.lastTickPosX = player.prevPosX = player.posX = x;
    					player.lastTickPosY = player.prevPosY = player.posY = y;
    					player.lastTickPosZ = player.prevPosZ = player.posZ = z;
    					
    					player.boundingBox.setBounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);

    					roomOrigin.xCoord += xOffset;
    					roomOrigin.yCoord += 0.1f*i;
    					roomOrigin.zCoord += zOffset;

    					Vec3 look = player.getLookVec();
    					Vec3 forward = Vec3.createVectorHelper(look.xCoord,0,look.zCoord).normalize();
    					player.fallDistance = 0.0F;
    					playFootstepSound(mc,
    							player.posX + forward.xCoord * 0.4f,
    							player.posY-player.height,
    							player.posZ + forward.zCoord * 0.4f);
    					break;
    				}
    			}
    		}
    	}
    }
	
    public void playFootstepSound( Minecraft mc, double x, double y, double z )
    {
        Block block = mc.theWorld.getBlock(MathHelper.floor_double(x),
                MathHelper.floor_double(y - 0.5f),
                MathHelper.floor_double(z));

        if (block != null && block.getMaterial() != Material.air)
        {
            mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getStepSound()),
                    (block.stepSound.getVolume() + 1.0F) / 8.0F,
                    block.stepSound.getFrequency() * 0.5F,
                    (float) x, (float) y, (float) z));
        }
    }

    // use simple neck modeling to estimate torso location
    public Vec3 getEstimatedTorsoPosition(double x, double y, double z)
    {
        Entity player = Minecraft.getMinecraft().thePlayer;
        Vec3 look = player.getLookVec();
        Vec3 forward = Vec3.createVectorHelper(look.xCoord, 0, look.zCoord).normalize();
        float factor = (float)look.yCoord * 0.25f;
        Vec3 torso = Vec3.createVectorHelper(
                x + forward.xCoord * factor,
                y + forward.yCoord * factor,
                z + forward.zCoord * factor);

        return torso;
    }


    public void updateTeleportArc(Minecraft mc, Entity player)
    {
        Vec3 start = this.getControllerOffhandPos_World();
        Vec3 tiltedAim = mc.roomScale.getControllerOffhandDir_World();
        Matrix4f handRotation =MCOpenVR.getAimRotation(1);
        Matrix4f rot = Matrix4f.rotationY(this.worldRotationRadians);
        handRotation = Matrix4f.multiply(rot, handRotation);
        
        // extract hand roll
        Quatf handQuat = OpenVRUtil.convertMatrix4ftoRotationQuat(handRotation);
        EulerOrient euler = OpenVRUtil.getEulerAnglesDegYXZ(handQuat);
        
        int maxSteps = 50;
        movementTeleportArc[0].xCoord = start.xCoord;
        movementTeleportArc[0].yCoord = start.yCoord;
        movementTeleportArc[0].zCoord = start.zCoord;
        movementTeleportArcSteps = 1;

        // calculate gravity vector for arc
        float gravityAcceleration = 0.098f;
        Matrix4f rollCounter = OpenVRUtil.rotationZMatrix((float)MathHelper.deg2Rad*-euler.roll);
        Matrix4f gravityTilt = OpenVRUtil.rotationXMatrix((float)Math.PI * -.8f);
        Matrix4f gravityRotation = Matrix4f.multiply(handRotation, rollCounter);
        
        Vector3f forward = new Vector3f(0,1,0);
        Vector3f gravityDirection = gravityRotation.transform(forward);
        Vec3 gravity = Vec3.createVectorHelper(-gravityDirection.x, -gravityDirection.y, -gravityDirection.z);
        gravity.xCoord *= gravityAcceleration;
        gravity.yCoord *= gravityAcceleration;
        gravity.zCoord *= gravityAcceleration;
        
     //   gravity.rotateAroundY(this.worldRotationRadians);

        // calculate initial move step	
        float speed = 0.5f;
        Vec3 velocity = Vec3.createVectorHelper(
                tiltedAim.xCoord * speed,
                tiltedAim.yCoord * speed,
                tiltedAim.zCoord * speed);

        Vec3 pos = Vec3.createVectorHelper(start.xCoord, start.yCoord, start.zCoord);
        Vec3 newPos = Vec3.createVectorHelper(0,0,0);

        // trace arc
        for (int i=movementTeleportArcSteps;i<maxSteps;i++)
        {
        	if (i*4 > teleportEnergy) {
        		break;
        		}
        	
            newPos.xCoord = pos.xCoord + velocity.xCoord;
            newPos.yCoord = pos.yCoord + velocity.yCoord;
            newPos.zCoord = pos.zCoord + velocity.zCoord;

            MovingObjectPosition collision = mc.theWorld.rayTraceBlocks(pos, newPos, !mc.thePlayer.isInWater(), true, false);
			
            if (collision != null && collision.typeOfHit != MovingObjectPosition.MovingObjectType.MISS)
            {
                movementTeleportArc[i].xCoord = collision.hitVec.xCoord;
                movementTeleportArc[i].yCoord = collision.hitVec.yCoord;
                movementTeleportArc[i].zCoord = collision.hitVec.zCoord;
                movementTeleportArcSteps = i + 1;

                Vec3 traceDir = pos.subtract(newPos).normalize();
                Vec3 reverseEpsilon = Vec3.createVectorHelper(-traceDir.xCoord * 0.02, -traceDir.yCoord * 0.02, -traceDir.zCoord * 0.02);

                checkAndSetTeleportDestination(mc, player, start, collision, reverseEpsilon);
                          
                break;
            }

            pos.xCoord = newPos.xCoord;
            pos.yCoord = newPos.yCoord;
            pos.zCoord = newPos.zCoord;

            movementTeleportArc[i].xCoord = newPos.xCoord;
            movementTeleportArc[i].yCoord = newPos.yCoord;
            movementTeleportArc[i].zCoord = newPos.zCoord;
            movementTeleportArcSteps = i + 1;

            velocity.xCoord += gravity.xCoord;
            velocity.yCoord += gravity.yCoord;
            velocity.zCoord += gravity.zCoord;
        }
    }

    public void updateTeleportDestinations(EntityRenderer renderer, Minecraft mc, Entity player)
    {
        mc.mcProfiler.startSection("updateTeleportDestinations");

        // no teleporting if on a server that disallows teleporting
        if (getFreeMoveMode())
        {
            movementTeleportDestination.xCoord = 0.0;
            movementTeleportDestination.yCoord = 0.0;
            movementTeleportDestination.zCoord = 0.0;
            movementTeleportArcSteps = 0;
            return;
        }

        if (vrMovementStyle.arcAiming)
        {
            movementTeleportDestination.xCoord = 0.0;
            movementTeleportDestination.yCoord = 0.0;
            movementTeleportDestination.zCoord = 0.0;

            if (movementTeleportProgress>0.0f)
            {
                updateTeleportArc(mc, player);
            }
        }
        else //non-arc modes.
        {
            Vec3 start = this.getControllerOffhandPos_World();
            Vec3 aimDir = mc.roomScale.getControllerOffhandDir_World();

            // setup teleport forwards to the mouse cursor
            double movementTeleportDistance = 250.0;
            Vec3 movementTeleportPos = start.addVector(
                    aimDir.xCoord * movementTeleportDistance,
                    aimDir.yCoord * movementTeleportDistance,
                    aimDir.zCoord * movementTeleportDistance);
            MovingObjectPosition collision = mc.theWorld.rayTraceBlocks(start, movementTeleportPos, !mc.thePlayer.isInWater(), true, false);
            Vec3 traceDir = start.subtract(movementTeleportPos).normalize();
            Vec3 reverseEpsilon = Vec3.createVectorHelper(-traceDir.xCoord * 0.02, -traceDir.yCoord * 0.02, -traceDir.zCoord * 0.02);

            // don't update while charging up a teleport
            if (movementTeleportProgress != 0)
                return;

            if (collision != null && collision.typeOfHit != MovingObjectPosition.MovingObjectType.MISS)
            {
                checkAndSetTeleportDestination(mc, player, start, collision, reverseEpsilon);
            }
        }
        mc.mcProfiler.endSection();
    }

    // look for a valid place to stand on the block that the trace collided with
    private boolean checkAndSetTeleportDestination(Minecraft mc, Entity player, Vec3 start, MovingObjectPosition collision, Vec3 reverseEpsilon)
    {
        boolean bFoundValidSpot = false;

        
		if (collision.sideHit != 1) 
		{ //sides
		//jrbudda require arc hitting top of block.	unless ladder or vine.
			Block testClimb = player.worldObj.getBlock(collision.blockX, collision.blockY, collision.blockZ);
		//	System.out.println(testClimb.getUnlocalizedName() + " " + collision.typeOfHit + " " + collision.sideHit);
			   				   			   
			if ( testClimb == Blocks.ladder || testClimb == Blocks.vine) {
			            Vec3 dest = Vec3.createVectorHelper(collision.blockX+0.5, collision.blockY + 0.5, collision.blockZ+0.5);
	            		Block playerblock = player.worldObj.getBlock((int)player.posX, (int)player.boundingBox.minY -1, (int)player.posZ);
	            		if(playerblock == testClimb) dest.yCoord-=1;
                        movementTeleportDestination.xCoord = dest.xCoord;
                        movementTeleportDestination.yCoord = dest.yCoord;
                        movementTeleportDestination.zCoord = dest.zCoord;
                        movementTeleportDestinationSideHit = collision.sideHit;
						return true; //really should check if the block above is passable. Maybe later.
			} else {
					if (!mc.thePlayer.capabilities.allowFlying && mc.vrSettings.vrLimitedSurvivalTeleport) {return false;} //if creative, check if can hop on top.
			}
		}
		
        for ( int k = 0; k < 1 && !bFoundValidSpot; k++ )
        {
            Vec3 hitVec = collision.hitVec;// ( k == 1 ) ? collision.hitVec.addVector(-reverseEpsilon.xCoord, -reverseEpsilon.yCoord, -reverseEpsilon.zCoord)
                    						//: collision.hitVec.addVector(reverseEpsilon.xCoord, reverseEpsilon.yCoord, reverseEpsilon.zCoord);

            Vec3 debugPos = Vec3.createVectorHelper(
                    MathHelper.floor_double(hitVec.xCoord) + 0.5,
                    MathHelper.floor_double(hitVec.yCoord),
                    MathHelper.floor_double(hitVec.zCoord) + 0.5);

            int bx = collision.blockX;
            int bz = collision.blockZ;

            // search for a solid block with two empty blocks above it
            int startBlockY = collision.blockY -1 ; 
            startBlockY = Math.max(startBlockY, 0);
            for (int by = startBlockY; by < startBlockY + 2; by++)
            {
            	if (canStand(player.worldObj,bx, by, bz))
            	{
            		float maxTeleportDist = 16.0f;

            		float var27 = 0.0625F; //uhhhh?

            		double ox = hitVec.xCoord - player.posX;
            		double oy = by + 1 - player.posY;
            		double oz = hitVec.zCoord - player.posZ;
            		AxisAlignedBB bb = player.boundingBox.copy().contract((double)var27, (double)var27, (double)var27).offset(ox, oy, oz); 
            		bb.minY = by+1f;
            		bb.maxY = by+2.8f;
            		boolean emptySpotReq = mc.theWorld.getCollidingBoundingBoxes(player,bb).isEmpty();

            		double ox2 = bx + 0.5f - player.posX;
            		double oy2 = by + 1.0f - player.posY;
            		double oz2 = bz + 0.5f - player.posZ;
            		AxisAlignedBB bb2 = player.boundingBox.copy().contract((double)var27, (double)var27, (double)var27).offset(ox2, oy2, oz2);
            		bb2.minY = by+1f;
            		bb2.maxY = by+2.8f;
            		boolean emptySpotCenter = mc.theWorld.getCollidingBoundingBoxes(player,bb2).isEmpty();

            		List l = mc.theWorld.getCollidingBoundingBoxes(player,bb2);

            		Vec3 dest;

            		//teleport to exact spot unless collision, then teleport to center.

            		if (emptySpotReq) {           	
            			dest = Vec3.createVectorHelper(hitVec.xCoord, by+1,hitVec.zCoord);
            		}
            		else {
            			dest = Vec3.createVectorHelper(bx + 0.5f, by + 1f, bz + 0.5f);
            		}



            		if (start.distanceTo(dest) <= maxTeleportDist && (emptySpotReq || emptySpotCenter))
            		{

            			Block testClimb = player.worldObj.getBlock(bx, by, bz);
            			
            			double y = testClimb.getBlockBoundsMaxY();
            			if (testClimb == Blocks.farmland) y = 1f; //cheeky bastard
            			
            			movementTeleportDestination.xCoord = dest.xCoord;
            			movementTeleportDestination.yCoord = y + by;
            			movementTeleportDestination.zCoord = dest.zCoord;
            			movementTeleportDestinationSideHit = collision.sideHit;

            			debugPos.xCoord = bx + 0.5;
            			debugPos.yCoord = by + 1;
            			debugPos.zCoord = bz + 0.5;

            			bFoundValidSpot = true;

            			break;

            		}
            	}

            }
        }
        
        if(bFoundValidSpot) { movementTeleportDistance = start.distanceTo(movementTeleportDestination);}
        
        return bFoundValidSpot;
    }

    private boolean canStand(World w, int bx, int by, int bz){
    	
    	return w.getBlock(bx,  by,  bz).isCollidable() && w.getBlock(bx,  by+1,  bz).isPassable(w, bx, by+1, bz) &&  w.getBlock(bx,  by+2,  bz).isPassable(w, bx, by+2, bz);
 
    }
    
    // rough interpolation between arc locations
    public Vec3 getInterpolatedArcPosition(float progress)
    {
        // not enough points to interpolate or before start
        if (movementTeleportArcSteps == 1 || progress <= 0.0f)
        {
            return Vec3.createVectorHelper(
                    movementTeleportArc[0].xCoord,
                    movementTeleportArc[0].yCoord,
                    movementTeleportArc[0].zCoord);
        }

        // past end of arc
        if (progress>=1.0f)
        {
            return Vec3.createVectorHelper(
                    movementTeleportArc[movementTeleportArcSteps-1].xCoord,
                    movementTeleportArc[movementTeleportArcSteps-1].yCoord,
                    movementTeleportArc[movementTeleportArcSteps-1].zCoord);
        }

        // which two points are we between?
        float stepFloat = progress * (float)(movementTeleportArcSteps - 1);
        int step = (int) Math.floor(stepFloat);

        double deltaX = movementTeleportArc[step+1].xCoord - movementTeleportArc[step].xCoord;
        double deltaY = movementTeleportArc[step+1].yCoord - movementTeleportArc[step].yCoord;
        double deltaZ = movementTeleportArc[step+1].zCoord - movementTeleportArc[step].zCoord;

        float stepProgress = stepFloat - step;

        return Vec3.createVectorHelper(
                movementTeleportArc[step].xCoord + deltaX * stepProgress,
                movementTeleportArc[step].yCoord + deltaY * stepProgress,
                movementTeleportArc[step].zCoord + deltaZ * stepProgress);
    }

    private Vec3 lastWeaponEndAir = Vec3.createVectorHelper(0,0,0);
    private boolean lastWeaponSolid = false;

    public float weapongSwingLen;
	public Vec3 weaponEnd;
	public Vec3 weaponEndlast;
	public float tickDist;
    public float lastmot;
	
    public int disableSwing = 3;
    
    public void updateSwingAttack()
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;

        if (!mc.vrSettings.weaponCollision)
            return;

        mc.mcProfiler.startSection("updateSwingAttack");

        Vec3 handPos = this.getControllerMainPos_World();
        Vec3 handDirection = this.getControllerMainDir_World();
        
        float thismot =(float) (40* Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ));
        if(!player.onGround) //higher threshold when jumping and falling.
        	thismot =(float) (40* Math.sqrt(player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ));	
        
        float mot = Math.max(thismot, lastmot);
       
        lastmot = thismot;
        
        ItemStack is = player.inventory.getCurrentItem();
        Item item = null;

        double speedthresh = (float) (is==null ? 3.5f + mot: 4.2f + mot); //account for lower apparent speed due to shorter fulcrum.         
        float weaponLength = is == null ?  0 : 0.3f; //no reach for hand
        float entityReachAdd =0f;
      
        if(is!=null )item = is.getItem();
        
        if (item instanceof ItemSword){
        		entityReachAdd = 2.5f;
        		weaponLength = 0.3f;
               	speedthresh = 3f + mot;
        } else if (item instanceof ItemTool ||
        		item instanceof ItemHoe
        		){
        	entityReachAdd = 1.8f;
        	weaponLength = 0.3f;
        	speedthresh = 3f + mot;
        } else {
        	weaponLength = 0.0f;
        	entityReachAdd = 0.3f;
        	speedthresh = 1.5f + mot;
        }

        weaponLength *= this.worldScale;
        
        weapongSwingLen = weaponLength;
        weaponEnd = Vec3.createVectorHelper(
                handPos.xCoord + handDirection.xCoord * weaponLength,
                handPos.yCoord + handDirection.yCoord * weaponLength,
                handPos.zCoord + handDirection.zCoord * weaponLength);     
        
        if (disableSwing > 0 ) {
        	disableSwing--;
        	if(disableSwing<0)disableSwing = 0;
        	weaponEndlast = Vec3.createVectorHelper(weaponEnd.xCoord,	 weaponEnd.yCoord, 	 weaponEnd.zCoord);
        	lastmot = mot;
        	return;
        }
        
        tickDist = (float) (weaponEndlast.subtract(weaponEnd).lengthVector());
        
        float speed = (float) (tickDist * 20);
        
     	weaponEndlast = Vec3.createVectorHelper(weaponEnd.xCoord,	 weaponEnd.yCoord, 	 weaponEnd.zCoord);
        
        int passes = (int) (tickDist / .1f);
                 
        int bx = (int) MathHelper.floor_double(weaponEnd.xCoord);
        int by = (int) MathHelper.floor_double(weaponEnd.yCoord);
        int bz = (int) MathHelper.floor_double(weaponEnd.zCoord);

        boolean inAnEntity = false;
        boolean insolidBlock = false;
        boolean canact = speed > speedthresh && !lastWeaponSolid;
               
        Vec3 extWeapon = Vec3.createVectorHelper(
                handPos.xCoord + handDirection.xCoord * (weaponLength + entityReachAdd),
                handPos.yCoord + handDirection.yCoord * (weaponLength + entityReachAdd),
                handPos.zCoord + handDirection.zCoord * (weaponLength + entityReachAdd));
        
        	//Check EntityCollisions first
        	{

        		//experiment.
        		AxisAlignedBB weaponBB = AxisAlignedBB.getBoundingBox(
        				handPos.xCoord < extWeapon.xCoord ? handPos.xCoord : extWeapon.xCoord  ,
        						handPos.yCoord < extWeapon.yCoord ? handPos.yCoord : extWeapon.yCoord  ,
        								handPos.zCoord < extWeapon.zCoord ? handPos.zCoord : extWeapon.zCoord  ,
        										handPos.xCoord > extWeapon.xCoord ? handPos.xCoord : extWeapon.xCoord  ,
        												handPos.yCoord > extWeapon.yCoord ? handPos.yCoord : extWeapon.yCoord  ,
        														handPos.zCoord > extWeapon.zCoord ? handPos.zCoord : extWeapon.zCoord  
        				);

        		List entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
        				mc.renderViewEntity, weaponBB);
        		for (int e = 0; e < entities.size(); ++e)
        		{
        			Entity hitEntity = (Entity) entities.get(e);
        			if (hitEntity.canBeCollidedWith() && !(hitEntity == mc.renderViewEntity.ridingEntity))
        			{
        				if(canact){
        					mc.playerController.attackEntity(player, hitEntity);
        					this.triggerHapticPulse(0, 1000);
           					lastWeaponSolid = true;
        				}
         				inAnEntity = true;
         			}
        		}

        	if(!inAnEntity){
        		Block block = mc.theWorld.getBlock(bx, by, bz);
        		Material material = block.getMaterial();

        		// every time end of weapon enters a solid for the first time, trace from our previous air position
        		// and damage the block it collides with... 

        		MovingObjectPosition col = mc.theWorld.rayTraceBlocks(lastWeaponEndAir, weaponEnd, false, false, true);
        		if (col != null && col.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        		{
        			if (!(block.getMaterial() == material.air) && !block.getMaterial().isLiquid())
        			{
        				if(canact){
        					float hardness = block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.thePlayer.worldObj, col.blockX, col.blockY, col.blockZ);
        				//	System.out.println("hardness=" + hardness);
        					if (hardness * 4.0f > 1.0f)
        					{
        						mc.playerController.onPlayerDestroyBlock(col.blockX, col.blockY, col.blockZ, col.sideHit);
        					} else
        					{
        						//is this viable?
        						clearBlockHitDelay();
        						for (int i = 0; i < 4; i++)
        						{
        							mc.playerController.onPlayerDamageBlock(col.blockX, col.blockY, col.blockZ, col.sideHit);
        						}
        					}
             				this.triggerHapticPulse(0, 1000);
            			//   System.out.println("Hit block speed =" + speed + " mot " + mot + " thresh " + speedthresh) ;            				
            				lastWeaponSolid = true;
        				}
           				insolidBlock = true;
        			}
        		}
        	}
        }
        
        	
        	
        if (!inAnEntity && !insolidBlock)
        {
            lastWeaponEndAir.xCoord = weaponEnd.xCoord;
            lastWeaponEndAir.yCoord = weaponEnd.yCoord;
            lastWeaponEndAir.zCoord = weaponEnd.zCoord;
            lastWeaponSolid = false;
        }
        mc.mcProfiler.endSection();
    }
	
	public boolean getFreeMoveMode() { return freeMoveMode; }
	
	public void setFreeMoveMode(boolean free) { 
		boolean was = freeMoveMode;
		if(noTeleportClient){
			freeMoveMode = true;
		} else {
			freeMoveMode = free;
		}
		if(free != was)
			Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C17PacketCustomPayload("MC|Vive|FreeMove", (byte[]) (freeMoveMode ?  new byte[]{1} : new byte[]{0} )));	
	}

	public float getTeleportEnergy () {return teleportEnergy;}

	//================= IROOMSCALEADAPTER =============================
	
	
	float worldScale =  Minecraft.getMinecraft().vrSettings.vrWorldScale;
	float worldRotationRadians;
	
	@Override
	public boolean isHMDTracking() {
		return MCOpenVR.headIsTracking;
	}

	private Vec3 vecMult(Vec3 in, float factor){
		return Vec3.createVectorHelper(in.xCoord * factor,	in.yCoord * factor, in.zCoord*factor);
	}
	
	@Override
	public Vec3 getHMDPos_World() {	
		Vec3 out = vecMult(MCOpenVR.getCenterEyePosition(),worldScale);
		out.rotateAroundY(worldRotationRadians);
		return out.addVector(interPolatedRoomOrigin.xCoord, interPolatedRoomOrigin.yCoord, interPolatedRoomOrigin.zCoord);
	}

	@Override
	public Vec3 getHMDDir_World() {
		Vector3f v3 = MCOpenVR.headDirection;
		Vec3 out = Vec3.createVectorHelper(v3.x, v3.y, v3.z);
		out.rotateAroundY(worldRotationRadians);
		return out;
	}

	@Override
	public float getHMDYaw_World() {
		Vec3 dir = getHMDDir_World();
		 return (float)Math.toDegrees(Math.atan2(dir.xCoord, dir.zCoord));    
	}

	@Override
	public float getHMDPitch_World() {
		Vec3 dir = getHMDDir_World();
		return (float)Math.toDegrees(Math.asin(dir.yCoord/dir.lengthVector())); 
	}

	@Override
	public boolean isControllerMainTracking() {
		return MCOpenVR.controllerTracking[0];
	}

	@Override
	public Vec3 getControllerMainPos_World() {
		Vec3 out = vecMult(MCOpenVR.getAimSource(0),worldScale);
		out.rotateAroundY(worldRotationRadians);
		return out.addVector(interPolatedRoomOrigin.xCoord, interPolatedRoomOrigin.yCoord, interPolatedRoomOrigin.zCoord);
		}

	@Override
	public Vec3 getControllerMainDir_World() {
		Vector3f v3 = MCOpenVR.controllerDirection;
		Vec3 out = Vec3.createVectorHelper(v3.x, v3.y, v3.z);
		out.rotateAroundY(worldRotationRadians);
		return out;
	}

	@Override
	public float getControllerMainYaw_World() {
		return (float) (MCOpenVR.aimYaw + Minecraft.getMinecraft().vrSettings.vrWorldRotation);
	}

	@Override
	public float getControllerMainPitch_World() {
		return MCOpenVR.aimPitch;
	}

	@Override
	public boolean isControllerOffhandTracking() {
		return MCOpenVR.controllerTracking[1];
	}

	@Override
	public Vec3 getControllerOffhandPos_World() {
		Vec3 out = vecMult(MCOpenVR.getAimSource(1),worldScale);
		out.rotateAroundY(worldRotationRadians);
		return out.addVector(interPolatedRoomOrigin.xCoord, interPolatedRoomOrigin.yCoord, interPolatedRoomOrigin.zCoord);	}

	@Override
	public Vec3 getControllerOffhandDir_World() {
		Vector3f v3 = MCOpenVR.lcontrollerDirection;
		Vec3 out = Vec3.createVectorHelper(v3.x, v3.y, v3.z);
		out.rotateAroundY(worldRotationRadians);
		return out;
	}

	@Override
	public float getControllerOffhandYaw_World() {
		return  (float) (MCOpenVR.laimYaw + Minecraft.getMinecraft().vrSettings.vrWorldRotation);
	}

	@Override
	public float getControllerOffhandPitch_World() {
		return MCOpenVR.laimPitch;
	}

	@Override
	public Vec3 getRoomOriginPos_World() {
		return roomOrigin;
	}
	
	private Vec3 getInterpolatedRoomOriginPos_World(float nano) {
		Vec3 out = Vec3.createVectorHelper(0, 0, 0);
		out.xCoord = lastroomOrigin.xCoord + (roomOrigin.xCoord - lastroomOrigin.xCoord) * (double)nano;
		out.yCoord = lastroomOrigin.yCoord + (roomOrigin.yCoord - lastroomOrigin.yCoord) * (double)nano;
		out.zCoord = lastroomOrigin.zCoord + (roomOrigin.zCoord - lastroomOrigin.zCoord) * (double)nano;
		return out;
	}

	@Override
	public Vec3 getRoomOriginUpDir_World() { //ummmm
		return Vec3.createVectorHelper(0, 1, 0);
	}
	
	public EulerOrient getHMDEuler_World(){ //TOTO: important place to add user rotation.
		EulerOrient out = MCOpenVR.getOrientationEuler(EyeType.ovrEye_Center);
		out.yaw += Math.toDegrees(this.worldRotationRadians);
		return out;
	}
	

	@Override
	public void triggerHapticPulse(int controller, int strength) {
		MCOpenVR.triggerHapticPulse(controller, strength);
	}

	@Override
	public FloatBuffer getHMDMatrix_World() {
		Matrix4f out = MCOpenVR.hmdRotation;
		Matrix4f rot = Matrix4f.rotationY(worldRotationRadians);
		return Matrix4f.multiply(rot, out).toFloatBuffer();
	}
	
	@Override
	public Vec3 getEyePos_World(EyeType eye) {
		Vec3 out = vecMult(MCOpenVR.getEyePosition(eye),worldScale);
		out.rotateAroundY(worldRotationRadians);
		return out.addVector(interPolatedRoomOrigin.xCoord, interPolatedRoomOrigin.yCoord, interPolatedRoomOrigin.zCoord);
	}
	

	@Override
	public FloatBuffer getControllerMatrix_World(int controller) {
		Matrix4f out = MCOpenVR.getAimRotation(controller);
		Matrix4f rot = Matrix4f.rotationY(worldRotationRadians);
		return Matrix4f.multiply(rot,out).transposed().toFloatBuffer();
	}

	@Override
	public Vec3 getCustomControllerVector(int controller, Vec3 axis) {
		Vector3f v3 = MCOpenVR.getAimRotation(controller).transform(new Vector3f((float)axis.xCoord, (float)axis.yCoord,(float) axis.zCoord));
		Vec3 out =  Vec3.createVectorHelper(v3.x, v3.y, v3.z);
		out.rotateAroundY(worldRotationRadians);
		return out;
	}

	@Override
	public Vec3 getHMDPos_Room() {
		return vecMult(MCOpenVR.getCenterEyePosition(),worldScale);
	}

	@Override
	public Vec3 getControllerPos_Room(int i) {
		return vecMult(MCOpenVR.getAimSource(i),worldScale);
	}
	
	@Override
	public Vec3 getEyePos_Room(EyeType eye) {
		return vecMult(MCOpenVR.getEyePosition(eye),worldScale);
	}

	@Override
	public FloatBuffer getHMDMatrix_Room() {
		return MCOpenVR.hmdRotation.toFloatBuffer();
	}

	@Override
	public float getControllerYaw_Room(int controller) {
		if(controller == 0) return MCOpenVR.aimYaw;
		return MCOpenVR.laimYaw;
	}

	@Override
	public float getControllerPitch_Room(int controller) {
		if(controller == 0) return MCOpenVR.aimPitch;
		return MCOpenVR.laimPitch;
	}

	@Override
	public Vec3 getControllerPos_World(int c) {
		return c == 0 ? this.getControllerMainPos_World() : this.getControllerOffhandPos_World();
	}
	
	private void hackPCMP(){

		Class c = Minecraft.getMinecraft().playerController.getClass();
		try {
			//vanilla
			hitBlockDelay = c.getDeclaredField("i");
		} catch (NoSuchFieldException e) {
			try{
				//forge?
				hitBlockDelay = c.getDeclaredField("field_78781_i");
			} catch (NoSuchFieldException e1) {
				try {
					//deobf
					hitBlockDelay = c.getDeclaredField("blockHitDelay");
				} catch (NoSuchFieldException e3) {
					System.out.println("REPORT TO JRBUDDA " + e3.toString() + " " +c.getName());
					e.printStackTrace();
				}			
			}
		}
		catch (SecurityException e2) {

		}

		if(hitBlockDelay!=null){
			hitBlockDelay.setAccessible(true);
		}
	}

	
	
	Field hitBlockDelay;
    //JRBUDDA: This is the only thing this file does, necessary? Use reflection to modify field directly from elsewhere?
    // VIVE START - function to allow damaging blocks immediately
	public void clearBlockHitDelay() { 
	
		if(hitBlockDelay == null) {
			hackPCMP();
		}
		
		if(hitBlockDelay !=null){
			try {
				hitBlockDelay.setInt(Minecraft.getMinecraft().playerController, 0);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    // VIVE END - function to allow damaging blocks immediately
	
}

