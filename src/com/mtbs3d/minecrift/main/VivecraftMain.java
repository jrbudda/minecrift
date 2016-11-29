package com.mtbs3d.minecrift.main;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mtbs3d.minecrift.tweaker.MinecriftClassTransformer;
import com.mtbs3d.minecrift.tweaker.MinecriftClassTransformer.Stage;
import com.mtbs3d.minecrift.tweaker.MinecriftForgeClassTransformer;
import com.mtbs3d.minecrift.tweaker.MinecriftForgeLateClassTransformer;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.src.Reflector;

public class VivecraftMain
{
	private static final String smcName = "shadersmodcore.transform.SMCClassTransformer";
	private static final String[] encapsulatedTransformers = new String[]{
		smcName,
		"jp.mc.ancientred.starminer.core.SMTransformer", // lol this is so not going to work
	};
	private static final String[] removedTransformers = new String[]{
		"com.pau101.fairylights.core.FairyLightsClassTransformer", // Get out of our EntityRenderer
	};
	
	public static void main(String[] p_main_0_)
    {

		LaunchClassLoader load = (LaunchClassLoader) Thread.currentThread().getContextClassLoader();
		
		try {
			Field f = load.getClass().getDeclaredField("transformers");
			f.setAccessible(true);
			
			
			List<IClassTransformer> transformers = (List<IClassTransformer>) f.get(load);
			List<IClassTransformer> encapsulateObf = new ArrayList<IClassTransformer>();
			List<IClassTransformer> encapsulateDeobf = new ArrayList<IClassTransformer>();

			boolean passedDeobf = false;
			System.out.println("************** Vivecraft classloader pre-filter ***************");
			for (final Iterator it = transformers.iterator(); it.hasNext(); ) {
				IClassTransformer t = (IClassTransformer) it.next();

				System.out.println(t.getClass().getName());

				if (t.getClass().getName().equals("cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer")) {
					passedDeobf = true;
				}
				if (t.getClass().getName().equals("jp.mc.ancientred.starminer.core.SMTransformer")) {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("WARNING: STARMINER DETECTED - STUFF IS PROBABLY GOING TO EXPLODE HORRIBLY");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				}
				for (String dt : encapsulatedTransformers) {
				    if (t.getClass().getName().equals(dt)) {
				    	if (passedDeobf || t.getClass().getName().equals(smcName)) {
				    		encapsulateDeobf.add(t);
				    	} else {
				    		encapsulateObf.add(t);
				    	}
				    	it.remove();
				    	break;
				    }
				}
				for (String dt : removedTransformers) {
				    if (t.getClass().getName().equals(dt)) {
				    	it.remove();
				    	break;
				    }
				}
			}

			transformers.add(2,new MinecriftClassTransformer(Stage.main));
			int forgeObfIndex = 7;

			if (encapsulateObf.size() > 0) { //Dirty Harry Potter.
				transformers.add(forgeObfIndex, new MinecriftClassTransformer(Stage.cache));
				transformers.addAll(forgeObfIndex + 1, encapsulateObf);
				transformers.add(forgeObfIndex + encapsulateObf.size() + 1, new MinecriftClassTransformer(Stage.replace));
				forgeObfIndex += encapsulateObf.size() + 2;
			}
			if (encapsulateDeobf.size() > 0) { //Dirtier Harry Potter.
				transformers.add(transformers.size() - 1, new MinecriftClassTransformer(Stage.cache));
				transformers.addAll(transformers.size() - 1, encapsulateDeobf);
				transformers.add(transformers.size() - 1, new MinecriftClassTransformer(Stage.replace));
			}

			try {
				Class.forName("cpw.mods.fml.common.API"); // don't ask
				transformers.add(forgeObfIndex, new MinecriftForgeClassTransformer());
				transformers.add(transformers.size() - 1, new MinecriftForgeLateClassTransformer());
			} catch (ClassNotFoundException e) {}

	    	System.out.println("************** Vivecraft classloader filter ***************");
			for (final Iterator it = transformers.iterator(); it.hasNext(); ) {
				IClassTransformer t = (IClassTransformer) it.next();
				System.out.println(t.getClass().getName());
			}
			
		} catch (Exception e) {
			System.out.println("Vivecraft Filter Error");
			e.printStackTrace();
		}

		try {
        	
       	 final String launchTarget = "net.minecraft.client.main.Main";
         final Class<?> clazz = Class.forName(launchTarget, false, load);
         final Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});

		mainMethod.invoke(null, (Object) p_main_0_);
			
		} catch (Exception e) {
	    	System.out.println("************** Vivecraft critical error ***************");
			e.printStackTrace();
			System.exit(1);
		}
    	
    }

}
