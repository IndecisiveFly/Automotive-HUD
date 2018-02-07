//automotive_hud.cpp

#include <iostream>
#include <stdio.h>
#include <unistd.h>
#include <string>

#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>

int main(int argc, char* argv[])
{
  struct sockaddr_rc loc_addr = {0}, rem_addr = {0};
  char buff[1024] = {0};
  int s, client, bytes_read;
  socklen_t opt = sizeof(rem_addr);
  bdaddr_t tmp = {0};

  //socket
  s = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);

  //bind socket to first connected bluetooth
  loc_addr.rc_family = AF_BLUETOOTH;
  loc_addr.rc_bdaddr = tmp;
  loc_addr.rc_channel = (uint8_t) 1;
  bind(s, (struct sockaddr *)&loc_addr, sizeof(loc_addr));

  listen(s, 1);

  client = accept(s, (struct sockaddr *)&rem_addr, &opt);
  ba2str(&rem_addr.rc_bdaddr, buff);
  fprintf(stdout, "accepted connection from %s\n", buff);
  memset(buff, 0, sizeof(buff));

  //read from client

  //currently read 5 messages before exiting
  for(int i=0; i<5; i++)
  {
    bytes_read = read(client, buff, sizeof(buff));
    if(bytes_read>0)
    {
      printf("recieved: %s\n", buff);
      memset(buff, 0, sizeof(buff)); //clear out buffer for next read
    }
  }


  //close connection
  close(client);
  close(s);

  return 0;
}
