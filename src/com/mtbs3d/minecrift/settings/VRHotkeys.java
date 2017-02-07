/**
* Copyright 2013 Mark Browning, StellaArtois
* Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
*/
package com.mtbs3d.minecrift.settings;

import com.mtbs3d.minecrift.provider.MCOpenVR;
import com.mtbs3d.minecrift.provider.OpenVRPlayer;
import com.mtbs3d.minecrift.settings.VRSettings;
import com.mtbs3d.minecrift.utils.Quaternion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.util.Vec3;

import java.nio.FloatBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Matrix4f;

public class VRHotkeys {

	static long nextRead = 0;
	static final long COOLOFF_PERIOD_MILLIS = 500;

	public static boolean handleKeyboardInputs(Minecraft mc)
	{
		// Support cool-off period for key presses - otherwise keys can get spammed...
		if (nextRead != 0 && System.currentTimeMillis() < nextRead)
		return false;

		// Capture Minecrift key events
		boolean gotKey = false;

		// Debug aim
		if (Keyboard.getEventKey() == Keyboard.KEY_RSHIFT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.storeDebugAim = true;
			mc.printChatMessage("Show aim (RCTRL+RSHIFT): done");
			gotKey = true;
		}

		// Walk up blocks
		if (Keyboard.getEventKey() == Keyboard.KEY_B && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.walkUpBlocks = !mc.vrSettings.walkUpBlocks;
			mc.printChatMessage("Walk up blocks (RCTRL+B): " + (mc.vrSettings.walkUpBlocks ? "YES" : "NO"));
			gotKey = true;
		}

		// Player inertia
		if (Keyboard.getEventKey() == Keyboard.KEY_I && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
		{
			mc.vrSettings.inertiaFactor += 1;
			if (mc.vrSettings.inertiaFactor > VRSettings.INERTIA_MASSIVE)
			mc.vrSettings.inertiaFactor = VRSettings.INERTIA_NONE;
			switch (mc.vrSettings.inertiaFactor)
			{
			case VRSettings.INERTIA_NONE:
				mc.printChatMessage("Player player movement inertia (LCTRL-I): None");
				break;
			case VRSettings.INERTIA_NORMAL:
				mc.printChatMessage("Player player movement inertia (LCTRL-I): Normal");
				break;
			case VRSettings.INERTIA_LARGE:
				mc.printChatMessage("Player player movement inertia (LCTRL-I): Large");
				break;
			case VRSettings.INERTIA_MASSIVE:
				mc.printChatMessage("Player player movement inertia (LCTRL-I): Massive");
				break;
			}
			gotKey = true;
		}

		// Render full player model or just an disembodied hand...
		if (Keyboard.getEventKey() == Keyboard.KEY_H && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.renderFullFirstPersonModelMode++;
			if (mc.vrSettings.renderFullFirstPersonModelMode > VRSettings.RENDER_FIRST_PERSON_NONE)
			mc.vrSettings.renderFullFirstPersonModelMode = VRSettings.RENDER_FIRST_PERSON_FULL;

				switch (mc.vrSettings.renderFullFirstPersonModelMode)
			{
			case VRSettings.RENDER_FIRST_PERSON_FULL:
				mc.printChatMessage("First person model (RCTRL-H): Full");
				break;
			case VRSettings.RENDER_FIRST_PERSON_HAND:
				mc.printChatMessage("First person model (RCTRL-H): Hand");
				break;
			case VRSettings.RENDER_FIRST_PERSON_NONE:
				mc.printChatMessage("First person model (RCTRL-H): None");
				break;
			}
			gotKey = true;
		}
		// VIVE START - hotkeys

		// Testing different movement styles
		if (Keyboard.getEventKey() == Keyboard.KEY_M && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			// JRBUDDA ADDED all dis.
			if (mc.vrPlayer.getFreeMoveMode()) {
				//cycle restricted movement styles
				if (mc.vrPlayer.useLControllerForRestricedMovement) {
					mc.vrPlayer.useLControllerForRestricedMovement = false;
					mc.printChatMessage("Restricted movement mode set to gaze");
				} else {
					mc.vrPlayer.useLControllerForRestricedMovement = true;
					mc.printChatMessage("Restricted movement mode set to left controller");
				}
			} else {
				
				OpenVRPlayer vrp = mc.vrPlayer;
				
				// cycle VR movement styles
				if (vrp.vrMovementStyle.name == "Minimal") vrp.vrMovementStyle.setStyle("Beam");
				else if (vrp.vrMovementStyle.name == "Beam") vrp.vrMovementStyle.setStyle("Tunnel");
				else if (vrp.vrMovementStyle.name == "Tunnel") vrp.vrMovementStyle.setStyle("Grapple");
				else if (vrp.vrMovementStyle.name == "Grapple") vrp.vrMovementStyle.setStyle("Arc");
				else vrp.vrMovementStyle.setStyle("Minimal");
			
						
			}
					
			gotKey = true;
		}

		if (Keyboard.getEventKey() == Keyboard.KEY_R && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			// for testing restricted client mode
			
			if (mc.vrPlayer.getFreeMoveMode()) {
				mc.vrPlayer.setFreeMoveMode(false);
							mc.printChatMessage("Restricted movement disabled (teleporting allowed)");
				} else {
				mc.vrPlayer.setFreeMoveMode(true);
				mc.printChatMessage("Restricted movement enabled (no teleporting)");
			}
			
			gotKey = true;
		}
		
		
		if (Keyboard.getEventKey() == Keyboard.KEY_LEFT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.vrFixedCamposX -= 0.01;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT&& Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.vrFixedCamposX += 0.01;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_UP&& Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.vrFixedCamposZ -= 0.01;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_DOWN && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.vrFixedCamposZ += 0.01;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_PRIOR && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.vrFixedCamposY += 0.01;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_NEXT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.vrSettings.vrFixedCamposY -= 0.01;
			gotKey = true;
		}
		
		if (Keyboard.getEventKey() == Keyboard.KEY_UP && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			mc.vrSettings.vrFixedCamrotPitch -= 0.5;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_DOWN && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			mc.vrSettings.vrFixedCamrotPitch += 0.5;	
			gotKey = true;
		
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_LEFT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			mc.vrSettings.vrFixedCamrotYaw -= 0.5;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			mc.vrSettings.vrFixedCamrotYaw += 0.5;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_PRIOR && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			mc.vrSettings.vrFixedCamrotRoll -= 0.05;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_NEXT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			mc.vrSettings.vrFixedCamrotRoll += 0.05;	
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_INSERT && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			mc.gameSettings.fovSetting +=1 ;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_DELETE && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))

		{
			mc.gameSettings.fovSetting -=1 ;
			gotKey = true;
		}
		if (Keyboard.getEventKey() == Keyboard.KEY_HOME && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		{
			snapMRCam(mc,0);
			gotKey = true;
		}

		// VIVE END - hotkeys

		if (gotKey) {
			mc.vrSettings.saveOptions();
		}

		return gotKey;
	}

	public static void snapMRCam(Minecraft mc, int controller) {
		mc.vrSettings.vrFixedCamrotPitch = -Minecraft.getMinecraft().roomScale.getControllerPitch_World(controller) + mc.vrSettings.mrMovingCamOffsetPitch;
		mc.vrSettings.vrFixedCamrotYaw = Minecraft.getMinecraft().roomScale.getControllerYaw_World(controller) + mc.vrSettings.vrWorldRotation + mc.vrSettings.mrMovingCamOffsetYaw;
		mc.vrSettings.vrFixedCamrotRoll = Minecraft.getMinecraft().roomScale.getControllerRoll_World(controller) + mc.vrSettings.mrMovingCamOffsetRoll;

		org.lwjgl.util.vector.Vector4f vec = new org.lwjgl.util.vector.Vector4f(mc.vrSettings.mrMovingCamOffsetX, mc.vrSettings.mrMovingCamOffsetY, mc.vrSettings.mrMovingCamOffsetZ, 1);
		org.lwjgl.util.vector.Matrix4f matrix = new org.lwjgl.util.vector.Matrix4f();
		matrix.rotate((float)Math.toRadians(mc.vrSettings.vrFixedCamrotRoll), new org.lwjgl.util.vector.Vector3f(0, 0, 1));
		matrix.rotate((float)Math.toRadians(mc.vrSettings.vrFixedCamrotPitch), new org.lwjgl.util.vector.Vector3f(-1, 0, 0));
		matrix.rotate((float)Math.toRadians(mc.vrSettings.vrFixedCamrotYaw), new org.lwjgl.util.vector.Vector3f(0, -1, 0));
		org.lwjgl.util.vector.Matrix4f.transform(matrix, vec, vec);

		Vec3 pos = Minecraft.getMinecraft().roomScale.getControllerPos_Room(controller);
		mc.vrSettings.vrFixedCamposX = ((float)pos.xCoord + vec.x) / mc.vrPlayer.worldScale;
		mc.vrSettings.vrFixedCamposY = ((float)pos.yCoord + vec.y) / mc.vrPlayer.worldScale;
		mc.vrSettings.vrFixedCamposZ = ((float)pos.zCoord + vec.z) / mc.vrPlayer.worldScale;
		
		MCOpenVR.mrControllerPos = Minecraft.getMinecraft().roomScale.getControllerPos_Room(controller);
		MCOpenVR.mrControllerPitch = -Minecraft.getMinecraft().roomScale.getControllerPitch_World(controller);
		MCOpenVR.mrControllerYaw = Minecraft.getMinecraft().roomScale.getControllerYaw_World(controller) + mc.vrSettings.vrWorldRotation;
		MCOpenVR.mrControllerRoll = Minecraft.getMinecraft().roomScale.getControllerRoll_World(controller);
	}
}
