#include "mdpqueue.c"
#include <unistd.h> 
#include <stdio.h> 
#include <stdlib.h>
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h>
#include "constant.c"

/**
 * Socket Implementation in C (Bluetooth/TCP)
 * 
 * 
 **/

#define BLUETOOTH 1
#define TCP 0

struct tcp_socket {
    int type;
    int server_sock;
    int client_sock;
    char buffer[BUFFER_SIZE];
    struct mdp_queue* queue;
};

struct tcp_socket* init_socket(int type)
{
    printf("init_socket()\n");
    struct tcp_socket *custom_socket = malloc(sizeof(struct tcp_socket));
    custom_socket->queue = init_queue();
    custom_socket->type = type;
    return custom_socket;
}

int setup_socket(struct tcp_socket* ssocket)
{
    printf("setup_socket()\n");
    if(ssocket->type == BLUETOOTH)
    {
        // ssocket->server_sock = socket()
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
    else
    {
        goto error;
    }
    return 1;
    error: 
    free(ssocket);
    ssocket = NULL;
    return 0;
}

int listen_socket(struct tcp_socket* ssocket)
{
    if(ssocket->type == BLUETOOTH)
    {

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
    else
    {

    }
    return -3;
}

char* read_socket(struct tcp_socket* ssocket)
{
    int valread = read(ssocket->client_sock, ssocket->buffer, BUFFER_SIZE);
    return ssocket->buffer;
}

void send_socket(struct tcp_socket* ssocket, char* data)
{
    send(ssocket->client_sock, data, strlen(data), 0);
}