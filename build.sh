#!/bin/bash
python2 getchanges.py %*
python2 build.py $@
