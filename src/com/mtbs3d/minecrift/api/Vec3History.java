package com.mtbs3d.minecrift.api;

import java.awt.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;


public class Vec3History {

	private class entry{
		public long ts;
		public Vec3 data;
		public entry(Vec3 in){
			this.ts = Minecraft.getSystemTime();
			this.data = in;
		}
	}
	
	private int _capacity = 90*5;
	private LinkedList<entry> _data = new LinkedList<entry>();
	
	
	public void add(Vec3 in){
		_data.add(new entry(in));
		if (_data.size() > _capacity) _data.removeFirst();
	}
	
	public void clear(){
		_data.clear();
	}
	
	public Vec3 latest(){
		return _data.getLast().data;
	}
	

	/**
	 * Get the total integrated device translation for the specified time period. Return value is in meters.
	 */
	public double totalMovement(double seconds){
		long now = Minecraft.getSystemTime();
		ListIterator<entry> it = _data.listIterator(_data.size());
		entry last = null;
		double sum = 0;
		int count = 0;
		while (it.hasPrevious()){
			entry i = it.previous();
			count++;
			if(now - i.ts > seconds *1000)
				break;
			if (last == null){
				last = i;
				continue;
			}
			sum += (last.data.distanceTo(i.data));
		}
		return sum;
	}
	
	/**
	 * Get the vector representing the difference in position from now to @seconds ago.
	 */
	public Vec3 netMovement(double seconds){
		long now = Minecraft.getSystemTime();
		ListIterator<entry> it = _data.listIterator(_data.size());
		entry last = null;
		entry thing = null;
		double sum = 0;
		
		while (it.hasPrevious()){
			entry i = it.previous();
			if(now - i.ts > seconds *1000) break;
			if (last == null){
				last = i;
				continue;
			}
			thing = i;
		}
		if(last == null || thing == null) return Vec3.createVectorHelper(0, 0, 0);
		return thing.data.subtract(last.data);	
	}
	
	/**
	 * Get the average scalar speed of the device over the specified length of time. Returns m/s.
	 */
	public double averageSpeed(double seconds){
		long now = Minecraft.getSystemTime();
		ListIterator<entry> it = _data.listIterator(_data.size());
		double out = 0;
		entry last = null;
		int j = 0;
		while (it.hasPrevious()){
			entry i = it.previous();
			if(now - i.ts > seconds *1000) break;
			if (last == null){
				last = i;
				continue;
			}
			j++;
			double tdelta = (.001*(last.ts - i.ts));
			double ddelta = (last.data.subtract(i.data).lengthVector());
			out = out + ddelta/tdelta;
		}
		if(j == 0) return out;
		
		return out/j;
	}

	/**
	 * Get the average room position for the last @seconds.
	 */
	public Vec3 averagePosition(double seconds){
		long now = Minecraft.getSystemTime();
		ListIterator<entry> it = _data.listIterator(_data.size());
		Vec3 out = Vec3.createVectorHelper(0, 0, 0);
		int j = 0;
		while (it.hasPrevious()){
			entry i = it.previous();
			if(now - i.ts > seconds *1000) break;
			j++;
			out=out.addVector(i.data.xCoord, i.data.yCoord, i.data.zCoord);
		}
		if(j==0) return out;
		return Vec3.createVectorHelper(out.xCoord/j, out.yCoord/j, out.zCoord/j);
	}
	
}
