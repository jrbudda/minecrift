import os, os.path, sys, tempfile, re
import shutil, fnmatch
import subprocess, shlex
from optparse import OptionParser
from minecriftversion import mc_version, minecrift_version_num, minecrift_build, of_file_extension, of_file_md5, mcp_version

base_dir = os.path.dirname(os.path.abspath(__file__))

#Helpers taken from forge mod loader, https://github.com/MinecraftForge/FML/blob/master/install/fml.py
def cmdsplit(args):
    if os.sep == '\\':
        args = args.replace('\\', '\\\\')
    return shlex.split(args)

crlf = re.compile(r"\n(?<!\r)")
def apply_patch( mcp_dir, patch_file, target_dir ):
    if os.name == 'nt':
        with tempfile.NamedTemporaryFile(delete=False) as temp_file:
            with open(patch_file,'rb') as patch:
                temp_file.write( crlf.sub("\r\n", patch.read() ))
            patch_file = temp_file.name
        applydiff = os.path.abspath(os.path.join(mcp_dir, 'runtime', 'bin', 'applydiff.exe'))
        cmd = cmdsplit('"%s" -N -uf -p1 -i "%s"' % (applydiff, patch_file ))
    else:
        with tempfile.NamedTemporaryFile(delete=False) as temp_file:
            with open(patch_file,'rb') as patch:
                temp_file.write( patch.read())
            patch_file = temp_file.name
        cmd = cmdsplit('patch -N -p1 -i "%s" ' % patch_file )

    process = subprocess.Popen(cmd, cwd=target_dir, bufsize=-1)
    process.communicate()

    os.unlink(patch_file)

def apply_patches(mcp_dir, patch_dir, target_dir, find=None, rep=None):
    for path, _, filelist in os.walk(patch_dir, followlinks=True):
        for cur_file in fnmatch.filter(filelist, '*.patch'):
            patch_file = os.path.normpath(os.path.join(patch_dir, path[len(patch_dir)+1:], cur_file))
            apply_patch( mcp_dir, patch_file, target_dir )

def merge_tree(root_src_dir, root_dst_dir):
    for src_dir, dirs, files in os.walk(root_src_dir):
        dst_dir = src_dir.replace(root_src_dir, root_dst_dir)
        if not os.path.exists(dst_dir):
            os.mkdir(dst_dir)
        for file_ in files:
            src_file = os.path.join(src_dir, file_)
            dst_file = os.path.join(dst_dir, file_)
            print("Copying file %s" % src_file.replace(root_src_dir+"/","") )
            if os.path.exists(dst_file):
                os.remove(dst_file)
            shutil.copy(src_file, dst_dir)


def applychanges(mcp_dir, patch_dir = "patches", applyPatches=True, backup = True, copyOriginal=True, origDir='.minecraft_orig', mergeInNew=True ):
    print("Applying Changes...")

    mod_src_dir = os.path.join(mcp_dir, "src","minecraft")
    mod_bak_dir = os.path.join(mcp_dir, "src","minecraft-bak")
    org_src_dir = os.path.join(mcp_dir, "src",origDir)
    mod_ass_dir = os.path.join(mcp_dir, "src","assets")

    if backup and os.path.exists(mod_src_dir):
        print("Backing up src/minecraft to src/minecraft-bak")
        shutil.rmtree( mod_bak_dir, True )
        shutil.move( mod_src_dir, mod_bak_dir )
        
    if copyOriginal:
        shutil.copytree( org_src_dir, mod_src_dir, ignore=lambda p,f: [".git"]  )

    if applyPatches:
        #apply patches
        apply_patches( mcp_dir, os.path.join( base_dir, patch_dir ), mod_src_dir )
        
    if mergeInNew:
        #merge in the new classes
        merge_tree( os.path.join( base_dir, "src" ), mod_src_dir )
        merge_tree( os.path.join( base_dir, "assets", "vivecraft"), mod_ass_dir )

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-m', '--mcp-dir', action='store', dest='mcp_dir', help='Path to MCP to use', default=None)
    parser.add_option('-p', '--patch-dir', action='store', dest='patch_dir', help='Path to base patch dir', default='patches')    
    options, _ = parser.parse_args()

    if not options.mcp_dir is None:
        applychanges(os.path.abspath(options.mcp_dir), options.patch_dir)
    elif os.path.isfile(os.path.join('..', 'runtime', 'commands.py')):
        applychanges(os.path.abspath('..'), options.patch_dir)
    else:
        applychanges(os.path.abspath(mcp_version), options.patch_dir)
