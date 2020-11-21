#include <unistd.h> 
#include <stdio.h> 
#include <stdlib.h>
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h>
#include "constant.c"
#include <termios.h>
#include <fcntl.h>
#include <errno.h>
#include "mdp_queue_t.c"
/**
 * Socket Implementation in C (Bluetooth/TCP)
 * 
 * 
 **/

#define BLUETOOTH 1
#define TCP 0
#define SERIAL 2

struct mdp_socket {
    int type;
    int server_sock;
    int client_sock;
    char buffer[BUFFER_SIZE];
};

struct mdp_socket* init_socket(int type)
{
    printf("init_socket()\n");
    struct mdp_socket *custom_socket = malloc(sizeof(struct mdp_socket));
    custom_socket->type = type;
    return custom_socket;
};

int read_bytes(struct mdp_socket*, unsigned int, void*);

int setup_socket(struct mdp_socket* ssocket)
{
    printf("setup_socket()\n");
    if(ssocket->type == BLUETOOTH)
    {
        printf("tcp socket selected\n");
        int opt = 1;
        struct sockaddr_in address;
        int addrlen = sizeof(address);
        if((ssocket->server_sock = socket(AF_INET, SOCK_STREAM, 0)) == 0)
        {
            printf("fail to create\n");
            goto error;
        }

        if(setsockopt(ssocket->server_sock, SOL_SOCKET, SO_REUSEPORT, &opt, (socklen_t) sizeof(opt)))
        {
            printf("fail to set\n");
            goto error;
        }
        address.sin_family = AF_INET;
        address.sin_addr.s_addr = INADDR_ANY;
        address.sin_port = htons(3001);

        if(bind(ssocket->server_sock, (struct sockaddr *)&address, sizeof(address)) < 0)
        {
            printf("fail to bind\n");
            goto error;
        }
    }
    else if(ssocket->type == TCP)
    {
        printf("tcp socket selected\n");
        int opt = 1;
        struct sockaddr_in address;
        int addrlen = sizeof(address);
        if((ssocket->server_sock = socket(AF_INET, SOCK_STREAM, 0)) == 0)
        {
            printf("fail to create\n");
            goto error;
        }

        if(setsockopt(ssocket->server_sock, SOL_SOCKET, SO_REUSEPORT, &opt, (socklen_t) sizeof(opt)))
        {
            printf("fail to set\n");
            goto error;
        }
        address.sin_family = AF_INET;
        address.sin_addr.s_addr = INADDR_ANY;
        address.sin_port = htons(PORT);

        if(bind(ssocket->server_sock, (struct sockaddr *)&address, sizeof(address)) < 0)
        {
            printf("fail to bind\n");
            goto error;
        }
    }
    else if(ssocket->type == SERIAL)
    {
        ssocket->client_sock = open("/dev/ttyUSB0", O_RDWR);
        if(ssocket->client_sock)
        {
            printf("Error %i from open: %s\n", errno, strerror(errno));
            goto error;
        }
    }
    else
    {
        goto error;
    }
    return 1;
    error: 
    free(ssocket);
    ssocket = NULL;
    return 0;
};

int listen_socket(struct mdp_socket* ssocket)
{
    if(ssocket->type == BLUETOOTH)
    {
        printf("listening\n");
        if(listen(ssocket->server_sock, 3) < 0)
        {
            return -1;
        }
        struct sockaddr_in address;
        int addrlen = sizeof(address);
        printf("waiting to accept\n");
        if((ssocket->client_sock = accept(ssocket->server_sock, (struct sockaddr *)&address,(socklen_t*)&addrlen)) < 0)
        {
            return -2;
        }
        printf("connection accepted\n");
        return 0;
    }
    else if(ssocket->type == TCP)
    {
        printf("listening\n");
        if(listen(ssocket->server_sock, 3) < 0)
        {
            return -1;
        }
        struct sockaddr_in address;
        int addrlen = sizeof(address);
        printf("waiting to accept\n");
        if((ssocket->client_sock = accept(ssocket->server_sock, (struct sockaddr *)&address,(socklen_t*)&addrlen)) < 0)
        {
            return -2;
        }
        printf("connection accepted\n");
        return 0;
    }
    else if(ssocket->type == SERIAL)
    {
        // Serial don't need to listen
        return 0;
    }
    else
    {

    }
    return -3;
};

struct packet_t* read_socket(struct mdp_socket* ssocket)
{
    char type = 0;
    unsigned int length = 0;
    char* data = 0;
    if(read_bytes(ssocket, sizeof(type), (void*)(&type)) == 0)
    {
        printf("error reading type\n");
        return NULL;
    }

    if(read_bytes(ssocket, sizeof(length), (void*)(&length)) == 0)
    {
        printf("error reading length\n");
        return NULL;
    }
    printf("type: %d, length: %d\n", type, length);
    data = malloc(length);
    if(read_bytes(ssocket, length, (void*)data) == 0)
    {
        printf("error reading data\n");
        return NULL;
    }
    struct packet_t* packet = malloc(sizeof(struct packet_t));
    packet->type = type;
    packet->command = malloc(length+1);
    memcpy(packet->command, data, length);
    *(packet->command+length) = '\0';
    free(data);
    return packet;
};

void send_socket(struct mdp_socket* ssocket, char* data)
{
    printf("sending %s\n", data);
    write(ssocket->client_sock, data, strlen(data));
};

int read_bytes(struct mdp_socket* ssocket, unsigned int x, void* buffer)
{
    printf("reading from [%p]\n", ssocket);
    int bytesRead = 0;
    int result;
    while(bytesRead < x)
    {
        printf("left with %d bytes\n", (x-bytesRead));
        result = read(ssocket->client_sock, buffer + bytesRead, x-bytesRead);
        if(result < 1)
        {
            printf("[%p] error reading\n", ssocket);
            return 0;
        }
        bytesRead += result;
    }
    return 1;
};