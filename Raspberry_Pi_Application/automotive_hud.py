from bluetooth import *
from visual import * 

def main():
    #display class
    display = Visual()

    #server bluetooth sockets
    server_sock=BluetoothSocket(RFCOMM)
    server_sock.bind(("",PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid="00101101-0000-1000-8000-A0803F9B34FB"

    print ("waiting for connection on RFCOMM channel ", port)

    advertise_service( server_sock, "Automotive HUD", service_id = uuid, service_classes = [uuid, SERIAL_PORT_CLASS], profiles = [SERIAL_PORT_PROFILE])
    #wait on socket and accept connection
    client_sock, client_info = server_sock.accept()
    print ("Accepted connection from ", client_info)
    display.draw_speed("0")
    display.draw_units()

    #recieve data and process accordingly
    while 1:
        try:
            data = client_sock.recv(1024)
            if not data: break 
            print ("recieved: ", data)
            values = data[2:]
            if data[0] == "q":
                print ("exiting")
                client_sock.close()
                server_sock.close()
                display.exit()
                break
            if data[0] == "c":
                display.set_color(values)
            if data[0] == "m":
                display.set_mph()
            if data[0] == "k":
                display.set_kmh()
            if data[0] == "s":
                display.draw_speed(values)
            if data[0] == "l":
                display.draw_location(values)
            client_sock.send(values)

        except IOError:
            print ("IO Error")
            client_sock.close()
            server_sock.close()
            display.exit()
            break

        except KeyboardInterrupt:
            print ("disconnected")
            client_sock.close()
            server_sock.close()
            display.exit()
            print ("all done")
            break

main()
