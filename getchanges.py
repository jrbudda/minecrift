#!/usr/bin/env python

import os, os.path, sys
import shutil, glob, fnmatch
import subprocess, logging, shlex, re
from optparse import OptionParser
from minecriftversion import mcp_version, minecrift_version_num, minecrift_build
from build import replacelineinfile

base_dir = os.path.dirname(os.path.abspath(__file__))

def cmdsplit(args):
    if os.sep == '\\':
        args = args.replace('\\', '\\\\')
    return shlex.split(args)

def create_patch( target_dir, src_file, mod_file, label, patch_file ):
    
    print "Checking patch status for %s..." % src_file
    if os.name == 'nt':
        diff = os.path.abspath(os.path.join(base_dir, 'bin', 'diff.exe'))
    else:
        diff = "diff"

    cmd = cmdsplit('"%s" -u --label "a/%s" "%s" --label "b/%s" "%s"' % (diff, label, src_file, label, mod_file ))

    process = subprocess.Popen(cmd, cwd=target_dir, bufsize=-1, stdout=subprocess.PIPE)
    stdout, stderr = process.communicate()
    if stdout:
        with open( patch_file, 'wb') as out:
            out.write( stdout.replace('\r\n','\n').replace('\r','\n') )

def main(mcp_dir, patch_dir = "patches", orig_dir = ".minecraft_orig"):
    new_src_dir    = os.path.join( base_dir , "src" )
    patch_base_dir = os.path.join( base_dir , patch_dir )
    patchsrc_base_dir = os.path.join( base_dir , "patchsrc" )
    assets_base_dir    = os.path.join(base_dir, "assets", "vivecraft" )

    try:
        shutil.rmtree( new_src_dir )
        shutil.rmtree( patch_base_dir )
        shutil.rmtree( patchsrc_base_dir )
        shutil.rmtree( assets_base_dir )
    except OSError as e:
        pass
    
    if not os.path.exists( new_src_dir ):
        os.mkdir( new_src_dir )
    
    if not os.path.exists( patch_base_dir ):
        os.mkdir( patch_base_dir )

    if not os.path.exists( patchsrc_base_dir ):
        os.mkdir( patchsrc_base_dir )

    if not os.path.exists( assets_base_dir ):
        os.makedirs( assets_base_dir )

    mod_src_dir = os.path.join( mcp_dir , "src", "minecraft" )
    assets_src_dir = os.path.join( mcp_dir , "src", "assets" )
    org_src_dir = os.path.join( mcp_dir , "src", orig_dir )

    for src_dir, dirs, files in os.walk(mod_src_dir):
        pkg       = os.path.relpath(src_dir,mod_src_dir)
        new_dir   = os.path.join( new_src_dir,    pkg )
        mod_dir   = os.path.join( org_src_dir,    pkg )
        patch_dir = os.path.join( patch_base_dir, pkg )
        patchsrc_dir = os.path.join( patchsrc_base_dir, pkg )
        if not os.path.exists(new_dir):
            os.mkdir(new_dir)
        if not os.path.exists(patch_dir):
            os.mkdir(patch_dir)
        if not os.path.exists(patchsrc_dir):
            os.mkdir(patchsrc_dir)
        for file_ in files:
            mod_file = os.path.join(src_dir, file_)
            org_file = os.path.join(mod_dir, file_)

            if mod_file[-4:]!="java":
                continue

            if file_ == "Minecraft.java":
                # Update Minecrift version
                print "Updating Minecraft.java Minecrift version: [Minecrift %s %s] %s" % ( minecrift_version_num, minecrift_build, org_file ) 
                replacelineinfile( mod_file, "public final String minecriftVerString",     "    public final String minecriftVerString = \"Vivecraft %s %s\";\n" % (minecrift_version_num, minecrift_build) );
                
            if os.path.exists(org_file):
                patch_file = os.path.join(patch_dir,file_+".patch")
                label = pkg.replace("\\","/") + "/" + file_ #patch label always has "/"

                create_patch( mcp_dir, org_file, mod_file, label, patch_file )
                if os.path.exists( patch_file ):
                    shutil.copy(mod_file, patchsrc_dir)
            else:
                new_file = os.path.join(new_dir, file_)
                #new class file, just replace
                if os.path.exists( new_file ):
                    os.remove( new_file )
                shutil.copy(mod_file, new_dir)

    for asset_dir, dirs, files in os.walk(assets_src_dir):
        pkg       = os.path.relpath(asset_dir,assets_src_dir)
        new_dir   = os.path.join( assets_base_dir,    pkg )
        if not os.path.exists(new_dir):
            os.mkdir(new_dir)
        for file_ in files:              
            new_file = os.path.join(new_dir, file_)
            mod_file = os.path.join(asset_dir, file_)
            print "Copy asset %s" % (mod_file)

            #new class file, just replace
            if os.path.exists( new_file ):
                os.remove( new_file )
            shutil.copy(mod_file, new_dir)
    
    removeEmptyFolders(patch_base_dir)
    removeEmptyFolders(new_src_dir)
    removeEmptyFolders(patchsrc_base_dir)
    removeEmptyFolders(assets_base_dir)

def removeEmptyFolders(path):
    if not os.path.isdir(path):
        return

    # remove empty subfolders
    files = os.listdir(path)
    if len(files):
        for f in files:
            fullpath = os.path.join(path, f)
            if os.path.isdir(fullpath):
                removeEmptyFolders(fullpath)

    # if folder empty, delete it
    files = os.listdir(path)
    if len(files) == 0:
        os.rmdir(path)

    
if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-m', '--mcp-dir', action='store', dest='mcp_dir', help='Path to MCP to use', default=None)
    parser.add_option('-o', '--orig-dir', action='store', dest='orig_dir', help='Name of original source dir', default=".minecraft_orig")
    parser.add_option('-p', '--patch-dir', action='store', dest='patch_dir', help='Patch dest dir base name to use', default='patches')
    options, _ = parser.parse_args()

    if not options.mcp_dir is None:
        main(os.path.abspath(options.mcp_dir), options.patch_dir, options.orig_dir)
    elif os.path.isfile(os.path.join('..', 'runtime', 'commands.py')):
        main(os.path.abspath('..'), options.patch_dir, options.orig_dir)
    else:
        main(os.path.abspath(mcp_version), options.patch_dir, options.orig_dir)
