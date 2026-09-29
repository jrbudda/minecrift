import os, os.path, sys
import zipfile, urllib.request as urllib2
import platform
import shutil, tempfile, json
import errno
import platform
import shutil
import time
from shutil import move
from tempfile import mkstemp
from os import remove, close
from minecriftversion import mc_version, of_file_name, of_json_name, minecrift_version_num, \
    minecrift_build, of_file_extension, of_file_md5, mcp_version, mc_file_md5, mc_file_url, \
    mcp_download_url, mcp_uses_generics
from hashlib import md5  # pylint: disable-msg=E0611
from optparse import OptionParser
from applychanges import applychanges, apply_patch
from idea import createIdeaProject, removeIdeaProject


base_dir = os.path.dirname(os.path.abspath(__file__))

preferredarch = ''
nomerge = False
nopatch = False
nocompilefixpatch = False
clean = False
force = False
dependenciesOnly = False
includeForge = False

try:
    WindowsError
except NameError:
    WindowsError = OSError

def osArch():
    if platform.machine().endswith('64'):
        return '64'
    else:
        return '32'

def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc: # Python >2.5
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else: raise

#Helpers taken from forge mod loader, https://github.com/MinecraftForge/FML/blob/master/install/fml.py
def get_md5(file):
    if not os.path.isfile(file):
        return ""
    with open(file, 'rb') as fh:
        return md5(fh.read()).hexdigest()

def download_file(url, target, md5=None):
    name = os.path.basename(target)
    download = True
    if not is_non_zero_file(target):
        if os.path.isfile(target):
            os.remove(target)
        download = True
    elif not md5 == None and not md5 == "":
        if not get_md5(target) == md5:
            print('File Exists but bad MD5!: %s [MD5:%s]' % ( os.path.basename(target), get_md5(target) ))
            os.remove(target)
            download = True
        else:
            print('File Exists: %s [MD5:%s]' % ( os.path.basename(target), get_md5(target) ))
            download = False
    else:
        print('File Exists: %s' % os.path.basename(target))
        download = False 
        
    if download is True:
        print('Downloading: %s' % os.path.basename(target))
        try:
            with open(target,"wb") as tf:
                res = urllib2.urlopen(urllib2.Request( url, headers = {"User-Agent":"Mozilla/5.0"}))
                tf.write( res.read() )
            if not md5 == None and not md5 == "":
                if not get_md5(target) == md5:
                    print('Download of %s failed md5 check, deleting' % name)
                    os.remove(target)
                    return False
        except Exception as e:
            print(e)
            print('Download of %s failed, download it manually from \'%s\' to \'%s\'' % (target, url, target))
            if os.path.isfile(target):
                os.remove(target)
            return False

    return True

def download_native(url, folder, name):
    if not os.path.exists(folder):
        os.makedirs(folder)

    target = os.path.join(folder, name)
    if not download_file(url, target):
        return False

    return True

def is_non_zero_file(fpath):  
    return True if os.path.isfile(fpath) and os.path.getsize(fpath) > 0 else False

def installAndPatchMcp( mcp_dir ):

    mcp_exists = True
    if not os.path.exists(mcp_dir+"/runtime/commands.py"):
        mcp_exists = False
        mcp_zip_file = os.path.join( base_dir,mcp_version+".zip" )
        print( "Checking for mcp zip file: %s" % mcp_zip_file )
        if not os.path.exists( mcp_zip_file ) and mcp_download_url:
            # Attempt download
            download_file( mcp_download_url, mcp_zip_file )

        if os.path.exists( mcp_zip_file ):
            if not os.path.exists( mcp_dir ):
                os.mkdir( mcp_dir )
            mcp_zip = zipfile.ZipFile( mcp_zip_file )
            mcp_zip.extractall( mcp_dir )
            import stat
            astyle = os.path.join(mcp_dir,"runtime","bin","astyle-osx")
            st = os.stat( astyle )
            os.chmod(astyle, st.st_mode | stat.S_IEXEC)
            mcp_exists = True

    if mcp_exists == False:
        print("No %s directory or zip file found. Please copy the %s.zip file into %s and re-run the command." % (mcp_version, mcp_version, base_dir))
        exit(1)
    
    # Patch mcp.cfg for additional mem
    print("Patching mcp.cfg. Ignore \"FAILED\" hunks")
    mcp_cfg_patch_file = os.path.join("mcppatches", "mcp.cfg.patch")
    if os.path.exists(mcp_cfg_patch_file):
        apply_patch( mcp_dir, mcp_cfg_patch_file, os.path.join(mcp_dir,"conf"))
    
    # Patch mcp.cfg with minecraft jar md5
    mcp_cfg_file = os.path.join(mcp_dir,"conf","mcp.cfg")
    if os.path.exists(mcp_cfg_file):
        replacelineinfile( mcp_cfg_file, "MD5Client  =", "MD5Client  = %s\n" % mc_file_md5, True );   # Multiple 'MD5Client' entries - hack to get first one currently
        #replacelineinfile( mcp_cfg_file, "MD5Server  =", "MD5Server  = %s\n" % mc_server_file_md5, True );

    # patch joined.srg if necessary
    mcp_joined_srg = os.path.join(mcp_dir,"conf","joined.srg")
    patch_joined_srg = os.path.join(base_dir,"mcppatches","joined.srg")
    if os.path.exists(patch_joined_srg):
        print('Updating joined.srg: copying %s to %s' % (patch_joined_srg, mcp_joined_srg))
        shutil.copy(patch_joined_srg,mcp_joined_srg)

    # Patch fffix.py
    fffix_patch_path = os.path.join(base_dir, "mcppatches", "fffix.py.patch")
    if os.path.exists(fffix_patch_path):
        print("Patching fffix.py. Ignore \"FAILED\" hunks")
        apply_patch( mcp_dir, os.path.join("mcppatches", "fffix.py.patch"), os.path.join(mcp_dir,"runtime","pylibs"))

    # Port MCP's own scripts to Python 3
    mcp_py3_patch = os.path.join(base_dir, "mcppatches", mcp_version + "-py3.patch")
    if os.path.exists(mcp_py3_patch):
        print('Patching MCP scripts for Python 3: %s' % mcp_py3_patch)
        failed = apply_source_patch(mcp_py3_patch, mcp_dir)
        if failed:
            print("ERROR: %s does not apply to this MCP (%s); extract a fresh %s.zip" % (mcp_py3_patch, ", ".join(failed), mcp_version))
            sys.exit(1)

    # Use fixed fernflower.jar
    ff_jar_source_path = os.path.join(base_dir, "mcppatches", "fernflower-opt-fix.jar")
    ff_jar_dest_path = os.path.join(mcp_dir,"runtime","bin","fernflower.jar")
    if os.path.exists(ff_jar_source_path):
        print('Updating fernflower.jar: copying %s to %s' % (ff_jar_source_path, ff_jar_dest_path))
        shutil.copy(ff_jar_source_path,ff_jar_dest_path)

    # Patch Start.java with minecraft version
    start_java_file = os.path.join(base_dir,"mcppatches","Start.java")
    if os.path.exists(start_java_file):
        target_start_java_file = os.path.join(mcp_dir,"conf","patches","Start.java")
        print('Updating Start.java: copying %s to %s' % (start_java_file, target_start_java_file))
        shutil.copy(start_java_file,target_start_java_file)
        replacelineinfile( target_start_java_file, "args = concat(new String[] {\"--version\", \"mcp\"", "        args = concat(new String[] {\"--version\", \"mcp\", \"--accessToken\", \"0\", \"--assetsDir\", \"assets\", \"--assetIndex\", \"%s\", \"--userProperties\", \"{}\"}, args);\n" % mc_version );
    
    # Setup the appropriate mcp file versions
    mcp_version_cfg = os.path.join(mcp_dir,"conf","version.cfg")
    replacelineinfile( mcp_version_cfg, "ClientVersion =", "ClientVersion = %s\n" % mc_version );
    replacelineinfile( mcp_version_cfg, "ServerVersion =", "ServerVersion = %s\n" % mc_version );

    # Patch in mcp mappings (if present)
    params_csv_source = os.path.join(base_dir,"mcppatches","mappings","params.csv")
    params_csv_dest = os.path.join(mcp_dir,"conf","params.csv")
    if os.path.exists(params_csv_source):
        shutil.copy(params_csv_source,params_csv_dest)

    methods_csv_source = os.path.join(base_dir,"mcppatches","mappings","methods.csv")
    methods_csv_dest = os.path.join(mcp_dir,"conf","methods.csv")
    if os.path.exists(methods_csv_source):
        shutil.copy(methods_csv_source,methods_csv_dest)

    fields_csv_source = os.path.join(base_dir,"mcppatches","mappings","fields.csv")
    fields_csv_dest = os.path.join(mcp_dir,"conf","fields.csv")
    if os.path.exists(fields_csv_source):
        shutil.copy(fields_csv_source,fields_csv_dest)


def download_deps( mcp_dir, download_mc, forgedep=False ):

    jars = os.path.join(mcp_dir,"jars")

    versions =  os.path.join(jars,"versions",mc_version)
    mkdir_p( versions )

    if sys.platform == 'darwin':
        native = "osx"
    elif sys.platform == "linux":
        native = "linux"
    elif sys.platform == "linux2":
        native = "linux"
    else:
        native = "windows"

    if not forgedep:
        flat_lib_dir = os.path.join(base_dir,"lib",mc_version)
        flat_native_dir = os.path.join(base_dir,"lib",mc_version,"natives",native)
    else:
        flat_lib_dir = os.path.join(base_dir,"lib",mc_version+"-forge")
        flat_native_dir = os.path.join(base_dir,"lib",mc_version+"-forge","natives",native)

    mkdir_p( flat_lib_dir )
    mkdir_p( flat_native_dir )
     
    # Get minecrift json file
    json_file = os.path.join(versions,mc_version+".json")
    if forgedep is False:
        source_json_file = os.path.join("installer",mc_version+".json")
        print('Updating json: copying %s to %s' % (source_json_file, json_file))
        shutil.copy(source_json_file,json_file)
    else:
        source_json_file = os.path.join("installer",mc_version+"-forge.json")
    
    # Use optifine json name for destination dir and jar names
    optifine_dest_dir = os.path.join(jars,"libraries","optifine","OptiFine",of_json_name )
    mkdir_p( optifine_dest_dir )

    print('Checking Optifine...')
    optifine_jar = "OptiFine-"+of_json_name+".jar"
    optifine_dest_file = os.path.join( optifine_dest_dir, optifine_jar )
 
    download_optifine = False
    optifine_md5 = ''
    if not is_non_zero_file( optifine_dest_file ):
        download_optifine = True
    else:
        optifine_md5 = get_md5( optifine_dest_file )
        print('Optifine md5: %s' % optifine_md5)
        if optifine_md5 != of_file_md5:
            download_optifine = True
            print('Bad MD5!')
        else:
            print('MD5 good!')
    
    if download_optifine: 
        # Use optifine filename for URL
        optifine_url = "http://optifine.net/download.php?f=OptiFine_"+of_file_name+of_file_extension
        print('Downloading Optifine...')
        if not download_file( optifine_url, optifine_dest_file, of_file_md5 ):
            print('FAILED to download Optifine!')
            sys.exit(1)
        else:
            shutil.copy(optifine_dest_file,os.path.join(flat_lib_dir, os.path.basename(optifine_dest_file)))
            
    if of_file_md5 == "":
        optifine_md5 = get_md5( optifine_dest_file )
        print('Optifine md5: %s' % optifine_md5)
        sys.exit(0)

    json_obj = []
    with open(source_json_file,"rb") as f:
        #data=f.read()
        #print 'JSON File:\n%s' % data
        json_obj = json.load( f )
    try:
        newlibs = []
        for lib in json_obj['libraries']:
            libname = lib["name"]
            skip = False
            if "rules" in  lib:
                for rule in lib["rules"]:
                    if "action" in rule and rule["action"] == "allow" and "os" in rule:
                        skip = True
                        for entry in rule["os"]:
                            if "name" in entry:
                                if rule["os"]["name"] == native:
                                    skip = False

            if skip:
                print('File: %s\nSkipping due to rules' % libname)
                continue
                
            group,artifact,version = lib["name"].split(":")
            if "url" in lib:
                repo = lib["url"]
            else:
                repo = "https://libraries.minecraft.net/"

            if "natives" in lib:
                url = group.replace(".","/")+ "/"+artifact+"/"+version +"/"+artifact+"-"+version+"-"+lib["natives"][native]+".jar"
            else:
                url = group.replace(".","/")+ "/"+artifact+"/"+version +"/"+artifact+"-"+version+".jar"

            index = url.find('${arch}')
            if index > -1:
                # Get both 32 and 64 bit versions
                url32 = url.replace('${arch}', '32')
                file32 = os.path.join(jars,"libraries",url32.replace("/",os.sep))
                mkdir_p(os.path.dirname(file32))
                download_file( repo + url32, file32 )
                shutil.copy(file32,os.path.join(flat_lib_dir, os.path.basename(file32)))
                
                url64 = url.replace('${arch}', '64')
                file64 = os.path.join(jars,"libraries",url64.replace("/",os.sep))
                mkdir_p(os.path.dirname(file64))
                download_file(repo + url64, file64)
                shutil.copy(file64,os.path.join(flat_lib_dir, os.path.basename(file64)))                

                # Use preferred architecture to choose which natives to extract.
                if preferredarch == '32':
                    print('    Using preferred arch 32bit')
                    extractnatives( lib, jars, file32, flat_native_dir )
                else:
                    print('    Using preferred arch 64bit')
                    extractnatives( lib, jars, file64, flat_native_dir )                
               
            else:
                file = os.path.join(jars,"libraries",url.replace("/",os.sep))
                mkdir_p(os.path.dirname(file))
                if download_file( repo + url, file ) == True:
                    shutil.copy(file,os.path.join(flat_lib_dir, os.path.basename(file)))  
                    extractnatives( lib, jars, file, flat_native_dir )
                
            newlibs.append( lib )
        json_obj['libraries'] = newlibs
        if forgedep is False:
            with open(json_file,"w+", newline='') as f:
                json.dump( json_obj,f, indent=1 )
    except Exception as e:
        print('ERROR: %s' % e)
        raise

    if download_mc == True:
        jar_file = os.path.join(versions,mc_version+".jar")
        jar_url = mc_file_url
        download_file( jar_url, jar_file, mc_file_md5 )
        print('Used url: ' + jar_url)
        shutil.copy(jar_file,os.path.join(flat_lib_dir, os.path.basename(jar_file))) 
        
        if mc_file_md5 == "":
            mc_md5 = get_md5( jar_file )
            print('%s md5: %s' % ( os.path.basename(jar_file), mc_md5 ))
            sys.exit(0)	

def extractnatives( lib, jars, file, copydestdir ):
    if "natives" in lib:
        folder = os.path.join(jars,"versions",mc_version,mc_version+"-natives")
        mkdir_p(folder)
        zip = zipfile.ZipFile(file)
        #print 'Native extraction: folder: %s, file to unzip: %s' % (folder, file)
        for name in zip.namelist():
            if not name.startswith('META-INF') and not name.endswith('/'):
                out_file = os.path.join(folder, name)
                print('    Extracting native library %s' % name)
                out = open(out_file, 'wb')
                out.write(zip.read(name))
                out.flush()
                out.close()
                shutil.copy(out_file,os.path.join(copydestdir, os.path.basename(out_file))) 

def _py2_str_hash(name):
    # Python 2.7 str hash (hash randomization off) with a 32-bit C long, as in
    # the 32-bit Python 2.7 that ships with MCP (runtime/bin/python/python_mcp.exe).
    b = name.encode('utf-8')
    if not b:
        return 0
    x = (b[0] << 7) & 0xFFFFFFFF
    for c in b:
        x = ((1000003 * x) ^ c) & 0xFFFFFFFF
    x ^= len(b) & 0xFFFFFFFF
    if x == 0xFFFFFFFF:  # -1 is reserved for errors
        x = 0xFFFFFFFE
    return x

def _py2_set_order(names):
    # Order in which Python 2.7 iterates set(names). Python 3 randomizes string
    # hashes per run, so iterating a real set would give a different jar order on
    # every install, and MCInjector/fernflower output (which the patches target)
    # depends on that order.
    size, table, used = 8, [None] * 8, 0
    def insert(tbl, name, h):
        mask = len(tbl) - 1
        i = h & mask
        perturb = h
        while tbl[i & mask] is not None:
            i = ((i << 2) + i + perturb + 1) & 0xFFFFFFFF
            perturb >>= 5
        tbl[i & mask] = (name, h)
    for name in names:
        insert(table, name, _py2_str_hash(name))
        used += 1
        if used * 3 >= len(table) * 2:
            minused = used * 2 if used > 50000 else used * 4
            size = 8
            while size <= minused:
                size <<= 1
            old, table = table, [None] * size
            for entry in old:
                if entry is not None:
                    insert(table, entry[0], entry[1])
    return [entry[0] for entry in table if entry is not None]

def zipmerge( target_file, source_file ):
    out_file, out_filename = tempfile.mkstemp()
    out = zipfile.ZipFile(out_filename,'a')
    try:
        target = zipfile.ZipFile( target_file, 'r')
    except Exception as e:
        print('zipmerge: target not a zip-file: %s' % target_file)
        raise

    try:        
        source = zipfile.ZipFile( source_file, 'r' )
    except Exception as e:
        print('zipmerge: source not a zip-file: %s' % source_file)
        raise
        
    #source supersedes target (in the same order Python 2.7 iterated these sets)
    source_files = _py2_set_order( source.namelist() )
    source_set = set( source_files )
    target_files = _py2_set_order( [f for f in _py2_set_order( target.namelist() ) if f not in source_set] )

    for file in source_files:
        out.writestr( file, source.open( file ).read() )

    for file in target_files:
        out.writestr( file, target.open( file ).read() )

    source.close()
    target.close()
    out.close()
    os.remove( target_file )
    shutil.copy( out_filename, target_file )


def symlink(source, link_name):
    import os
    os_symlink = getattr(os, "symlink", None)
    if callable(os_symlink):
        try:
            os_symlink(source, link_name)
        except Exception:
            pass
    else:
        import ctypes
        csl = ctypes.windll.kernel32.CreateSymbolicLinkW
        csl.argtypes = (ctypes.c_wchar_p, ctypes.c_wchar_p, ctypes.c_uint32)
        csl.restype = ctypes.c_ubyte
        flags = 1 if os.path.isdir(source) else 0
        if csl(link_name, source, flags) == 0:
            raise ctypes.WinError()

def osArch():
    if platform.machine().endswith('64'):
        return '64'
    else:
        return '32'

def is32bitPreferred():
    if preferredarch == '32':
        return True

    return False

# MCP 9.08's fernflower is not deterministic: the same input jar occasionally
# decompiles a few methods in a different (equivalent) form, and the patches
# below then fail to apply. These three reject files come from MCP's own
# fernflower patches and are produced identically by every good decompile; any
# other reject, or different reject contents, means a bad decompile. Hashes are
# of the reject file with CRs removed.
EXPECTED_REJECTS = {
    "net/minecraft/client/renderer/RenderBlocks.#": "87518fe5bbf2",
    "net/minecraft/client/renderer/RenderGlobal.#": "50987f208ff0",
    "net/minecraft/client/renderer/texture/TextureManage#": "d665d382ffa8",
}
MAX_DECOMPILE_ATTEMPTS = 10

def hunk_text(hunk, eol):
    # old and new text of a hunk; "\ No newline at end of file" drops the final newline
    # on the side(s) of the line before it
    old, new, old_eol, new_eol = [], [], eol, eol
    for i, l in enumerate(hunk):
        if l[:1] == "\\":
            prev = hunk[i - 1][:1]
            if prev in (" ", "-"):
                old_eol = ""
            if prev in (" ", "+"):
                new_eol = ""
        elif l[:1] in (" ", "-"):
            old.append(l[1:])
            if l[:1] == " ":
                new.append(l[1:])
        else:
            new.append(l[1:])
    return eol.join(old) + old_eol, eol.join(new) + new_eol

def apply_source_patch(patch_file, target_dir):
    # Apply a unified diff by content instead of line numbers: a hunk is applied when its
    # old lines are present and skipped when its new lines already are, so applying twice
    # is harmless. Returns the files with hunks that match neither way.
    with open(patch_file, "r") as fh:
        lines = fh.read().splitlines()
    patches, i = [], 0
    while i < len(lines):
        if lines[i].startswith("+++ "):
            new_file = lines[i - 1].startswith("--- /dev/null")
            target = lines[i][4:].split("\t")[0]
            target = target[2:] if target.startswith("b/") else target
            hunks, i = [], i + 1
            while i < len(lines) and not lines[i].startswith(("diff ", "--- ")):
                if lines[i].startswith("@@"):
                    hunks.append([])
                elif hunks and lines[i][:1] in (" ", "-", "+", "\\"):
                    hunks[-1].append(lines[i])
                i += 1
            patches.append((target, new_file, hunks))
        else:
            i += 1
    failed = []
    for target, new_file, hunks in patches:
        path = os.path.join(target_dir, target)
        if new_file:
            content = hunk_text([l for h in hunks for l in h], "\n")[1]
            if not os.path.exists(path):
                mkdir_p(os.path.dirname(path))
                with open(path, "w", newline="") as fh:
                    fh.write(content)
            continue
        with open(path, "r", newline="") as fh:
            text = fh.read()
        eol = "\r\n" if "\r\n" in text else "\n"
        for hunk in hunks:
            old, new = hunk_text(hunk, eol)
            if text.count(old) == 1:
                text = text.replace(old, new)
            elif new not in text:
                failed.append(target)
        with open(path, "w", newline="") as fh:
            fh.write(text)
    return sorted(set(failed))

def normalize_decompile(src_dir):
    # fernflower also picks between equivalent forms of some methods, so builds are not
    # byte-identical to the official one (and some forms make the patches fail). Each file
    # in mcppatches/normalize is a single-hunk patch from one such form to the form the
    # official builds were made from; alternatives for the same spot simply don't match.
    # expected.sha1 lists the resulting files, to report forms nobody has seen yet.
    import hashlib
    norm_dir = os.path.join(base_dir, "mcppatches", "normalize")
    if not os.path.isdir(norm_dir):
        return
    for path, _, files in os.walk(norm_dir):
        for f in sorted(files):
            if f.endswith(".patch"):
                apply_source_patch(os.path.join(path, f), src_dir)
    with open(os.path.join(norm_dir, "expected.sha1"), "r") as fh:
        for line in fh:
            digest, target = line.split()
            with open(os.path.join(src_dir, target), "rb") as jf:
                if hashlib.sha1(jf.read().replace(b"\r", b"")).hexdigest()[:12] != digest:
                    print("WARNING: unknown decompile of %s; the build will not be byte-identical to official builds" % target)

def unexpected_rejects(src_dir):
    import hashlib
    found = []
    for path, _, files in os.walk(src_dir):
        for f in files:
            if f.endswith("#"):
                full = os.path.join(path, f)
                rel = os.path.relpath(full, src_dir).replace(os.sep, "/")
                with open(full, "rb") as fh:
                    digest = hashlib.sha1(fh.read().replace(b"\r", b"")).hexdigest()[:12]
                if EXPECTED_REJECTS.get(rel) != digest:
                    found.append(rel)
    return found

def main(mcp_dir, attempt=1):
    print('Using base dir: %s' % base_dir)
    print('Using mcp dir: %s (use -m <mcp-dir> to change)' % mcp_dir)
    print('Preferred architecture: %sbit - preferring %sbit native extraction (use -a 32 or -a 64 to change)' % (preferredarch, preferredarch))
    if dependenciesOnly:
        print('Downloading dependencies ONLY')
    else:
        if nomerge is True:
            print('NO Optifine merging')
        if nocompilefixpatch is True:
            print('SKIPPING Apply compile fix patches')
        if nopatch is True:
            print('SKIPPING Apply Minecrift patches')
        
    if clean == True and attempt == 1:
        print('Cleaning...')
        if force == False:
            print('')
            print('WARNING:')
            print('The clean option will delete all folders created by MCP, including the')
            print('src folder which may contain changes you made to the code, along with any')
            print('saved worlds from the client or server.')
            print('Minecrift downloaded dependencies will also be removed and re-downloaded.')
            print('Patches will be left alone however.')
            answer = input('If you really want to clean up, enter "Yes" ')
            if answer.lower() not in ['yes']:
                print('You have not entered "Yes", aborting the clean up process')
                sys.exit(1)        
        print('Cleaning mcp dir...')
        reallyrmtree(mcp_dir)
        print('Cleaning lib dir...')
        reallyrmtree(os.path.join(base_dir,'lib'))
        print('Cleaning patchsrc dir...')
        reallyrmtree(os.path.join(base_dir,'patchsrc'))
        print('Removing idea project files...')
        removeIdeaProject(base_dir)

    print('Installing mcp...')
    installAndPatchMcp(mcp_dir)

    print("\nDownloading dependencies...")
    if includeForge:
        download_deps( mcp_dir, attempt == 1, True ) # Forge libs

    download_deps( mcp_dir, attempt == 1, False ) # Vanilla libs
    if dependenciesOnly:
        sys.exit(1)

    if nomerge == False and attempt == 1:
        print("Applying Optifine...")
        optifine = os.path.join(mcp_dir,"jars","libraries","optifine","OptiFine",of_json_name,"OptiFine-"+of_json_name+".jar" )
        minecraft_jar = os.path.join( mcp_dir,"jars","versions",mc_version,mc_version+".jar")
        print(' Merging\n  %s\n into\n  %s' % (optifine, minecraft_jar))
        zipmerge( minecraft_jar, optifine )
    else:
        print("Skipping Optifine merge!")
    
    print("Decompiling...")
    src_dir = os.path.join(mcp_dir, "src","minecraft")
    if os.path.exists( src_dir ):
        shutil.rmtree( src_dir, True )
    sys.path.append(mcp_dir)
    sys.path.append(os.path.join(mcp_dir, "runtime"))
    os.chdir(mcp_dir)
    from runtime.decompile import decompile

    # This *has* to sync with the default options used in <mcpdir>/runtime/decompile.py for
    # the currently used version of MCP
    
    decompile(conffile=None,      # -c
              force_jad=False,    # -j
              force_csv=False,    # -s
              no_recompile=False, # -r
              no_comments=False,  # -d
              no_reformat=False,  # -a
              no_renamer=False,   # -n
              no_patch=False,     # -p
              only_patch=False,   # -o
              keep_lvt=False,     # -l
              keep_generics=mcp_uses_generics, # -g, True for MCP 1.8.8+, False otherwise
              only_client=True,   # --client
              only_server=False,  # --server
              force_rg=False,     # --rg
              workdir=None,       # -w
              json=None,          # --json
              nocopy=True         # --nocopy
              )

    os.chdir( base_dir )

    normalize_decompile(src_dir)

    # Create original nofix decompile src dir
    org_no_fix_src_dir = os.path.join(mcp_dir, "src",".minecraft_orig_nofix")
    if os.path.exists( org_no_fix_src_dir ):
        shutil.rmtree( org_no_fix_src_dir, True )
    shutil.copytree( src_dir, org_no_fix_src_dir )

    if nocompilefixpatch == False:
        compile_error_patching_done = False
        
        # Patch stage 1: apply only the patches needed to correct the
        # optifine merge decompile errors
        mcp_patch_dir = os.path.join( base_dir, "mcppatches", "patches" )
        if os.path.exists( mcp_patch_dir ):
            print("Patching Optifine merge decompile errors...")
            applychanges( mcp_dir, patch_dir="mcppatches/patches", backup=False, copyOriginal=False, mergeInNew=False )
            compile_error_patching_done = True
        
        # Address problem files - copy over directly
        problem_file_dir = os.path.join( base_dir, "mcppatches", "problemfiles" )
        if os.path.exists( problem_file_dir ):
            print("Addressing problem files...")        
            xp_problem_file = os.path.join(problem_file_dir, "xp.java")
            shutil.copy( xp_problem_file, os.path.join( mcp_dir, "src", "minecraft", "net", "minecraft", "src", "xp.java" ) )
            chunkrenderdispatcher_problem_file = os.path.join(problem_file_dir, "ChunkRenderDispatcher.java")
            shutil.copy( chunkrenderdispatcher_problem_file, os.path.join( mcp_dir, "src", "minecraft", "net", "minecraft", "client", "renderer", "chunk", "ChunkRenderDispatcher.java" ) )
            compile_error_patching_done = True

        # Update the client md5
        if compile_error_patching_done == True:
            print("Updating client.md5...")
            os.chdir(mcp_dir)
            from runtime.updatemd5 import updatemd5
            updatemd5( None, True, True, False )
            os.chdir( base_dir )

            
    # Create original (fixed) decompile src dir       
    org_src_dir = os.path.join(mcp_dir, "src",".minecraft_orig")
    if os.path.exists( org_src_dir ):
        shutil.rmtree( org_src_dir, True )
    shutil.copytree( src_dir, org_src_dir )                
                
    if nopatch == False:
        # Patch stage 2: Now apply our main Minecrift patches, only
        # changes needed for Minecrift functionality
        print("Applying full Minecrift patches...")
        applychanges( mcp_dir )
    else:
        print("Apply patches skipped!")

    # create idea project if it doesn't already exist
    if not os.path.exists(os.path.join(base_dir, '.idea')):
        print("Creating idea project...")
        createIdeaProject(base_dir, mc_version, os.path.basename(mcp_dir), is32bitPreferred())

    rejects = unexpected_rejects(src_dir)
    if rejects:
        print("Patches did not apply cleanly (decompile attempt %d of %d): %s" % (attempt, MAX_DECOMPILE_ATTEMPTS, ", ".join(rejects)))
        if attempt < MAX_DECOMPILE_ATTEMPTS:
            print("Fernflower produced a different decompile, retrying...")
            main(mcp_dir, attempt + 1)
        else:
            print("ERROR: giving up after %d decompile attempts" % MAX_DECOMPILE_ATTEMPTS)
            sys.exit(1)


def reallyrmtree(path):
    if not sys.platform.startswith('win'):
        if os.path.exists(path):
            shutil.rmtree(path)
    else:
        i = 0
        try:
            while os.stat(path) and i < 20:
                shutil.rmtree(path, onerror=rmtree_onerror)
                i += 1
        except OSError:
            pass

        # raise OSError if the path still exists even after trying really hard
        try:
            os.stat(path)
        except OSError:
            pass
        else:
            raise OSError(errno.EPERM, "Failed to remove: '" + path + "'", path)

def rmtree_onerror(func, path, _):
    if not os.access(path, os.W_OK):
        os.chmod(path, stat.S_IWUSR)
    time.sleep(0.5)
    try:
        func(path)
    except OSError:
        pass
        
def replacelineinfile(file_path, pattern, subst, firstmatchonly=False):
    #Create temp file
    fh, abs_path = mkstemp()
    new_file = open(abs_path,'w', newline='')
    old_file = open(file_path,'r', newline='')
    hit = False
    for line in old_file:
        if pattern in line and not (firstmatchonly == True and hit == True): 
            new_file.write(subst)
            hit = True
        else:
            new_file.write(line)
    #close temp file
    new_file.close()
    close(fh)
    old_file.close()
    #Remove original file
    remove(file_path)
    #Move new file
    move(abs_path, file_path)
    
if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-o', '--no-optifine', dest='nomerge', default=False, action='store_true', help='If specified, no optifine merge will be carried out')
    parser.add_option('-c', '--clean', dest='clean', default=False, action='store_true', help='Cleans the mcp dir, and REMOVES ALL SOURCE IN THE MCPxxx/SRC dir. Re-downloads dependencies')
    parser.add_option('-f', '--force', dest='force', default=False, action='store_true', help='Forces any changes without prompts')
    parser.add_option('-d', '--dependenciesOnly', dest='dep', default=False, action='store_true', help='Gets the dependencies only - no merge, compile or apply changes are performed.')
    parser.add_option('-n', '--no-patch', dest='nopatch', default=False, action='store_true', help='If specified, no patches will be applied at the end of installation')
    parser.add_option('-x', '--no-fix-patch', dest='nocompilefixpatch', default=False, action='store_true', help='If specified, no compile fix patches will be applied at the end of installation')
    parser.add_option('-m', '--mcp-dir', action='store', dest='mcp_dir', help='Path to MCP to use', default=None)
    parser.add_option('-a', '--architecture', action='store', dest='arch', help='Architecture to use (\'32\' or \'64\'); prefer 32 or 64bit dlls', default=None)
    parser.add_option('-i', '--includeForge', dest='includeForge', default=False, action='store_true', help='Also include download of Forge dependencies')
    options, _ = parser.parse_args()

    if not options.arch is None:
        if options.arch == '32':
            preferredarch = '32'
        elif options.arch == '64':
            preferredarch = '64'
            
    if preferredarch == '':
        preferredarch = osArch()

        
    nomerge = options.nomerge
    nopatch = options.nopatch
    nocompilefixpatch = options.nocompilefixpatch
    clean = options.clean
    force = options.force
    dependenciesOnly = options.dep
    
    if not options.mcp_dir is None:
        main(os.path.abspath(options.mcp_dir))
    elif os.path.isfile(os.path.join('..', 'runtime', 'commands.py')):
        main(os.path.abspath('..'))
    else:
        main(os.path.abspath(mcp_version))
