#include "mdpqueue.c"
#include "mdpsocket.c"
#include "mdp_queue_t.c"
#include "logger.c"
#include <stdio.h>
#include <pthread.h> 

/**
 * Pipe for IPC
 */
int to_android_queue[2], to_android[2];
int to_arduino_queue[2], to_arduino[2];
int to_algo_queue[2], to_algo[2];

/**
 * Socket
 */ 
struct mdp_socket *algo_client = NULL, *android_client = NULL, *arduino_client = NULL, *image_client = NULL;

/**
 * Queue
 */
struct mdp_queue_t *algo_queue = NULL, *android_queue = NULL, *arduino_queue = NULL;

/**
 * Process ID list
 */
pid_t pid[9];

void setup_IPC()
{
    if(pipe(to_android_queue) == -1)
    {
        fprintf(stderr, "[pipe()] Android Queue Failed"); 
        return 1;
    }
    if(pipe(to_android) == -1)
    {
        fprintf(stderr, "[pipe()] Android Failed"); 
        return 1;
    }
    if(pipe(to_arduino_queue) == -1)
    {
        fprintf(stderr, "[pipe()] Arduino Queue Failed"); 
        return 1;
    }
    if(pipe(to_arduino) == -1)
    {
        fprintf(stderr, "[pipe()] Arduino Failed"); 
        return 1;
    }
    if(pipe(to_algo_queue) == -1)
    {
        fprintf(stderr, "[pipe()] Android Queue Failed"); 
        return 1;
    }
    if(pipe(to_algo) == -1)
    {
        fprintf(stderr, "[pipe()] Android Queue Failed"); 
        return 1;
    }
}

void init()
{
    int pCount;

    algo_client = init_socket(TCP);
    setup_socket(algo_client);
    listen_socket(algo_client);
    printf("[%p]\tcreated algo socket\n", algo_client);
    

    // Bluetooth connection
    android_client = init_socket(BLUETOOTH);
    setup_socket(android_client);
    listen_socket(android_client);
    printf("[%p]\tcreated android socket\n", android_client);

    android_queue = init_queue_t(100);
    algo_queue = init_queue_t(100);
    arduino_queue = init_queue_t(100);

    for(pCount = 0; pCount < 6; pCount++)
    {
        pid[pCount] = fork();
        if(pid[pCount] == 0)
        {
            switch(pCount)
            {
                case 0:
                break;
                case 1:
                break;
                case 2:
                break;
                case 3:
                break;
                case 4:
                break;
                case 5:
                break;
                case 6:
                break;
            }
        }
    }
}

/**
 * Android Queue Process
 */
void prepare_android_queue()
{
    pthread_t queue_read, queue_write;
    pthread_create(&queue_read, NULL, read_from_android_queue, NULL); 
    pthread_create(&queue_write, NULL, write_to_android_queue, NULL); 
    pthread_join(queue_read);
    pthread_join(queue_write);
}

/**
 * Android Queue Thread
 */
void write_to_android_queue()
{
    while(1)
    {
        struct packet_t* pkt = malloc(sizeof(struct packet_t));
        size_t n;
        read(to_android_queue[0], &n, sizeof(n));
        pkt->command = malloc(n);
        read(to_android_queue[0], pkt->command, n);
        enqueue_t(android_queue, pkt)
        free(pkt);
    }
}

void read_from_android_queue()
{
    while(1)
    {
        struct node_t* node = dequeue_t(android_queue);
        //send_socket(algo_client, node->data->command);
        write(to_android[1], strlen(node->data->command), sizeof(size_t));
        write(to_android[1], node->data->command, strlen(node->data->command))
        free(node->data->command);
        free(node->data);
        free(node);
    }
}

void write_android()
{
    char buffer[512];
    while(1)
    {
        size_t n;
        read(to_android[0], &n, sizeof(n));
        read(to_android[0], buffer, n);
        send_socket_wsize(android_client, buffer, n);
    }
}

void read_android()
{
    while(1)
    {
        struct packet_t* pkt = read_socket(android_client);
        if(pkt != NULL) {
            printf("read android: %s\n", pkt->command);
            if(pkt->type == 1)
            {
                write(to_algo_queue[1], strlen(pkt->command), sizeof(size_t));
                write(to_algo_queue[1], pkt->command, strlen(pkt->command));
                free(pkt);
            }
            else if(pkt->type == 2)
            {
                write(to_android_queue[1], strlen(pkt->command), sizeof(size_t));
                write(to_android_queue[1], pkt->command, strlen(pkt->command));
                free(pkt);
            }
            else
            {
                printf("error packet type: %d\n", pkt->type);
            }
        }
    }
    return 0;
}