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

### Bluetooth configuration
`sudo vim /etc/systemd/system/dbus-org.bluez.service`

  * edit two lines to look like:
```
ExecStart=/usr/lib/bluetooth/bluetoothd -C 
ExecStartPost=/usr/bin/sdptool add SP
```

### Pybluez
`pip install pybluez`
