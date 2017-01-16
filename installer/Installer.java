import org.json.JSONObject;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Derived from https://github.com/MinecraftForge/Installer/
 * Copyright 2013 MinecraftForge developers, & Mark Browning, StellaArtois
 *
 * Licensed under GNU LGPL v2.1 or later.
 *
 * @author mabrowning
 *
 */
public class Installer extends JPanel  implements PropertyChangeListener
{
    private static final long serialVersionUID = -562178983462626162L;
    private String tempDir = System.getProperty("java.io.tmpdir");

    private static final boolean ALLOW_FORGE_INSTALL = true;  // VIVE: disabled, forge install isn't working currently
    private static final boolean ALLOW_HYDRA_INSTALL = false;  // TODO: Change to true once Hydra is fixed up
	private static final boolean ALLOW_SHADERSMOD_INSTALL = true;  

    private static final boolean NEEDS_2010_REDIST = false;
    private static final boolean NEEDS_2012_REDIST = false;

    // Currently needed for Win boxes - C++ redists

    public static String winredist2012_64url = "http://download.microsoft.com/download/1/6/B/16B06F60-3B20-4FF2-B699-5E9B7962F9AE/VSU_4/vcredist_x64.exe";
    public static String winredist2012_32url = "http://download.microsoft.com/download/1/6/B/16B06F60-3B20-4FF2-B699-5E9B7962F9AE/VSU_4/vcredist_x86.exe";
    public static String winredist2010_64url = "http://download.microsoft.com/download/A/8/0/A80747C3-41BD-45DF-B505-E9710D2744E0/vcredist_x64.exe";
    public static String winredist2010_32url = "http://download.microsoft.com/download/C/6/D/C6D0FD4E-9E53-4897-9B91-836EBA2AACD3/vcredist_x86.exe";

    /* DO NOT RENAME THESE STRING CONSTS - THEY ARE USED IN (AND THE VALUES UPDATED BY) THE AUTOMATED BUILD SCRIPTS */
    private static final String MINECRAFT_VERSION = "1.7.10";
    private static final String MC_VERSION        = "1.7.10";
    private static final String MC_MD5            = "e6b7a531b95d0c172acb704d1f54d1b3";
    private static final String OF_LIB_PATH       = "libraries/optifine/OptiFine/";
    private static final String OF_FILE_NAME      = "1.7.10_HD_U_D1";
    private static final String OF_JSON_NAME      = "1.7.10_HD_U_D1";
    private static final String OF_MD5            = "57c724fe8335c82aef8d54c101043e60";
    private static final String OF_VERSION_EXT    = ".jar";
    private static final String FORGE_VERSION     = "10.13.4.1614";
    /* END OF DO NOT RENAME */

    private String mc_url = "https://s3.amazonaws.com/Minecraft.Download/versions/" + MINECRAFT_VERSION + "/" + MINECRAFT_VERSION +".jar";

    private InstallTask task;
    private static ProgressMonitor monitor;
    static private File targetDir;
    private String[] forgeVersions = null;
    private boolean forgeVersionInstalled = false;
    private static final String FULL_FORGE_VERSION = MINECRAFT_VERSION + "-" + FORGE_VERSION + "-" + MINECRAFT_VERSION;
    private String forge_url = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + FULL_FORGE_VERSION + "/forge-" + FULL_FORGE_VERSION + "-installer.jar";
    private File forgeInstaller = new File(tempDir + "/" + FULL_FORGE_VERSION + ".jar");
    private JTextField selectedDirText;
    private JLabel infoLabel;
    private JDialog dialog;
    private JPanel fileEntryPanel;
    private Frame emptyFrame;
    private String jar_id;
    private String version;
    private String mod = "";
    private JCheckBox useForge;
	private JCheckBox useShadersMod;
	private ButtonGroup bg = new ButtonGroup();
    private JCheckBox createProfile;
    private JComboBox forgeVersion;
    private JCheckBox useHydra;
    private JCheckBox useHrtf;
    private final boolean QUIET_DEV = false;
	private File releaseNotes = null;
    private static String releaseNotePathAddition = "";
    private static JLabel instructions;
	private String smcVanillaURL = "http://www.karyonix.net/shadersmod/files/ShadersMod-v2.3.29mc1.7.10-installer.jar";
	private String smcForgeURL = "http://www.karyonix.net/shadersmod/files/ShadersModCore-v2.3.31-mc1.7.10-f.jar";	
	private  final String smcVanillaLib  = "libraries/shadersmodcore/ShadersModCore/2.3.29mc1.7.10";
    private  final String smcForgelib   = "libraries/shadersmodcore/ShadersModCore/2.3.31mc1.7.10-f";
	private  final String smcVanillaFile  = "ShadersModCore-2.3.29mc1.7.10.jar";
    private  final String smcForgeFile   = "ShadersModCore-2.3.31mc1.7.10-f.jar";
	private  final String smcVanillaMD5  = "4797D91A1F3752EF47242637901199CB";
    private  final String smcForgeMD5   = "F66374AEA8DDA5F3B7CCB20C230375D7";
	
    
    static private final String forgeNotFound = "Forge not found..." ;

    private String userHomeDir;
    private String osType;
    private boolean isWindows = false;
    private String appDataDir;

    class InstallTask extends SwingWorker<Void, Void>{
    	
        private boolean DownloadOptiFine()
        {
            boolean success = true;
            boolean deleted = false;

            try {
                File fod = new File(targetDir,OF_LIB_PATH+OF_JSON_NAME);
                fod.mkdirs();
                File fo = new File(fod,"OptiFine-"+OF_JSON_NAME+".jar");

                // Attempt to get the Optifine MD5
                String optOnDiskMd5 = GetMd5(fo);
                System.out.println(optOnDiskMd5 == null ? fo.getCanonicalPath() : fo.getCanonicalPath() + " MD5: " + optOnDiskMd5);

                // Test MD5
                if (optOnDiskMd5 == null)
                {
                    // Just continue...
                    System.out.println("Optifine not found - downloading");
                }
                else if (!optOnDiskMd5.equalsIgnoreCase(OF_MD5)) {
                    // Bad copy. Attempt delete just to make sure.
                    System.out.println("Optifine MD5 bad - downloading");

                    try {
                        deleted = fo.delete();
                    }
                    catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
                }
                else {
                    // A good copy!
                    System.out.println("Optifine MD5 good! " + OF_MD5);
                    return true;
                }

                // Need to attempt download...
                success = downloadFile("http://optifine.net/download.php?f=OptiFine_" + OF_FILE_NAME + OF_VERSION_EXT, fo);

                // Check (potentially) downloaded optifine md5
                optOnDiskMd5 = GetMd5(fo);
                if (success == false || optOnDiskMd5 == null || !optOnDiskMd5.equalsIgnoreCase(OF_MD5)) {
                    // No good
                    if (optOnDiskMd5 != null)
                        System.out.println("Optifine - bad MD5. Got " + optOnDiskMd5 + ", expected " + OF_MD5);
                    try {
                        deleted = fo.delete();
                    }
                    catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
                    return false;
                }

                return true;
            } catch (Exception e) {
                finalMessage += " Error: "+e.getLocalizedMessage();
            }
            return false;
        }

		  private boolean downloadSMC(boolean forge)
        {
			  String dir = null;
			  String file = null;
			  String url = null;
			  String goodmd5 = null;
			  String temp = "temp.jar";
			  if (forge) {
				  dir = smcForgelib;
				  file = smcForgeFile;
				  url = smcForgeURL;
				  goodmd5 = smcForgeMD5;
			  } else {
				  dir = smcVanillaLib;
				  file = smcVanillaFile;
				  url = smcVanillaURL; 
				  goodmd5 = smcVanillaMD5;
			  }
	  
            boolean success = true;
            boolean deleted = false;

            try {
                File fod = new File(targetDir,dir);
                fod.mkdirs();
                File fo = new File(fod,file);

                // Attempt to get the Optifine MD5
                String md5 = GetMd5(fo);
                System.out.println(md5 == null ? fo.getCanonicalPath() : fo.getCanonicalPath() + " MD5: " + md5);

                // Test MD5
                if (md5 == null)
                {
                    // Just continue...
                    System.out.println("ShadersMod not found - downloading");
                }
                else if (!md5.equalsIgnoreCase(goodmd5)) {
                    // Bad copy. Attempt delete just to make sure.
                    System.out.println("ShadersMod MD5 bad - downloading");

                    try {
                        deleted = fo.delete();
                    }
                    catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
                }
                else {
                    // A good copy!
                    System.out.println("ShadersMod MD5 good! " + md5);
                    return true;
                }

                // Need to attempt download...
                
                if(forge) {
                    success = downloadFile(url, fo);
                	
                }else {
                	 File t = new File(fod,temp);
                	 if( downloadFile(url, t)){
                		 
                		 ZipInputStream temp_jar = new ZipInputStream(new FileInputStream(t));
                		 
                		   ZipEntry ze = null;
                           byte data[] = new byte[1024];
                           while ((ze = temp_jar.getNextEntry()) != null) {
                               if(ze.getName().equals(file)) //extract the core jar.

                               {
                            	   FileOutputStream output = new FileOutputStream(fo);
                                   try
                                   {
                                	   byte[] buffer = new byte[2048];
                                       int len = 0;
                                       while ((len = temp_jar.read(buffer)) > 0)
                                       {
                                           output.write(buffer, 0, len);
                                       }
                                   }
                                   finally
                                   {
                                       if(output!=null) output.close();
                                   }
                               }
                           }
                           temp_jar.close();
                           t.delete();
                           return true;                		 
                	 } else {
						return false;
					 }
            
                }   
                
                //Check (potentially) downloaded shadersmodcore md5
                md5 = GetMd5(fo);
                if (success == false || md5 == null || !md5.equalsIgnoreCase(goodmd5)) {
                    // No good
                    if (md5 != null)
                        System.out.println("ShadersMod - bad MD5. Got " + md5 + ", expected " + goodmd5);
                    try {
                        deleted = fo.delete();
                    }
                    catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
                    return false;
                }

                return true;
            } catch (Exception e) {
                finalMessage += " Error: "+e.getLocalizedMessage();
            }
            return false;
        }
		
        private boolean downloadFile(String surl, File fo)
        {
            return downloadFile(surl, fo, null);
        }

        private boolean downloadFile(String surl, File fo, String md5)
        {
            boolean success = true;

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fo);
                System.out.println(surl);
                URL url = new URL(surl);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                long bytes = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.flush();
            }
            catch(Exception ex) {
                ex.printStackTrace();
                success = false;
            }
            finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) { }
                }
            }
            if (success) {
                if (!checkMD5(fo, md5)){
                    JOptionPane.showMessageDialog(null, "Bad md5 for " + fo.getName() + "!" + " actual: " + GetMd5(fo).toLowerCase(),"Error downloading", JOptionPane.ERROR_MESSAGE);
                    fo.delete();
                    success = false;
                }
            }

            return success;
        }

		private boolean checkMD5(File a, String b){
			if (a.exists() == false) return false;
			if(b == null) return true;
			return GetMd5(a).equalsIgnoreCase(b);
		}
		
        private String GetMd5(File fo)
        {
            if (!fo.exists())
                return null;

            if (fo.length() < 1)
                return null;

            FileInputStream fis = null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                fis = new FileInputStream(fo);

                byte[] buffer = new byte[(int)fo.length()];
                int numOfBytesRead = 0;
                while( (numOfBytesRead = fis.read(buffer)) > 0)
                {
                    md.update(buffer, 0, numOfBytesRead);
                }
                byte[] hash = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02X", b));
                }
                return sb.toString();
            }
            catch (Exception ex)
            {
                return null;
            }
            finally {
                if (fis != null)
                {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Shamelessly ripped from Forge ClientInstall
        private boolean installForge(File target)
        {
/*            File versionRootDir = new File(target,"versions");
            File versionTarget = new File(versionRootDir,MINECRAFT_VERSION);
            if (!versionTarget.mkdirs() && !versionTarget.isDirectory())
            {
                versionTarget.delete();
                versionTarget.mkdirs();
            }

            File librariesDir = new File(target, "libraries");
            List<JsonNode> libraries = VersionInfo.getVersionInfo().getArrayNode("libraries");
            monitor.setMaximum(libraries.size() + 3);
            int progress = 3;

            File versionJsonFile = new File(versionTarget,VersionInfo.getVersionTarget()+".json");

            if (!VersionInfo.isInheritedJson())
            {
                File clientJarFile = new File(versionTarget, VersionInfo.getVersionTarget()+".jar");
                File minecraftJarFile = VersionInfo.getMinecraftFile(versionRootDir);

                try
                {
                    boolean delete = false;
                    monitor.setNote("Considering minecraft client jar");
                    monitor.setProgress(1);

                    if (!minecraftJarFile.exists())
                    {
                        minecraftJarFile = File.createTempFile("minecraft_client", ".jar");
                        delete = true;
                        monitor.setNote(String.format("Downloading minecraft client version %s", VersionInfo.getMinecraftVersion()));
                        String clientUrl = String.format(DownloadUtils.VERSION_URL_CLIENT.replace("{MCVER}", VersionInfo.getMinecraftVersion()));
                        System.out.println("  Temp File: " + minecraftJarFile.getAbsolutePath());

                        if (!DownloadUtils.downloadFileEtag("minecraft server", minecraftJarFile, clientUrl))
                        {
                            minecraftJarFile.delete();
                            JOptionPane.showMessageDialog(null, "Downloading minecraft failed, invalid e-tag checksum.\n" +
                                            "Try again, or use the official launcher to run Minecraft " +
                                            VersionInfo.getMinecraftVersion() + " first.",
                                    "Error downloading", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        monitor.setProgress(2);
                    }

                    if (VersionInfo.getStripMetaInf())
                    {
                        monitor.setNote("Copying and filtering minecraft client jar");
                        copyAndStrip(minecraftJarFile, clientJarFile);
                        monitor.setProgress(3);
                    }
                    else
                    {
                        monitor.setNote("Copying minecraft client jar");
                        Files.copy(minecraftJarFile, clientJarFile);
                        monitor.setProgress(3);
                    }

                    if (delete)
                    {
                        minecraftJarFile.delete();
                    }
                }
                catch (IOException e1)
                {
                    JOptionPane.showMessageDialog(null, "You need to run the version "+VersionInfo.getMinecraftVersion()+" manually at least once", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            File targetLibraryFile = VersionInfo.getLibraryPath(librariesDir);
            grabbed = Lists.newArrayList();
            List<Artifact> bad = Lists.newArrayList();
            progress = DownloadUtils.downloadInstalledLibraries("clientreq", librariesDir, monitor, libraries, progress, grabbed, bad);

            monitor.close();
            if (bad.size() > 0)
            {
                String list = Joiner.on("\n").join(bad);
                JOptionPane.showMessageDialog(null, "These libraries failed to download. Try again.\n"+list, "Error downloading", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (!targetLibraryFile.getParentFile().mkdirs() && !targetLibraryFile.getParentFile().isDirectory())
            {
                if (!targetLibraryFile.getParentFile().delete())
                {
                    JOptionPane.showMessageDialog(null, "There was a problem with the launcher version data. You will need to clear "+targetLibraryFile.getAbsolutePath()+" manually", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else
                {
                    targetLibraryFile.getParentFile().mkdirs();
                }
            }


            JsonRootNode versionJson = JsonNodeFactories.object(VersionInfo.getVersionInfo().getFields());

            try
            {
                BufferedWriter newWriter = Files.newWriter(versionJsonFile, Charsets.UTF_8);
                PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson,newWriter);
                newWriter.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try
            {
                VersionInfo.extractFile(targetLibraryFile);
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(null, "There was a problem writing the system library file", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JdomParser parser = new JdomParser();
            JsonRootNode jsonProfileData;

            try
            {
                jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
            }
            catch (InvalidSyntaxException e)
            {
                JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            catch (Exception e)
            {
                throw Throwables.propagate(e);
            }




            HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
            HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
            if(profileCopy.containsKey(JsonNodeFactories.string(VersionInfo.getProfileName())))
            {
                HashMap<JsonStringNode, JsonNode> forgeProfileCopy = Maps.newHashMap(profileCopy.get(JsonNodeFactories.string(VersionInfo.getProfileName())).getFields());
                forgeProfileCopy.put(JsonNodeFactories.string("name"), JsonNodeFactories.string(VersionInfo.getProfileName()));
                forgeProfileCopy.put(JsonNodeFactories.string("lastVersionId"), JsonNodeFactories.string(VersionInfo.getVersionTarget()));
            }
            else
            {
                JsonField[] fields = new JsonField[] {
                        JsonNodeFactories.field("name", JsonNodeFactories.string(VersionInfo.getProfileName())),
                        JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(VersionInfo.getVersionTarget())),
                };
                profileCopy.put(JsonNodeFactories.string(VersionInfo.getProfileName()), JsonNodeFactories.object(fields));
            }
            JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);
            rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);

            jsonProfileData = JsonNodeFactories.object(rootCopy);

            try
            {
                BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
                PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData,newWriter);
                newWriter.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
 */
            return true;


//            return success;
        }

        private boolean SetupMinecraftAsLibrary() {
		String s = "";
            try {
				File mc_jar = null;
				String minecriftVersionName = "vivecraft-" + version + mod;
				s+=		minecriftVersionName;
				File tar = new File(targetDir, "versions" + File.separator + minecriftVersionName + File.separator +  minecriftVersionName + ".jar");
				s+= " " + MC_MD5 + " " + GetMd5(tar);
				if(checkMD5(tar, MC_MD5)) return true;
				
				if(mc_jar == null){
					File vanilla = new File(targetDir, "versions" + File.separator + MINECRAFT_VERSION + File.separator + MINECRAFT_VERSION+".jar");
					s+= " " + MC_MD5 + " " + GetMd5(vanilla);
					if(checkMD5(vanilla, MC_MD5)) mc_jar = vanilla;
				}


				if(mc_jar == null){
					// Download the minecraft jar (we don't wont to require that it has previously been downloaded in Minecraft)
					mc_jar = new File(tempDir + File.separator + MINECRAFT_VERSION + ".jar");
					if (!mc_jar.exists() || !checkMD5(mc_jar, MC_MD5)) {

						if (!downloadFile(mc_url, mc_jar, MC_MD5)) {
							finalMessage += " Error: Failed to download " + MINECRAFT_VERSION + ".jar from " + mc_url;
							return false;
						}
					}
				}
					
				if(mc_jar == null) return false;

			InputStream src = new FileInputStream(mc_jar);
			tar.getParentFile().mkdirs();
			return copyInputStreamToFile(src, tar);

            } catch (Exception e) {
                finalMessage += " Error: "+e.getLocalizedMessage();
				return false;
            }



        }

        private boolean ExtractVersion() {
            if( jar_id != null )
            {
                InputStream version_json;
                if(useForge.isSelected() /*&& forgeVersion.getSelectedItem() != forgeNotFound*/ ) 
                	{
                    String filename;
					
					  if(!useShadersMod.isSelected()){
						filename = "version-forge.json";
						mod="-forge";
					  }
					 else{
						filename = "version-forge-shadersmod.json";
						mod="-forge-shadersmod";
					 }
                        version_json = new FilterInputStream( Installer.class.getResourceAsStream(filename) ) {
                        public int read(byte[] buff) throws IOException {
                            int ret = in.read(buff);
                            if( ret > 0 ) {
                                String s = new String( buff,0, ret, "UTF-8");
                                //s = s.replace("$FORGE_VERSION", (String)forgeVersion.getSelectedItem());
                                ret = s.length();
                                System.arraycopy(s.getBytes("UTF-8"), 0, buff, 0, ret);
                            }
                            return ret;
                        }
                    };
                } else {
                    String filename;
                    if( useShadersMod.isSelected() ) {
                        filename = "version-shadersmod.json";
						mod="-shadersmod";
                    } else {
                        filename = "version.json";
                    }
                    version_json = Installer.class.getResourceAsStream(filename);
                }
                jar_id += mod;
                InputStream version_jar =Installer.class.getResourceAsStream("version.jar");
                if( version_jar != null && version_json != null )
                    try {
                        File ver_dir = new File(new File(targetDir,"versions"),jar_id);
                        ver_dir.mkdirs();
                        File ver_json_file = new File (ver_dir, jar_id+".json");
                        FileOutputStream ver_json = new FileOutputStream(ver_json_file);
                        int d;
                        byte data[] = new byte[40960];

                        // Extract json
                        while ((d = version_json.read(data)) != -1) {
                            ver_json.write(data,0,d);
                        }
                        ver_json.close();

                        // Extract new lib
                        File lib_dir = new File(targetDir,"libraries/com/mtbs3d/minecrift/"+version);
                        lib_dir.mkdirs();
                        File ver_file = new File (lib_dir, "minecrift-"+version+".jar");
                        FileOutputStream ver_jar = new FileOutputStream(ver_file);
                        while ((d = version_jar.read(data)) != -1) {
                            ver_jar.write(data,0,d);
                        }
                        ver_jar.close();

                        //Create empty version jar file
                        //All code actually lives in libraries/

                        //ZipOutputStream null_jar = new ZipOutputStream(new FileOutputStream(new File (ver_dir, jar_id+".jar")));
                        //null_jar.putNextEntry(new ZipEntry("Classes actually in libraries directory"));
                        //null_jar.closeEntry();
                        //null_jar.close();
                        return ver_json_file.exists() && ver_file.exists();
                    } catch (Exception e) {
                        finalMessage += " Error: "+e.getLocalizedMessage();
                    }

            }
            return false;
        }

        private boolean EnableHRTF()           // Implementation by Zach Jaggi
        {
            // Find the correct location to stick alsoftrc
            File alsoftrc;

            //I honestly have no clue where Mac stores this, so I'm assuming the same as Linux.
            if (isWindows && appDataDir != null)
            {
                alsoftrc = new File(appDataDir, "alsoft.ini");
            }
            else
            {
                alsoftrc = new File(userHomeDir, ".alsoftrc");
            }
            try
            {
                //Overwrite the current file.
                alsoftrc.createNewFile();
                PrintWriter writer = new PrintWriter(alsoftrc);
                writer.write("hrtf = true\n");
                writer.write("frequency = 44100\n");
                writer.close();
                return true;
            }
            catch (Exception e)
            {
                finalMessage += " Error: "+e.getLocalizedMessage();
            }

            return false;
        }
        
        // VIVE START - install openVR dlls
        private boolean InstallOpenVR() {

		
			String osname = System.getProperty("os.name").toLowerCase();
			String osarch= System.getProperty("os.arch").toLowerCase();

			String osFolder = "win32";		
			String resource = "win32/openvr_api.dll";
				
			if (osname.contains("windows")){	

				installFile(osFolder, resource);
					osFolder = "win64";
					resource = "win64/openvr_api.dll";
					installFile(osFolder, resource);
			}
			else if( osname.contains("linux")){
				osFolder = "linux32";
				resource = "linux32/libopenvr_api.so";
					installFile(osFolder, resource);
					osFolder = "linux64";
					resource = "linux64/libopenvr_api.so";
					installFile(osFolder, resource);
			}
			else if( osname.contains("mac")){
				osFolder = "osx32";
				resource = "osx32/libopenvr_api.dylib";			
				installFile(osFolder, resource);
			}	
		return true;
        }

		private boolean installFile(String osFolder, String resource){
			File win32_dir = new File (targetDir, osFolder);
			win32_dir.mkdirs();


			InputStream openvrdll = Installer.class.getResourceAsStream(resource);
			File dll_out = new File (targetDir, resource);
			if (!copyInputStreamToFile(openvrdll, dll_out))
				return false;
				
			return true;		
		}

        // VIVE END - install openVR dll

        private void sleep(int millis)
        {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {}
        }

        /*
         * Main task. Executed in background thread.
         */
        public String finalMessage;
        @Override
        public Void doInBackground()
        {
            StringBuilder sbErrors = new StringBuilder();
            String minecriftVersionName = "vivecraft-" + version + mod;
            boolean checkedRedists = false;
            boolean redistSuccess = true;
            boolean downloadedForge = false;
            boolean installedForge = false;

            monitor.setProgress(0);

            try {
                // Set progress dialog size (using reflection - hacky)
                Field progressdialog = monitor.getClass().getDeclaredField("dialog");
                if (progressdialog != null) {
                    progressdialog.setAccessible(true);
                    Dialog dlg = (Dialog) progressdialog.get(monitor);
                    if (dlg != null) {
                        dlg.setSize(550, 200);
                        dlg.setLocationRelativeTo(null);
                    }
                }
            }
            catch (NoSuchFieldException e) {}
            catch (IllegalAccessException e) {}


            finalMessage = "Failed: Couldn't download C++ redistributables. ";
            monitor.setNote("Checking for required libraries...");
            monitor.setProgress(5);

            if (System.getProperty("os.name").contains("Windows"))
            {
                // Windows C++ redists (ah the joys of c native code)

                checkedRedists = true;



                // Determine if we have a Win 64bit OS.
                boolean is64bitOS = (System.getenv("ProgramFiles(x86)") != null);

                File redist2012_64 = new File(tempDir + "/vcredist_x64_2012.exe");
                File redist2012_32 = new File(tempDir + "/vcredist_x86_2012.exe");
                File redist2010_64 = new File(tempDir + "/vcredist_x64_2010.exe");
                File redist2010_32 = new File(tempDir + "/vcredist_x86_2010.exe");

                boolean neededRedist2012_64 = false;
                boolean neededRedist2012_32 = false;
                boolean neededRedist2010_64 = false;
                boolean neededRedist2010_32 = false;

                // Download VS 2012 64bit
                if (NEEDS_2012_REDIST && redistSuccess && is64bitOS) {
                    if (!redist2012_64.exists()) {
                        neededRedist2012_64 = true;
                        monitor.setNote("Downloading VC 2012 C++ 64bit redist...");
                        monitor.setProgress(10);
                        if (!downloadFile(winredist2012_64url, redist2012_64)) {
                            redist2012_64.deleteOnExit();
                            redistSuccess = false;
                        }
                    }
                }

                // Download VS 2010 64bit
                if (NEEDS_2010_REDIST && redistSuccess && is64bitOS) {
                    if (!redist2010_64.exists()) {
                        neededRedist2010_64 = true;
                        monitor.setNote("Downloading VC 2010 C++ 64bit redist...");
                        monitor.setProgress(15);
                        if (!downloadFile(winredist2010_64url, redist2010_64)) {
                            redist2010_64.deleteOnExit();
                            redistSuccess = false;
                        }
                    }
                }

                // Download VS 2012 32bit
                if (NEEDS_2012_REDIST && redistSuccess && !redist2012_32.exists()) {
                    neededRedist2012_32 = true;
                    monitor.setNote("Downloading VC 2012 C++ 32bit redist...");
                    monitor.setProgress(20);
                    if (!downloadFile(winredist2012_32url, redist2012_32)) {
                        redist2012_32.deleteOnExit();
                        redistSuccess = false;
                    }
                }

                // Download VS 2010 32bit
                if (NEEDS_2010_REDIST && redistSuccess && !redist2010_32.exists()) {
                    neededRedist2010_32 = true;
                    monitor.setNote("Downloading VC 2010 C++ 32bit redist...");
                    monitor.setProgress(25);
                    if (!downloadFile(winredist2010_32url, redist2010_32)) {
                        redist2010_32.deleteOnExit();
                        redistSuccess = false;
                    }
                }

                // Install VS2012 64bit
                if (NEEDS_2012_REDIST && redistSuccess && is64bitOS && neededRedist2012_64) {
                    monitor.setNote("Installing VC 2012 C++ 32bit redist...");
                    monitor.setProgress(30);
                    try {
                        Process process = new ProcessBuilder(redist2012_64.getAbsolutePath(), "/quiet", "/norestart").start();
                        process.waitFor();
                    } catch (Exception e) {
                        e.printStackTrace();
                        redist2012_64.deleteOnExit();
                        redistSuccess = false;
                    }
                }

                // Install VS2010 64bit
                if (NEEDS_2010_REDIST && redistSuccess && is64bitOS && neededRedist2010_64) {
                    monitor.setNote("Installing VC 2010 C++ 64bit redist...");
                    monitor.setProgress(33);
                    try {
                        Process process = new ProcessBuilder(redist2010_64.getAbsolutePath(), "/quiet", "/norestart").start();
                        process.waitFor();
                    } catch (Exception e) {
                        e.printStackTrace();
                        redist2010_64.deleteOnExit();
                        redistSuccess = false;
                    }
                }

                // Install VS2012 32bit
                if (NEEDS_2012_REDIST && redistSuccess && neededRedist2012_32) {
                    monitor.setNote("Installing VC 2012 C++ 32bit redist...");
                    monitor.setProgress(36);
                    try {
                        Process process = new ProcessBuilder(redist2012_32.getAbsolutePath(), "/quiet", "/norestart").start();
                        process.waitFor();
                    } catch (Exception e) {
                        e.printStackTrace();
                        redist2012_32.deleteOnExit();
                        redistSuccess = false;
                    }
                }

                // Install VS2010 32bit
                if (NEEDS_2010_REDIST && redistSuccess && neededRedist2010_32) {
                    monitor.setNote("Installing VC 2010 C++ 32bit redist...");
                    monitor.setProgress(39);
                    try {
                        Process process = new ProcessBuilder(redist2010_32.getAbsolutePath(), "/quiet", "/norestart").start();
                        process.waitFor();
                    } catch (Exception e) {
                        e.printStackTrace();
                        redist2010_32.deleteOnExit();
                        redistSuccess = false;
                    }
                }
            }

            if (checkedRedists && !redistSuccess) {
				JOptionPane.showMessageDialog(null, "Could not download VC++ Redist. Game might not work.", "Warning", JOptionPane.INFORMATION_MESSAGE);
            }

            finalMessage = "Failed: Couldn't download Optifine. ";
            monitor.setNote("Checking Optifine... Please donate to them!");
            monitor.setProgress(42);
            // Attempt optifine download...
            boolean downloadedOptifine = false;
            monitor.setNote("Downloading Optifine... Please donate to them!");

            for (int i = 1; i <= 3; i++)
            {
                if (DownloadOptiFine())
                {
                    // Got it!
                    downloadedOptifine = true;
                    break;
                }

                // Failed. Sleep a bit and retry...
                if (i < 3) {
                    monitor.setNote("Downloading Optifine... waiting...");
                    try {
                        Thread.sleep(i * 1000);
                    }
                    catch (InterruptedException e) {
                    }
                    monitor.setNote("Downloading Optifine...retrying...");
                }
            }
            
            if(useShadersMod.isSelected()){
	            finalMessage = "Failed: Couldn't download ShadersMod. ";
	            monitor.setNote("Checking ShadersModCore");
	            monitor.setProgress(42);
	            boolean downloadedSMC = false;
	            monitor.setNote("Downloading ShadersModCore");
	
	            for (int i = 1; i <= 3; i++)
	            {
	                if (downloadSMC(useForge.isSelected()))
	                {
	                    // Got it!
	                	downloadedSMC = true;
	                    break;
	                }
	
	                // Failed. Sleep a bit and retry...
	                if (i < 3) {
	                    monitor.setNote("Downloading ShadersModCore... waiting...");
	                    try {
	                        Thread.sleep(i * 1000);
	                    }
	                    catch (InterruptedException e) {
	                    }
	                    monitor.setNote("Downloading ShadersModCore...retrying...");
	                }
	            }
            }
            
            monitor.setProgress(50);
			
            // VIVE START - install openVR
            monitor.setProgress(52);
            monitor.setNote("Installing OpenVR...");
            finalMessage = "Failed: Couldn't extract openvr_api.dll to .minecraft folder.";
            if(!InstallOpenVR())
            {
				monitor.close();
				return null;
            }
            // VIVE END - install openVR
            
            // Setup forge if necessary
            if (useForge.isSelected() && !forgeVersionInstalled) {
                monitor.setProgress(55);
                monitor.setNote("Downloading Forge " + FULL_FORGE_VERSION + "...");
                downloadedForge = downloadFile(forge_url, forgeInstaller);
            }
            if (downloadedForge && useForge.isSelected() && !forgeVersionInstalled) {
                monitor.setProgress(65);
                monitor.setNote("Installing Forge " + FULL_FORGE_VERSION + "...");
                installedForge = installForge(forgeInstaller);
            }
            monitor.setProgress(75);
            monitor.setNote("Extracting correct Vivecraft version...");
            finalMessage = "Failed: Couldn't extract Vivecraft. Try redownloading this installer.";
            if(!ExtractVersion())
            {
                monitor.close();
                return null;
            }
			
			monitor.setNote("Checking for base game...");
			if(!SetupMinecraftAsLibrary())
            {
            JOptionPane.showMessageDialog(null,
                                         "Could not locate or download base game. The Mincraft Launcher will attempt to download it.",
                                         "Warning!",JOptionPane.WARNING_MESSAGE);
            }
			
            if(useHrtf.isSelected())
            {
                monitor.setProgress(85);
                monitor.setNote("Configuring HRTF audio...");
                if(!EnableHRTF())
                {
                    sbErrors.append("Failed to set up HRTF! Vivecraft will still work but audio won't be binaural.\n");
                }
            }
            boolean profileCreated = false;
            if (createProfile.isSelected())
            {
                monitor.setProgress(95);
                monitor.setNote("Creating Vivecraft profile...");
                if (!updateLauncherJson(targetDir, minecriftVersionName))
                    sbErrors.append("Failed to set up 'Vivecraft' profile (you can still manually select Edit Profile->Use Version " + minecriftVersionName + " in the Minecraft launcher)\n");
                else
                    profileCreated = true;
            }
			
            if (!downloadedOptifine) {
                finalMessage = "Installed (but failed to download OptiFine). Restart Minecraft" +
                        (profileCreated == false ? " and Edit Profile->Use Version " + minecriftVersionName : " and select the '" + getMinecraftProfileName(useForge.isSelected(), useShadersMod.isSelected()) + "' profile.") +
                        "\nPlease download and install Optifine " + OF_FILE_NAME + " from https://optifine.net/downloads before attempting to play.";
            }
            else {
                finalMessage = "Installed successfully! Restart Minecraft" +
                        (profileCreated == false ? " and Edit Profile->Use Version " + minecriftVersionName : " and select the '" + getMinecraftProfileName(useForge.isSelected(), useShadersMod.isSelected()) + "' profile.");
            }
			
            monitor.setProgress(100);
            monitor.close();
            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            setCursor(null); // turn off the wait cursor
            JOptionPane.showMessageDialog(null, finalMessage, "Complete", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            emptyFrame.dispose();
        }

    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            System.out.println(progress);
        }
    }

    public void run()
    {
        JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new String[]{"Install", "Cancel"});

        emptyFrame = new Frame("Vivecraft Installer");
        emptyFrame.setUndecorated(true);
        emptyFrame.setVisible(true);
        emptyFrame.setLocationRelativeTo(null);
        dialog = optionPane.createDialog(emptyFrame, "Vivecraft Installer");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        String str =  ((String)optionPane.getValue());
        if (str !=null && ((String)optionPane.getValue()).equalsIgnoreCase("Install"))
        {
            int option = JOptionPane.showOptionDialog(
                                         null,
                                         "Please ensure you have closed the Minecraft launcher before proceeding.\n" +
                                         "Also, if installing with Forge please ensure you have installed Forge " + FORGE_VERSION + " first.",
                                         "Important!",

                                         JOptionPane.OK_CANCEL_OPTION,
                                         JOptionPane.WARNING_MESSAGE, null, null, null);
            
            if (option == JOptionPane.OK_OPTION) {
                monitor = new ProgressMonitor(null, "Installing Vivecraft...", "", 0, 100);
                monitor.setMillisToDecideToPopup(0);
                monitor.setMillisToPopup(0);

                task = new InstallTask();
                task.addPropertyChangeListener(this);
                task.execute();
            }
            else{
                dialog.dispose();
                emptyFrame.dispose();
            }
        }
        else{
            dialog.dispose();
            emptyFrame.dispose();
        }
    }

    private static void createAndShowGUI() {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase();
        String mcDir = ".minecraft";
        File minecraftDir;

        if (osType.contains("win") && System.getenv("APPDATA") != null)
        {
            minecraftDir = new File(System.getenv("APPDATA"), mcDir);
        }
        else if (osType.contains("mac"))
        {
            minecraftDir = new File(new File(new File(userHomeDir, "Library"),"Application Support"),"minecraft");
        }
        else
        {
            minecraftDir = new File(userHomeDir, mcDir);
            releaseNotePathAddition = "/";
        }

        Installer panel = new Installer(minecraftDir);
        panel.run();
    }

    private boolean updateLauncherJson(File mcBaseDirFile, String minecriftVer)
    {
        boolean result = false;

        try {
            int jsonIndentSpaces = 2;
            String profileName = getMinecraftProfileName(useForge.isSelected(), useShadersMod.isSelected());
            File fileJson = new File(mcBaseDirFile, "launcher_profiles.json");
            String json = readAsciiFile(fileJson);
            JSONObject root = new JSONObject(json);
            //System.out.println(root.toString(jsonIndentSpaces));

            JSONObject profiles = (JSONObject)root.get("profiles");
            JSONObject prof = null;
            try {
                prof = (JSONObject) profiles.get(profileName);
            }
            catch (Exception e) {}

            if (prof == null) {
                prof = new JSONObject();
                prof.put("name", profileName);
                prof.put("javaArgs", "-Xmx2G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn256M -Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true");
                prof.put("useHopperCrashService", false);
                prof.put("launcherVisibilityOnGameClose", "keep the launcher open");
				prof.put("launcherVisibilityOnGameClose", "keep the launcher open");
                profiles.put(profileName, prof);
            }
            prof.put("lastVersionId", minecriftVer + mod);
            root.put("selectedProfile", profileName);

            FileWriter fwJson = new FileWriter(fileJson);
            fwJson.write(root.toString(jsonIndentSpaces));
            fwJson.flush();
            fwJson.close();

            result = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private class FileSelectAction extends AbstractAction
    {
        private static final long serialVersionUID = 743815386102831493L;

        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(targetDir);
            dirChooser.setSelectedFile(targetDir);
            int response = dirChooser.showOpenDialog(Installer.this);
            switch (response)
            {
                case JFileChooser.APPROVE_OPTION:
                    targetDir = dirChooser.getSelectedFile();
                    updateFilePath();
                    break;
                default:
                    break;
            }
        }
    }

    private class updateActionF extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
        	updateInstructions();
        }
    }
    
    private class updateActionSM extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
        	updateInstructions();
        }
    }
    
        private class updateActionP extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
        	updateInstructions();
        }
    }
    
    
    public Installer(File targetDir)
    {
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel logoSplash = new JPanel();
        logoSplash.setLayout(new BoxLayout(logoSplash, BoxLayout.Y_AXIS));
        try {
            // Read png
            BufferedImage image;
            image = ImageIO.read(Installer.class.getResourceAsStream("logo.png"));
            ImageIcon icon = new ImageIcon(image);
            JLabel logoLabel = new JLabel(icon);
            logoLabel.setAlignmentX(CENTER_ALIGNMENT);
            logoLabel.setAlignmentY(CENTER_ALIGNMENT);
            logoLabel.setSize(image.getWidth(), image.getHeight());
            if (!QUIET_DEV)	// VIVE - hide oculus logo
	            logoSplash.add(logoLabel);
        } catch (IOException e) {
        } catch( IllegalArgumentException e) {
        }

        userHomeDir = System.getProperty("user.home", ".");
        osType = System.getProperty("os.name").toLowerCase();
        if (osType.contains("win"))
        {
            isWindows = true;
            appDataDir = System.getenv("APPDATA");
        }

        version = "UNKNOWN";
        try {
            InputStream ver = Installer.class.getResourceAsStream("version");
            if( ver != null )
            {
                String[] tok = new BufferedReader(new InputStreamReader(ver)).readLine().split(":");
                if( tok.length > 0)
                {
                    jar_id = tok[0];
                    version = tok[1];
                }
            }
        } catch (IOException e) { }

        // Read release notes, save to file
        String tmpFileName = System.getProperty("java.io.tmpdir") + releaseNotePathAddition + "Vivecraft" + version.toLowerCase() + "_release_notes.txt";
        releaseNotes = new File(tmpFileName);
        InputStream is = Installer.class.getResourceAsStream("release_notes.txt");
        if (!copyInputStreamToFile(is, releaseNotes)) {
            releaseNotes = null;
        }

        JLabel tag = new JLabel("Welcome! This will install Vivecraft "+ version);
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);
        
        logoSplash.add(Box.createRigidArea(new Dimension(5,20)));
        tag = new JLabel("Select path to minecraft. (The default here is almost always what you want.)");
        tag.setAlignmentX(CENTER_ALIGNMENT);
        tag.setAlignmentY(CENTER_ALIGNMENT);
        logoSplash.add(tag);

        logoSplash.setAlignmentX(CENTER_ALIGNMENT);
        logoSplash.setAlignmentY(TOP_ALIGNMENT);
        this.add(logoSplash);


        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel,BoxLayout.X_AXIS));
        
        Installer.targetDir = targetDir;
        selectedDirText = new JTextField();
        selectedDirText.setEditable(false);
        selectedDirText.setToolTipText("Path to minecraft");
        selectedDirText.setColumns(30);
        entryPanel.add(selectedDirText);
        JButton dirSelect = new JButton();
        dirSelect.setAction(new FileSelectAction());
        dirSelect.setText("...");
        dirSelect.setToolTipText("Select an alternative minecraft directory");
        entryPanel.add(dirSelect);

        entryPanel.setAlignmentX(LEFT_ALIGNMENT);
        entryPanel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel = new JLabel();
        infoLabel.setHorizontalTextPosition(JLabel.LEFT);
        infoLabel.setVerticalTextPosition(JLabel.TOP);
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel.setVisible(false);

        fileEntryPanel = new JPanel();
        fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel,BoxLayout.Y_AXIS));
        fileEntryPanel.add(infoLabel);
        fileEntryPanel.add(entryPanel);
        
        fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
        fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
        this.add(fileEntryPanel);
        this.add(Box.createVerticalStrut(5));

        JPanel optPanel = new JPanel();
        optPanel.setLayout( new BoxLayout(optPanel, BoxLayout.Y_AXIS));
        optPanel.setAlignmentX(LEFT_ALIGNMENT);
        optPanel.setAlignmentY(TOP_ALIGNMENT);

        //Add forge options
        JPanel forgePanel = new JPanel();
        forgePanel.setLayout( new BoxLayout(forgePanel, BoxLayout.X_AXIS));
        //Create forge: no/yes buttons
        useForge = new JCheckBox();
        AbstractAction actf = new updateActionF();
        actf.putValue(AbstractAction.NAME, "Install Vivecraft with Forge " + FORGE_VERSION);
        useForge.setAction(actf);
        forgeVersion = new JComboBox();
        if (!ALLOW_FORGE_INSTALL)
            useForge.setEnabled(false);
        useForge.setToolTipText(
                "<html>" +
                "If checked, installs Vivecraft with Forge support. The correct version of Forge<br>" +
                "(as displayed) must already be installed.<br>" +
                "</html>");

        //Add "yes" and "which version" to the forgePanel
        useForge.setAlignmentX(LEFT_ALIGNMENT);
        forgeVersion.setAlignmentX(LEFT_ALIGNMENT);
        forgePanel.add(useForge);
        //forgePanel.add(forgeVersion);
		
        // Profile creation / update support
        createProfile = new JCheckBox("", true);
		AbstractAction actp = new updateActionP();
        actp.putValue(AbstractAction.NAME, "Create Vivecraft launcher profile");
        createProfile.setAction(actp);
        createProfile.setAlignmentX(LEFT_ALIGNMENT);
        createProfile.setSelected(true);
        createProfile.setToolTipText(
                "<html>" +
                "If checked, if a Vivecraft profile doesn't already exist within the Minecraft launcher<br>" +
                "one is added. Then the profile is selected, and this Vivecraft version is set as the<br>" +
                "current version.<br>" +
                "</html>");

		useShadersMod = new JCheckBox();
        useShadersMod.setAlignmentX(LEFT_ALIGNMENT);
        if (!ALLOW_SHADERSMOD_INSTALL)
			useShadersMod.setEnabled(false);
        AbstractAction acts = new updateActionSM();
        acts.putValue(AbstractAction.NAME, "Install Vivecraft with ShadersMod 2.3.29");
        useShadersMod.setAction(acts);
        useShadersMod.setToolTipText(
                "<html>" +
                "If checked, sets the vivecraft profile to use ShadersMod <br>" +
                "support." +
                "</html>");

        useHydra = new JCheckBox("Razer Hydra support",false);
        useHydra.setAlignmentX(LEFT_ALIGNMENT);
        if (!ALLOW_HYDRA_INSTALL)
            useHydra.setEnabled(false);
        useHydra.setToolTipText(
                "<html>" +
                "If checked, installs the additional Razor Hydra native library required for Razor Hydra<br>" +
                "support." +
                "</html>");

        useHrtf = new JCheckBox("Enable binaural audio (Only needed once per PC)", false);
        useHrtf.setToolTipText(
                "<html>" +
                        "If checked, the installer will create the configuration file needed for OpenAL HRTF<br>" +
                        "ear-aware sound in Minecraft (and other games).<br>" +
                        " If the file has previously been created, you do not need to check this again.<br>" +
                        " NOTE: Your sound card's output MUST be set to 44.1Khz.<br>" +
                        " WARNING, will overwrite " + (isWindows ? (appDataDir + "\\alsoft.ini") : (userHomeDir + "/.alsoftrc")) + "!<br>" +
                        " Delete the " + (isWindows ? "alsoft.ini" : "alsoftrc") + " file to disable HRTF again." +
                        "</html>");
        useHrtf.setAlignmentX(LEFT_ALIGNMENT);

        //Add option panels option panel
        forgePanel.setAlignmentX(LEFT_ALIGNMENT);
        
        optPanel.add(forgePanel);
        optPanel.add(useShadersMod);
        optPanel.add(createProfile);
        optPanel.add(useHrtf);
        this.add(optPanel);

        this.add(Box.createRigidArea(new Dimension(5,20))); 
        
        instructions = new JLabel("",SwingConstants.CENTER);
        instructions.setAlignmentX(CENTER_ALIGNMENT);
        instructions.setAlignmentY(TOP_ALIGNMENT);
        instructions.setForeground(Color.RED);
        instructions.setPreferredSize(new Dimension(20, 40));
         this.add(instructions);
        
        
        this.add(Box.createVerticalGlue());
        JLabel github = linkify("Vivecraft is open source. find it on Github","https://github.com/jrbudda/minecrift/releases","Vivecraft Github");
        JLabel wiki = linkify("Vivecraft home page","http://www.vivecraft.org","Vivecraft Home");
        JLabel donate = linkify("If you think Vivecraft is awesome, please consider donating.","https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=JVBJLN5HJJS52&lc=US&item_name=jrbudda&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)","jrbudda's Paypal");
        JLabel optifine = linkify("Vivecraft includes OptiFine for performance. Consider donating to them as well.","http://optifine.net/donate.php","http://optifine.net/donate.php");

        github.setAlignmentX(CENTER_ALIGNMENT);
        github.setHorizontalAlignment(SwingConstants.CENTER);
        wiki.setAlignmentX(CENTER_ALIGNMENT);
        wiki.setHorizontalAlignment(SwingConstants.CENTER);
        donate.setAlignmentX(CENTER_ALIGNMENT);
        donate.setHorizontalAlignment(SwingConstants.CENTER);
        optifine.setAlignmentX(CENTER_ALIGNMENT);
        optifine.setHorizontalAlignment(SwingConstants.CENTER);
         
        this.add(Box.createRigidArea(new Dimension(5,20)));
        this.add( github );
        this.add( wiki );
        this.add( donate );
        this.add( optifine );

        this.setAlignmentX(LEFT_ALIGNMENT);
        updateFilePath();
		updateInstructions();
    }


    private void updateInstructions(){
	String out = "<html>";
		if(createProfile.isSelected()){
			out += "Please make sure the Minecraft Launcher is not running.";
		}
    	if (useForge.isSelected()){
			out += "<br>Please make sure Forge has been installed first.";
    	}
    	if (useForge.isSelected() && useShadersMod.isSelected()){
    	//	out += "Please make sure that ShadersModCore is NOT in your Forge mods folder!";
    	}
    	out+="</html>";
    	instructions.setText(out);
    	
    }
    
    private void updateFilePath()
    {
        try
        {
            targetDir = targetDir.getCanonicalFile();
            if( targetDir.exists() ) {
                File ForgeDir = new File( targetDir, "libraries"+File.separator+"net"+File.separator+"minecraftforge"+File.separator+"forge");
                if( ForgeDir.isDirectory() ) {
                    forgeVersions = ForgeDir.list();
                    if (forgeVersions != null && forgeVersions.length > 0) {
                        // Check for the currently required forge
                        for (String forgeVersion : forgeVersions) {
                            if (forgeVersion.contains(FORGE_VERSION)) {
                                forgeVersionInstalled = true;
                                break;
                            }
                        }
                    }
                }
            }
            selectedDirText.setText(targetDir.getPath());
            selectedDirText.setForeground(Color.BLACK);
            infoLabel.setVisible(false);
            fileEntryPanel.setBorder(null);
            if (dialog!=null)
            {
                dialog.invalidate();
                dialog.pack();
            }
        }
        catch (IOException e)
        {

            selectedDirText.setForeground(Color.RED);
            fileEntryPanel.setBorder(new LineBorder(Color.RED));
            infoLabel.setText("<html>"+"Error!"+"</html>");
            infoLabel.setVisible(true);
            if (dialog!=null)
            {
                dialog.invalidate();
                dialog.pack();
            }
        }
        if( forgeVersions == null || forgeVersions.length == 0 )
            forgeVersions =  new String[] { };
        forgeVersion.setModel( new DefaultComboBoxModel(forgeVersions));
    }


    public static void main(String[] args)
    {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
		try {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
		     } catch (Exception e) { e.printStackTrace(); }
    }
    public static JLabel linkify(final String text, String URL, String toolTip)
    {
        URI temp = null;
        try
        {
            temp = new URI(URL);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        final URI uri = temp;
        final JLabel link = new JLabel();
        link.setText("<HTML><FONT color=\"#000099\">"+text+"</FONT></HTML>");
        if(!toolTip.equals(""))
            link.setToolTipText(toolTip);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseListener() {
            public void mouseExited(MouseEvent arg0) {
				link.setText("<HTML><FONT color=\"#000099\">"+text+"</FONT></HTML>");
            }

            public void mouseEntered(MouseEvent arg0) {
				link.setText("<HTML><FONT color=\"#000099\"><U>"+text+"</U></FONT></HTML>");
            }

            public void mouseClicked(MouseEvent arg0) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane pane = new JOptionPane("Could not open link.");
                    JDialog dialog = pane.createDialog(new JFrame(), "");
                    dialog.setVisible(true);
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }
        });
        return link;
    }

    private String getMinecraftProfileName(boolean usingForge, boolean sm)
    {
        if(!usingForge) {
			if(sm)
				return "ViveCraft-ShadersMod " + MINECRAFT_VERSION;
			else
				return "ViveCraft " + MINECRAFT_VERSION;
        } else if(sm)
				return "ViveCraft-SM-Forge " + MINECRAFT_VERSION;
			else
				return "ViveCraft-Forge " + MINECRAFT_VERSION;
    }

    public static String readAsciiFile(File file)
            throws IOException
    {
        FileInputStream fin = new FileInputStream(file);
        InputStreamReader inr = new InputStreamReader(fin, "ASCII");
        BufferedReader br = new BufferedReader(inr);
        StringBuffer sb = new StringBuffer();
        for (;;) {
            String line = br.readLine();
            if (line == null)
                break;

            sb.append(line);
            sb.append("\n");
        }
        br.close();
        inr.close();
        fin.close();

        return sb.toString();
    }
	
	private boolean copyInputStreamToFile( InputStream in, File file ) 
	{
        if (in == null || file == null)
            return false;

        boolean success = true;

        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

}
