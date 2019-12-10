#!/bin/bash

adb kill-server

name='avd'
[[ $(docker ps -f "name=$name" --format '{{.Names}}') == $name ]] || docker run -d --rm -p 5555:5555 -p 5554:5554 --name $name --device /dev/kvm swind/android-emulator
