import socket

hostMACAddress = 'B8:27:EB:B7:27:72'
port = 3
backlog = 1
size = 1024
s = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
s.bind((hostMACAddress.port))
s.listen(backlog)

try:
    client, address = s.accept()
    while 1:
        data = client.recv(size)
        if data:
            print(data)
            client.send(data)
except:
    print("closing socket")
    client.close()
    s.close()
