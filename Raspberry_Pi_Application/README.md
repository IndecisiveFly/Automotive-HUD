# Raspberry Pi Application

Raspberry Pi source code for CSU Chico 2017-2018 Senior Project.

Raspberry Pi is used in this project to recieve data packets from Android application through Bluetooth, which then translates the information into a digital display for the user(s).

## Requirements/hardware used/tested on:
 * Raspberry Pi 3b. 
 * Android device with Bluetooth capabilities.
 * Some sort of display. (HDMI connection)
 
## Setup
### Update Pi
```
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get dist-upgrade -y
sudo rpi-update 
```

### For cpp bluetooth library
`sudo apt-get install libbluetooth-dev`

### Pybluez
`pip install pybluez`

### Bluetooth configuration
`sudo vim /etc/systemd/system/dbus-org.bluez.service`

  * edit two lines to look like:
```
ExecStart=/usr/lib/bluetooth/bluetoothd -C 
ExecStartPost=/usr/bin/sdptool add SP
```

[Source of following configurations for advertise service](https://stackoverflow.com/questions/34599703/rfcomm-bluetooth-permission-denied-error-raspberry-pi)

check if user pi is in bluetooth group
```
$cat /etc/group | grep bluetooth
bluetooth:x:111:pi
```
if not, add to group:

`sudo usermod -G bluetooth -a pi`

Then change group of /var/run/sdp file:

`sudo chgrp bluetotoh /var/run/sdp`

To make this persistent after reboot:
Create `/etc/systemd/system/var-run-sdp.path` with:
```
[Unit]
Descrption=Monitor /var/run/sdp

[Install]
WantedBy=bluetooth.service

[Path]
PathExists=/var/run/sdp
Unit=var-run-sdp.service
```

And another file, `/etc/systemd/system/var-run/sdp.service` with:
```
[Unit]
Description=Set permission of /var/run/sdp

[Install]
RequiredBy=var-run-sdp.path

[Service]
Type=simple
ExecStart=/bin/chgrp bluetooth /var/run/sdp
```

Finally start it all up:
```
sudo systemctl daemon-reload
sudo systemctl enable var-run-sdp.path
sudo systemctl enable var-run-sdp.service
sudo systemctl start var-run-sdp.path
```

If an error about file/directory not existing, follow [this link for instructions](https://www.raspberrypi.org/forums/viewtopic.php?t=132470)

or:

Edit `/lib/systemd/system/bluetooth.service`

and change:
`ExecStart=/usr/lib/bluetooth/bluetoothd`

to

`ExecStart=/usr/lib/bluetooth/bluetoothd --compat`


