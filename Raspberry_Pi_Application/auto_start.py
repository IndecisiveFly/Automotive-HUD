import psutil
import time
import subprocess

found = False

time.sleep(5)
#turn on bluetooth discoverable and accept connection
subprocess.call(["sudo", "hciconfig", "hci0", "sspmode", "1"])
subprocess.call(["sudo", "hciconfig", "hci0", "piscan"])

time.sleep(5)
#check if automotive_hud is running. run if not found
while 1:
    found = False
    for pid in psutil.pids():
        p = psutil.Process(pid)
        if p.name() == "python" and len(p.cmdline()) > 1 and "automotive_hud.py" in p.cmdline()[1]:
            found = True
            time.sleep(10)
    if found == False:
        try:
            subprocess.call(["python", "/home/pi/Automotive-HUD/Raspberry_Pi_Application/automotive_hud.py"])
        except KeyboardInterrupt:
            break
