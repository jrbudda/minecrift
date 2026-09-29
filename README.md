
Vivecraft for Minecraft 1.7.10
==============

This readme is intended for developers. For downloads and gameplay instructions please see the [official website](http://www.vivecraft.org/)


Using this Repository
========
 
 Vivecraft uses a system of patches to avoid distributing Minecraft code. This complicates the build process a little bit.
 
 - Fork, checkout or download the repo using your Git method of choice.
 - Install Java JDK 1.7 or 1.8 (JDK 1.6 cannot build the current sources, and JDK 9 and newer and the Java JRE will NOT work). Official builds are made with JDK 8, and the build scripts use a JDK 8 when they can find one; build with JDK 8 if you want your build to match a release byte for byte, which makes debugging much easier. If JDK 11 and JDK 7 are also installed, build.py uses them for the installer program itself (Installer.java and its org/json classes), as the official builds did.
 - Set the JAVA_HOME environment variable to the JDK directory
 - Add %JAVA_HOME%\bin to your PATH environment variable
 - Install Python 3 (last built with Python 3.14). Be sure to tick the 'add python to your PATH' option during install. [Download from python.org](https://www.python.org/downloads/)
 - Open a command prompt and navigate to the repo directory
 - Run install.bat
 
The install process (install.py) does a number of things:
 - It downloads MCP (Minecraft coder's pack) and extracts it to the \mcp908\ directory.
 - It downloads a ton of dependencies.
 - It merges Optifine into vanilla minecraft jar.
 - It decompiles and deobfuscates the combined minecraft/optifine into \mcp908\src\.minecraft_orig_nofix\
 - It applies any patches found in \mcppatches\ and copies the result to\mcp908\src\.minecraft_orig\
 - It applies all the patches found in \patches\ and copies the result to \mcp908\src\minecraft\. 
 - It copies all code files found in \src\ to \mcp908\src\minecraft\. This directory is now the full 'Vivecraft' codebase.
 
IF you use Eclipse you can open the workspace found in \mcp908\eclipse. You may have to redirect some build path jar's to the correct location. To run the game form eclipse you have to attach natives to lwjgl jar.

Make all changes to the game in the \mcp908\src\minecraft directory.

To build an installer:
========
 - run getchanges.bat. This compares mcp908\src\minecraft to mcp908\src\minecraft_orig. patches are generated for modified files and copied to \patches\. Whole new files are copied to \src\.
 - run build.bat. This takes the new files and patches and creates a jar. It then uses the code and jsons found in \installer\ to make an installer.exe.

To update code from github
========
  - After pulling changes from github run applychanges.bat. This backs up mcp908\src\minecraft to mcp908\src\minecraft_bak, and starts over by applying all patches in \patches\ to mcp908\src\minecraft_orig, and copies the result o mcp908\src\minecraft
  
 
