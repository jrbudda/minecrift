import os, os.path, sys, json, datetime, StringIO
import shutil, tempfile,zipfile, fnmatch
from xml.dom.minidom import parse
from optparse import OptionParser
import subprocess, shlex
from tempfile import mkstemp
from shutil import move
from os import remove, close
from install import download_deps, download_native, download_file, mkdir_p, replacelineinfile
from minecriftversion import mc_version, of_file_name, of_json_name, minecrift_version_num, \
  minecrift_build, of_file_extension, of_file_md5, mcp_version, forge_version, mc_file_md5

try:
    WindowsError
except NameError:
    WindowsError = OSError

base_dir = os.path.dirname(os.path.abspath(__file__))

def cmdsplit(args):
    if os.sep == '\\':
        args = args.replace('\\', '\\\\')
    return shlex.split(args)

def zipmerge( target_file, source_file ):
    out_file, out_filename = tempfile.mkstemp()
    out = zipfile.ZipFile(out_filename,'a')
    target = zipfile.ZipFile( target_file, 'r')
    source = zipfile.ZipFile( source_file, 'r' )

    #source supersedes target
    source_files = set( source.namelist() )
    target_files = set( target.namelist() ) - source_files

    for file in source_files:
        out.writestr( file, source.open( file ).read() )

    for file in target_files:
        out.writestr( file, target.open( file ).read() )

    source.close()
    target.close()
    out.close()
    #os.remove( target_file )
    shutil.copy( out_filename, target_file )

def process_json( addon, version ):
    json_id = "vivecraft-"+version+addon
    lib_id = "com.mtbs3d:minecrift:"+version
    time = datetime.datetime(1979,6,1).strftime("%Y-%m-%dT%H:%M:%S-05:00")
    with  open(os.path.join("installer","vivecraft-" + mc_version + addon + ".json"),"rb") as f:
        json_obj = json.load(f)
        json_obj["id"] = json_id
        json_obj["time"] = time
        json_obj["releaseTime"] = time
        json_obj["libraries"].insert(0,{"name":lib_id}) #Insert at beginning
        #json_obj["libraries"].append({"name":"net.minecraft:Minecraft:"+mc_version}) #Insert at end
        return json.dumps( json_obj, indent=1 )

def create_install(mcp_dir):
    print "Creating Installer..."
    reobf = os.path.join(mcp_dir,'reobf','minecraft')
    assets = os.path.join(base_dir,"assets")
    # VIVE - removed from inner loop. blk.class is EntityPlayerSP, not anything to do with sound?
    #if cur_file=='blk.class': #skip SoundManager
    #continue
    in_mem_zip = StringIO.StringIO()
    with zipfile.ZipFile( in_mem_zip,'w', zipfile.ZIP_DEFLATED) as zipout:
        for abs_path, _, filelist in os.walk(reobf, followlinks=True):
            arc_path = os.path.relpath( abs_path, reobf ).replace('\\','/').replace('.','')+'/'
            for cur_file in fnmatch.filter(filelist, '*.class'):
                #if cur_file in {'MinecriftClassTransformer.class','MinecriftForgeTweaker.class','MinecriftClassTransformer$Stage.class','MinecriftClassTransformer$1.class','MinecriftClassTransformer$2.class','MinecriftClassTransformer$3.class','MinecriftClassTransformer$4.class'}:
                if cur_file in {'abt.class','abu.class','abv.class', 'abw.class', 'abx.class', 'aby.class', 'abz.class', 'aca.class', 'acb.class', 'acc.class', 'acd.class', 'ace.class', 'acf.class'}: #skip CreativeTabs
                    continue
                if cur_file in {'bgt.class','bgu.class','bgv.class','bgw.class'}: #skip Main inner classes
                    continue
                in_file= os.path.join(abs_path,cur_file)
                arcname =  arc_path + cur_file
                zipout.write(in_file, arcname)
        print "Checking Assets..."
        for a, b, c in os.walk(assets):
            print a
            arc_path = os.path.relpath(a,base_dir).replace('\\','/').replace('.','')+'/'
            for cur_file in c:
                print "Adding asset %s..." % cur_file
                in_file= os.path.join(a,cur_file) 
                arcname =  arc_path + cur_file
                zipout.write(in_file, arcname)
    os.chdir( base_dir )

    
    in_mem_zip.seek(0)
    if os.getenv("RELEASE_VERSION"):
        version = os.getenv("RELEASE_VERSION")
    elif os.getenv("BUILD_NUMBER"):
        version = "b"+os.getenv("BUILD_NUMBER")
    else:
        version = minecrift_build

    version = minecrift_version_num+"-"+version
    
    # Replace version info in installer.java
    print "Updating installer versions..."
    installer_java_file = os.path.join("installer","Installer.java")
    replacelineinfile( installer_java_file, "private static final String MINECRAFT_VERSION", "    private static final String MINECRAFT_VERSION = \"%s\";\n" % mc_version );
    replacelineinfile( installer_java_file, "private static final String MC_VERSION",        "    private static final String MC_VERSION        = \"%s\";\n" % minecrift_version_num );
    replacelineinfile( installer_java_file, "private static final String MC_MD5",            "    private static final String MC_MD5            = \"%s\";\n" % mc_file_md5 );
    replacelineinfile( installer_java_file, "private static final String OF_FILE_NAME",      "    private static final String OF_FILE_NAME      = \"%s\";\n" % of_file_name );
    replacelineinfile( installer_java_file, "private static final String OF_JSON_NAME",      "    private static final String OF_JSON_NAME      = \"%s\";\n" % of_json_name );
    replacelineinfile( installer_java_file, "private static final String OF_MD5",            "    private static final String OF_MD5            = \"%s\";\n" % of_file_md5 );
    replacelineinfile( installer_java_file, "private static final String OF_VERSION_EXT",    "    private static final String OF_VERSION_EXT    = \"%s\";\n" % of_file_extension );
    replacelineinfile( installer_java_file, "private static final String FORGE_VERSION",     "    private static final String FORGE_VERSION     = \"%s\";\n" % forge_version );

    # Build installer.java
    print "Recompiling Installer.java..."
    subprocess.Popen( 
        cmdsplit("javac -source 1.6 -target 1.6 \"%s\"" % os.path.join(base_dir,installer_java_file)), 
            cwd=os.path.join(base_dir,"installer"),
            bufsize=-1).communicate()
	
    artifact_id = "vivecraft-"+version
    installer_id = artifact_id+"-installer"
    installer = os.path.join( installer_id+".jar" ) 
    shutil.copy( os.path.join("installer","installer.jar"), installer )
    with zipfile.ZipFile( installer,'a', zipfile.ZIP_DEFLATED) as install_out: #append to installer.jar
    
        # Add newly compiled class files
        for dirName, subdirList, fileList in os.walk("installer"):
            for afile in fileList:
                if os.path.isfile(os.path.join(dirName,afile)) and afile.endswith('.class'):
                    relpath = os.path.relpath(dirName, "installer")
                    print "Adding %s..." % os.path.join(relpath,afile)
                    install_out.write(os.path.join(dirName,afile), os.path.join(relpath,afile))
            
        # Add json files
        install_out.writestr( "version.json", process_json("", version))
        install_out.writestr( "version-forge.json", process_json("-forge", version))
        install_out.writestr( "version-shadersmod.json", process_json("-shadersmod", version))
        install_out.writestr( "version-forge-shadersmod.json", process_json("-forge-shadersmod", version))	
        
        # Add release notes
        install_out.write("CHANGES.md", "release_notes.txt")
        
        # Add version jar - this contains all the changed files (effectively minecrift.jar). A mix
        # of obfuscated and non-obfuscated files.
        install_out.writestr( "version.jar", in_mem_zip.read() )
        
        # Add the version info
        install_out.writestr( "version", artifact_id+":"+version )

    print("Creating Installer exe...")
    with open( os.path.join("installer","launch4j.xml"),"r" ) as inlaunch:
        with open( os.path.join("installer","launch4j","launch4j.xml"), "w" ) as outlaunch:
            outlaunch.write( inlaunch.read().replace("installer",installer_id))
    
    print("Invoking launch4j...")
    subprocess.Popen( 
        cmdsplit("java -jar \"%s\" \"%s\""% (
                os.path.join( base_dir,"installer","launch4j","launch4j.jar"),
                os.path.join( base_dir,"installer","launch4j","launch4j.xml"))), 
            cwd=os.path.join(base_dir,"installer","launch4j"),
            bufsize=-1).communicate()
            
    os.unlink( os.path.join( base_dir,"installer","launch4j","launch4j.xml") )
  
def readpomversion(pomFile):
	if not os.path.exists(pomFile):
		return ''
		
	dom = parse(pomFile)
	project = dom.getElementsByTagName("project")[0]
	version = str(project.getElementsByTagName("version")[0].firstChild.nodeValue)
	return version
  
def main(mcp_dir):
    print 'Using mcp dir: %s' % mcp_dir
    print 'Using base dir: %s' % base_dir
    
    print("Refreshing dependencies...")
    download_deps( mcp_dir, False )
    
    sys.path.append(mcp_dir)
    os.chdir(mcp_dir)

    reobf = os.path.join(mcp_dir,'reobf','minecraft')
    try:
        pass
        shutil.rmtree(reobf)
    except OSError:
        pass
		
	# Read Minecrift lib versions
	jRiftPom = os.path.join(base_dir, 'JRift', 'JRift', 'pom.xml')
	jRiftLibraryPom = os.path.join(base_dir, 'JRift', 'JRiftLibrary', 'pom.xml')
	jRiftVer = readpomversion(jRiftPom)
	print 'JRift: %s' % jRiftVer
	jRiftLibraryVer = readpomversion(jRiftLibraryPom)
	print 'JRiftLibrary: %s' % jRiftLibraryVer
        
    # Update Minecrift version
    minecraft_java_file = os.path.join(mcp_dir,'src','minecraft','net','minecraft','client','Minecraft.java')
    if os.path.exists(minecraft_java_file):
        print "Updating Minecraft.java with Vivecraft version: [Vivecraft %s %s] %s" % ( minecrift_version_num, minecrift_build, minecraft_java_file ) 
        replacelineinfile( minecraft_java_file, "public final String minecriftVerString",     "    public final String minecriftVerString = \"Vivecraft %s %s\";\n" % (minecrift_version_num, minecrift_build) );        

    print("Recompiling...")
    from runtime.mcp import recompile_side, reobfuscate_side
    from runtime.commands import Commands, CLIENT
    commands = Commands(None, verify=True)
    recompile_side( commands, CLIENT)

    print("Reobfuscating...")
    commands.creatergcfg(reobf=True, keep_lvt=True, keep_generics=True, srg_names=False)
    reobfuscate_side( commands, CLIENT )
    create_install( mcp_dir )
    
if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-m', '--mcp-dir', action='store', dest='mcp_dir', help='Path to MCP to use', default=None)
    options, _ = parser.parse_args()

    if not options.mcp_dir is None:
        main(os.path.abspath(options.mcp_dir))
    elif os.path.isfile(os.path.join('..', 'runtime', 'commands.py')):
        main(os.path.abspath('..'))
    else:
        main(os.path.abspath(mcp_version))	
