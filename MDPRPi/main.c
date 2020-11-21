#include "hub.c"

int main()
{
    /*
    struct tcp_socket* socket;
    socket = init_socket(TCP);
    if(setup_socket(socket) == 0)
    {
        exit(EXIT_FAILURE);
    }
    if(listen_socket(socket) < 0)
    {
        exit(EXIT_FAILURE);
    }
    char* exit = "exit";
    while(1)
    {

        char* data = read_socket(socket);
        if(strncmp(exit, data, 4) == 0)
        {
            printf("exit command received\n");
            goto exit;
        }
        else
        {
            printf("received string: %s\n", data);
            send_socket(socket, data);
        }
    }
    exit:
    return 0;
    */
   init();
   start_hub();
   return 0;
}
/**
int load_dbl()
{
    struct mdp_queue* queue;
    char str[32];
    int len;
    strcpy(str, "HELLO");
    len = strlen(str);
    printf("Enqueue String: %s - (%d)\n", str, len);
    if((queue = init_queue()) == NULL)
    {
        printf("fail to init queue\n");
        return 0;
    }
    enqueue(queue, str);
    char* data = dequeue(queue);
    len = strlen(data);
    printf("Dequeue String: %s - (%d)\n", data, len);
    // test
    strcpy(str, "BYEBYE");
    len = strlen(str);
    printf("Enqueue String: %s - (%d)\n", str, len);
    enqueue(queue, str);
    data = dequeue(queue);
    len = strlen(data);
    printf("Dequeue String: %s - (%d)\n", data, len);
    return 0;
}

int llss()
{
    struct mdp_queue* queue;
    char str[32];
    int len;
    strcpy(str, "HELLO");
    len = strlen(str);
    printf("Enqueue String: %s - (%d)\n", str, len);
    if((queue = init_queue()) == NULL)
    {
        printf("fail to init queue\n");
        return 0;
    }
    enqueue(queue, str);
    strcpy(str, "BYEBYE");
    len = strlen(str);
    printf("Enqueue String: %s - (%d)\n", str, len);
    enqueue(queue, str);
    char* data = dequeue(queue);
    len = strlen(data);
    printf("Dequeue String: %s - (%d)\n", data, len);
    // test
    data = dequeue(queue);
    len = strlen(data);
    printf("Dequeue String: %s - (%d)\n", data, len);
    return 0;
}

*/