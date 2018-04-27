import psutil
import time
import subprocess

found = False

time.sleep(5)
while 1:
    found = False
    for pid in psutil.pids():
        p = psutil.Process(pid)
        if p.name() == "python" and len(p.cmdline()) > 1 and "automotive_hud.py" in p.cmdline()[1]:
            found = True
            time.sleep(5)
    if found == False:
        try:
            subprocess.call(["python", "/home/pi/Automotive-HUD/Raspberry_Pi_Application/automotive_hud.py"])
        except KeyboardInterrupt:
            print ("quit")
