package com.mtbs3d.minecrift.api;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.util.vector.Matrix4f;

import com.google.common.base.Charsets;
import com.mtbs3d.minecrift.utils.Quaternion;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.Vec3;

public class NetworkHelper {

	public enum PacketDiscriminators {
		VERSION,
		REQUESTDATA,
		HEADDATA,
		CONTROLLER0DATA,
		CONTROLLER1DATA,
		WORLDSCALE,
		DRAW,
		MOVEMODE,
		UBERPACKET
	}

	private final static String channel = "Vivecraft";
	
	public static C17PacketCustomPayload getVivecraftClientPacket(PacketDiscriminators command, byte[] payload)
	{
		payload = ArrayUtils.add(payload,0,(byte)command.ordinal());
        return (new C17PacketCustomPayload(channel, payload));
	}
	
	public static S3FPacketCustomPayload getVivecraftServerPacket(PacketDiscriminators command, byte[] payload)
	{
		payload = ArrayUtils.add(payload,0,(byte)command.ordinal());
        return (new S3FPacketCustomPayload(channel, payload));
	}
	
	public static boolean serverWantsData = false;
	
	private static float worldScallast = 0;
	public static void sendVRPlayerPositions(IRoomscaleAdapter player) {
		if(!serverWantsData) return;
		float worldScale = Minecraft.getMinecraft().vrPlayer.worldScale;
		if (worldScale != worldScallast) {
			ByteBuf payload = Unpooled.buffer();
			payload.writeFloat(worldScale);
			byte[] out = new byte[payload.readableBytes()];
			payload.readBytes(out);
			C17PacketCustomPayload pack = getVivecraftClientPacket(PacketDiscriminators.WORLDSCALE,out);
			Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(pack);
			
			worldScallast = worldScale;
		}
		
		{
			FloatBuffer buffer = player.getHMDMatrix_World();
			buffer.rewind();
			Matrix4f matrix = new Matrix4f();
			matrix.load(buffer);
			matrix.transpose();

			Vec3 headPosition = player.getHMDPos_World();
			Quaternion headRotation = new Quaternion(matrix);
			
			ByteBuf payload = Unpooled.buffer();
			payload.writeFloat((float)headPosition.xCoord);
			payload.writeFloat((float)headPosition.yCoord);
			payload.writeFloat((float)headPosition.zCoord);
			payload.writeFloat((float)headRotation.w);
			payload.writeFloat((float)headRotation.x);
			payload.writeFloat((float)headRotation.y);
			payload.writeFloat((float)headRotation.z);
			byte[] out = new byte[payload.readableBytes()];
			payload.readBytes(out);
			C17PacketCustomPayload pack = getVivecraftClientPacket(PacketDiscriminators.HEADDATA,out);
			Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(pack);
		}	
		
		for (int i = 0; i < 2; i++) {
			Vec3 controllerPosition = player.getControllerPos_World(i);
			FloatBuffer buffer = player.getControllerMatrix_World(i);
			buffer.rewind();
			Matrix4f matrix = new Matrix4f();
			matrix.load(buffer);
			Quaternion controllerRotation = new Quaternion(matrix);
		
			ByteBuf payload = Unpooled.buffer();
			payload.writeBoolean(Minecraft.getMinecraft().vrSettings.vrReverseHands);
			payload.writeFloat((float)controllerPosition.xCoord);
			payload.writeFloat((float)controllerPosition.yCoord);
			payload.writeFloat((float)controllerPosition.zCoord);
			payload.writeFloat((float)controllerRotation.w);
			payload.writeFloat((float)controllerRotation.x);
			payload.writeFloat((float)controllerRotation.y);
			payload.writeFloat((float)controllerRotation.z);
			byte[] out = new byte[payload.readableBytes()];
			payload.readBytes(out);
			C17PacketCustomPayload pack  = getVivecraftClientPacket(i == 0? PacketDiscriminators.CONTROLLER0DATA : PacketDiscriminators.CONTROLLER1DATA,out);
			Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(pack);
		}
		
	}
	
}
