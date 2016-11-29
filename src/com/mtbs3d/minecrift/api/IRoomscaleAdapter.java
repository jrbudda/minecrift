package com.mtbs3d.minecrift.api;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Quaternion;

import com.mtbs3d.minecrift.render.QuaternionHelper;

import de.fruitfly.ovr.enums.EyeType;
import de.fruitfly.ovr.util.BufferUtil;
import net.minecraft.client.Minecraft.renderPass;
import net.minecraft.util.Vec3;

/**
 * This interface defines convenience methods for getting 'world coordinate' vectors from room-scale VR systems.
 *
 * @author jrbudda
 *
 */
public interface IRoomscaleAdapter  {

    public boolean isHMDTracking();
	public Vec3 getHMDPos_World();
	public Vec3 getHMDPos_Room(); 
	public Vec3 getHMDDir_World(); 
	public float getHMDYaw_World();  //degrees
	public float getHMDPitch_World(); //degrees
	
	public FloatBuffer getHMDMatrix_World();
	public FloatBuffer getHMDMatrix_Room();	
	public FloatBuffer getControllerMatrix_World(int controller);
	
	public Vec3 getEyePos_World(renderPass currentPass);
	public Vec3 getEyePos_Room(renderPass currentPass);
	
    public boolean isControllerMainTracking();
	public Vec3 getControllerMainDir_World(); 
	public float getControllerMainYaw_World(); //degrees
	public float getControllerMainPitch_World(); //degrees
	
	public float getControllerYaw_Room(int controller); //degrees
	public float getControllerPitch_Room(int controller); //degrees
	
	@Deprecated
	public Vec3 getControllerOffhandPos_World();
	@Deprecated
	public Vec3 getControllerMainPos_World(); 
	
    public boolean isControllerOffhandTracking();
	public Vec3 getControllerOffhandDir_World(); 
	public float getControllerOffhandYaw_World(); //degrees
	public float getControllerOffhandPitch_World(); //degrees

	public Vec3 getCustomHMDVector(Vec3 axis);
	public Vec3 getCustomControllerVector(int controller, Vec3 axis);
	
	public Vec3 getRoomOriginPos_World(); //degrees
	public Vec3 getRoomOriginUpDir_World(); //what do you do
	
	public void triggerHapticPulse(int controller, int duration);
	public Vec3 getControllerPos_Room(int i);
	public Vec3 getControllerPos_World(int c);
	public Vec3 getControllerDir_World(int c);
	public Vec3 getControllerDir_Room(int c);

}

