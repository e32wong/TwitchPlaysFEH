#/bin/bash

if [ "$#" -ne 1 ]; 
then
	echo "Usage: $0 APKFILE" >&2
	exit 1
else
    adb connect 192.168.1.41:5555
	adb uninstall com.nintendo.zaba
	adb install $1
	adb shell monkey -p com.nintendo.zaba -c android.intent.category.LAUNCHER 1
fi

