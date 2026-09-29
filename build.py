import os, os.path, sys, json, datetime, io
import shutil, tempfile,zipfile, fnmatch
from xml.dom.minidom import parse
from optparse import OptionParser
import subprocess, shlex
from tempfile import mkstemp
from shutil import move
from os import remove, close
import legacy40r12
from install import download_deps, download_native, download_file, mkdir_p, replacelineinfile, _py2_set_order
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

def _py2_json(obj):
    # Python 2.7 wrote dict keys in hash order; rebuild every dict in that order so
    # json.dumps produces the same bytes as the original Python 2 build.
    if isinstance(obj, dict):
        return dict((k, _py2_json(obj[k])) for k in _py2_set_order(list(obj)))
    if isinstance(obj, list):
        return [_py2_json(v) for v in obj]
    return obj

def process_json(addon, version, mcversion, forgeversion, ofversion):
    json_id = "vivecraft-"+version+addon
    lib_id = "com.mtbs3d:minecrift:"+version
    time = datetime.datetime(1979,6,1).strftime("%Y-%m-%dT%H:%M:%S-05:00")
    with  open(os.path.join("installer","vivecraft-" + mc_version + addon + ".json"),"r") as f:
        s=f.read()
        s=s.replace("$MCVERSION", mcversion)
        s=s.replace("$FORGEVERSION", forgeversion)
        s=s.replace("$OFVERSION", ofversion)
        s=s.replace("$VERSION", version+addon)
        json_obj = json.loads(s)
        json_obj["id"] = json_id
        json_obj["time"] = time
        json_obj["releaseTime"] = time
        json_obj["libraries"].insert(0,{"name":lib_id, "MMC-hint":"local"}) #Insert at beginning
        #json_obj["libraries"].append({"name":"net.minecraft:Minecraft:"+mc_version}) #Insert at end
        return json.dumps( _py2_json(json_obj), indent=1, separators=(', ', ': ') )

# The official installer program was compiled with JDK 11's javac (Installer.java) and
# shipped org/json classes compiled by JDK 7 for Java 6. main() uses those JDKs when they
# are installed so the installer is byte-identical too; Installer.java needs -source 1.8.
javac_cmd = "javac"
json_javac_cmd = None
JSON_SOURCES = ["JSONObject", "JSONArray", "JSONException", "JSONString", "JSONTokener"]

def create_install(mcp_dir):
    print("Creating Installer...")
    if os.getenv("RELEASE_VERSION"):
        version = os.getenv("RELEASE_VERSION")
    elif os.getenv("BUILD_NUMBER"):
        version = "b"+os.getenv("BUILD_NUMBER")
    else:
        version = minecrift_build

    version = minecrift_version_num+"-"+version
    
    artifact_id = "vivecraft-"+version
    installer_id = artifact_id+"-installer"
    legacy40r12.load(installer_id)
    reobf = os.path.join(mcp_dir,'reobf','minecraft')
    assets = os.path.join(base_dir,"assets")
    # VIVE - removed from inner loop. blk.class is EntityPlayerSP, not anything to do with sound?
    #if cur_file=='blk.class': #skip SoundManager
    #continue
    in_mem_zip = io.BytesIO()
    with zipfile.ZipFile( in_mem_zip,'w', zipfile.ZIP_DEFLATED) as zipout:
        legacy40r12.pin_in_order(zipout)
        vanilla = os.listdir(reobf)
        for abs_path, _, filelist in os.walk(reobf, followlinks=True):
            arc_path = os.path.relpath( abs_path, reobf ).replace('\\','/').replace('.','')+'/'
            for cur_file in fnmatch.filter(filelist, '*.class'):
                #if cur_file in {'MinecriftClassTransformer.class','MinecriftForgeTweaker.class','MinecriftClassTransformer$Stage.class','MinecriftClassTransformer$1.class','MinecriftClassTransformer$2.class','MinecriftClassTransformer$3.class','MinecriftClassTransformer$4.class'}:
                flg = False
                if cur_file in {'abt.class','abu.class','abv.class', 'abw.class', 'abx.class', 'aby.class', 'abz.class', 'aca.class', 'acb.class', 'acc.class', 'acd.class', 'ace.class', 'acf.class'}: #skip CreativeTabs
                    continue
                if cur_file in {'bgt.class','bgu.class','bgv.class','bgw.class'}: #skip Main inner classes
                    continue

                if cur_file in vanilla: #these misbehave when loaded in this jar, do some magic.
                    flg = True
                in_file= os.path.join(abs_path,cur_file)
                arcname =  arc_path + cur_file
                #if flg:
                #    arcname =  arc_path + cur_file.replace('.class', '.clazz')
                legacy40r12.write(zipout, in_file, arcname)
        print("Checking Assets...")
        for a, b, c in os.walk(assets):
            print(a)
            arc_path = os.path.relpath(a,base_dir).replace('\\','/').replace('.','')+'/'
            for cur_file in c:
                print("Adding asset %s..." % cur_file)
                in_file= os.path.join(a,cur_file) 
                arcname =  arc_path + cur_file
                legacy40r12.write(zipout, in_file, arcname)
    os.chdir( base_dir )

    
    in_mem_zip.seek(0)
    # Replace version info in installer.java
    print("Updating installer versions...")
    installer_java_file = os.path.join("installer","Installer.java")
    replacelineinfile( installer_java_file, "private static final String MINECRAFT_VERSION", "    private static final String MINECRAFT_VERSION = \"%s\";\n" % mc_version );
    replacelineinfile( installer_java_file, "private static final String MC_VERSION",        "    private static final String MC_VERSION        = \"%s\";\n" % minecrift_version_num );
    replacelineinfile( installer_java_file, "private static final String MC_MD5",            "    private static final String MC_MD5            = \"%s\";\n" % mc_file_md5 );
    replacelineinfile( installer_java_file, "private static final String OF_FILE_NAME",      "    private static final String OF_FILE_NAME      = \"%s\";\n" % of_file_name );
    replacelineinfile( installer_java_file, "private static final String OF_JSON_NAME",      "    private static final String OF_JSON_NAME      = \"%s\";\n" % of_json_name );
    replacelineinfile( installer_java_file, "private static final String OF_MD5",            "    private static final String OF_MD5            = \"%s\";\n" % of_file_md5 );
    replacelineinfile( installer_java_file, "private static final String OF_VERSION_EXT",    "    private static final String OF_VERSION_EXT    = \"%s\";\n" % of_file_extension );
    replacelineinfile( installer_java_file, "private static String FORGE_VERSION",     "    private static String FORGE_VERSION     = \"%s\";\n" % forge_version );

    # Build installer.java
    if json_javac_cmd:
        print("Compiling org/json for Java 6...")
        subprocess.Popen(
            cmdsplit("%s -source 1.6 -target 1.6 %s" % (json_javac_cmd, " ".join("org/json/%s.java" % n for n in JSON_SOURCES))),
                cwd=os.path.join(base_dir,"installer"),
                bufsize=-1).communicate()
    print("Recompiling Installer.java...")
    subprocess.Popen( 
        cmdsplit("%s -source 1.8 -target 1.8 \"%s\"" % (javac_cmd, os.path.join(base_dir,installer_java_file))), 
            cwd=os.path.join(base_dir,"installer"),
            bufsize=-1).communicate()
	
    installer = os.path.join( installer_id+".jar" ) 
    shutil.copy( os.path.join("installer","installer.jar"), installer )
    with zipfile.ZipFile( installer,'a', zipfile.ZIP_DEFLATED) as install_out: #append to installer.jar
    
        # Add newly compiled class files
        class_files = []
        for dirName, subdirList, fileList in os.walk("installer"):
            for afile in fileList:
                if os.path.isfile(os.path.join(dirName,afile)) and afile.endswith('.class'):
                    relpath = os.path.relpath(dirName, "installer")
                    class_files.append((os.path.join(dirName,afile), os.path.join(relpath,afile)))
        for path, arcname in legacy40r12.with_stale_classes(class_files):
            print("Adding %s..." % arcname)
            legacy40r12.write(install_out, path, arcname)

        # Add json files
        legacy40r12.writestr(install_out, "version.json", process_json("", version,minecrift_version_num,"",of_file_name ))
        legacy40r12.writestr(install_out, "version-forge.json", process_json("-forge", version,minecrift_version_num,forge_version,of_file_name ))
        legacy40r12.writestr(install_out, "version-shadersmod.json", process_json("-shadersmod", version,minecrift_version_num,"",of_file_name ))
        legacy40r12.writestr(install_out, "version-forge-shadersmod.json", process_json("-forge-shadersmod", version,minecrift_version_num,forge_version,of_file_name ))        
        legacy40r12.writestr(install_out, "version-multimc.json", process_json("-multimc", version,minecrift_version_num,"",of_file_name ))
        legacy40r12.writestr(install_out, "version-multimc-forge.json", process_json("-multimc-forge", version,minecrift_version_num,"",of_file_name ))


        
        # Add release notes
        legacy40r12.write(install_out, "CHANGES.md", "release_notes.txt")
        
        # Add version jar - this contains all the changed files (effectively minecrift.jar). A mix
        # of obfuscated and non-obfuscated files.
        legacy40r12.writestr(install_out, "version.jar", in_mem_zip.read())
        
        # Add the version info
        legacy40r12.writestr(install_out, "version", artifact_id+":"+version)
    legacy40r12.stop()

    print("Creating Installer exe...")
    with open( os.path.join("installer","launch4j.xml"),"r" ) as inlaunch:
        with open( os.path.join("installer","launch4j","launch4j.xml"), "w" ) as outlaunch:
            outlaunch.write( legacy40r12.launch4j_config(inlaunch.read().replace("installer",installer_id), os.path.join(base_dir,"installer","launch4j")))
    
    print("Invoking launch4j...")
    subprocess.Popen( 
        cmdsplit("java -jar \"%s\" \"%s\""% (
                os.path.join( base_dir,"installer","launch4j","launch4j.jar"),
                os.path.join( base_dir,"installer","launch4j","launch4j.xml"))), 
            cwd=os.path.join(base_dir,"installer","launch4j"),
            bufsize=-1).communicate()
            
    os.unlink( os.path.join( base_dir,"installer","launch4j","launch4j.xml") )
    legacy40r12.finish_exe(os.path.join(base_dir, installer_id+".exe"), os.path.join(base_dir, installer), os.path.join(base_dir,"installer","launch4j"))
  
def readpomversion(pomFile):
	if not os.path.exists(pomFile):
		return ''
		
	dom = parse(pomFile)
	project = dom.getElementsByTagName("project")[0]
	version = str(project.getElementsByTagName("version")[0].firstChild.nodeValue)
	return version
  
def main(mcp_dir):
    print('Using mcp dir: %s' % mcp_dir)
    print('Using base dir: %s' % base_dir)
    
    print("Refreshing dependencies...")
    download_deps( mcp_dir, False )
    
    sys.path.append(mcp_dir)
    sys.path.append(os.path.join(mcp_dir, "runtime"))
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
        print('JRift: %s' % jRiftVer)
        jRiftLibraryVer = readpomversion(jRiftLibraryPom)
        print('JRiftLibrary: %s' % jRiftLibraryVer)
        
    # Update Minecrift version
    minecraft_java_file = os.path.join(mcp_dir,'src','minecraft','net','minecraft','client','Minecraft.java')
    if os.path.exists(minecraft_java_file):
        print("Updating Minecraft.java with Vivecraft version: [Vivecraft %s %s] %s" % ( minecrift_version_num, minecrift_build, minecraft_java_file ) )
        replacelineinfile( minecraft_java_file, "public final String minecriftVerString",     "    public final String minecriftVerString = \"Vivecraft %s %s\";\n" % (minecrift_version_num, minecrift_build) );        

    print("Recompiling...")
    from runtime.mcp import recompile_side, reobfuscate_side
    from runtime.commands import Commands, CLIENT
    commands = Commands(None, verify=True)
    global javac_cmd, json_javac_cmd
    jdk11 = jdk7 = None
    for bindir in commands.findjdks():
        version = commands.javacversion(bindir)
        if version.startswith('11.') and jdk11 is None:
            jdk11 = bindir
        if version.startswith('1.7') and jdk7 is None:
            jdk7 = bindir
    if jdk11:
        javac_cmd = '"%s"' % os.path.join(jdk11, 'javac')
    elif commands.javaversion.startswith('1.8'):
        javac_cmd = commands.cmdjavac
    if jdk7:
        json_javac_cmd = '"%s"' % os.path.join(jdk7, 'javac')
    print('Installer.java: %s | org/json: %s' % (javac_cmd, json_javac_cmd or 'with Installer.java'))
    if not jdk11 or not jdk7:
        print('!! The installer program will not be byte-identical to official builds: they compiled'
              ' Installer.java with JDK 11 and org/json with JDK 7 (Java 6 target). !!')
    legacy40r12.java = (cmdsplit(commands.cmdjavac), cmdsplit(commands.cmdjava))
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
