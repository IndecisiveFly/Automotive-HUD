from bluetooth import *

server_sock=BluetoothSocket(RFCOMM)
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid="00101101-0000-1000-8000-A0803F9B34FB"

print ("waiting for connection on RFCOMM channel %d" % port)

client_sock, client_info = server_sock.accept()
print ("Accepted connection from ", client_info)

while 1:
    try:
        data = client_sock.recv(1024)
        if data <=0: break
        print ("recieved [%s]" % data)
        client_sock.send(data)

    except IOError:
        pass

    except KeyboardInterrupt:
        print ("disconnected")
        client_sock.close()
        server_sock.close()
        print ("all done")
        break

