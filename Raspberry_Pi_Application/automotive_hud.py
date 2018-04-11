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

    #wait on socket and accept connection
    client_sock, client_info = server_sock.accept()
    print ("Accepted connection from ", client_info)
    display.draw_speed("0")

    #recieve data and process accordingly
    while 1:
        try:
            data = client_sock.recv(1024)
            if not data: break #TODO detect client d/c
            print ("recieved: ", data)
            if data == "quit":
                print ("exiting")
                client_sock.close()
                server_sock.close()
                display.exit()
                break
            display.draw_speed(data) #TODO recieve "command" and then respond accordingly 
            client_sock.send(data)

        except IOError:
            print ("IO Error")
            pass

        except KeyboardInterrupt:
            print ("disconnected")
            client_sock.close()
            server_sock.close()
            print ("all done")
            break

main()
