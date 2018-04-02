# Raspberry Pi Application

Raspberry Pi source code for CSU Chico 2017-2018 Senior Project.

Raspberry Pi is used in this project to recieve data packets from Android application through Bluetooth, which then translates the information into a digital display for the user(s).

## Requirements/hardware used/tested on:
 * Raspberry Pi 3b. (Raspbian Stretch 2017-08-16)
 * Android device with Bluetooth capabilities.
 * Some sort of display. (HDMI connection to a Pico Projector)
 
## Setup

```
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get dist-upgrade -y
sudo rpi-update 
```

### For cpp bleutooth library
`sudo apt-get install libbluetooth-dev`

### Bluetooth configure
`sudo nano /etc/systemd/system/dbus-org.bluez.service`

  * edit two lines to look like:
```
ExecStart=/usr/lib/bluetooth/bluetoothd -C 
ExecStartPost=/usr/bin/sdptool add SP
```

### Pybluez
`pip install pybluez`