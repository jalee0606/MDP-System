#include "mdpqueue.c"
#include "mdpsocket.c"
#include "mdp_queue_t.c"
#include "logger.c"
#include <stdio.h>
#include <pthread.h> 

/**
 * Threading Hub 
 **/

struct mdp_queue_t *algo_queue = NULL, *android_queue = NULL, *arduino_queue = NULL;
struct mdp_socket *algo_client = NULL, *android_client = NULL, *arduino_client = NULL, *image_client = NULL;

void start_hub();
void* run_andread(void *vargp);
void* run_andwrite(void *vargp);
void* run_algread(void *vargp);
void* run_algwrite(void *vargp);
void* run_ardread(void *vargp);
void* run_ardwrite(void *vargp);

void init()
{
    // Initiate the queue to store the data
    algo_queue = init_queue_t(100);
    android_queue = init_queue_t(100);
    arduino_queue = init_queue_t(100);

    // Serial connection
    //arduino_client = init_socket(SERIAL);

    // Initiate the connection
    // Algo connection
    algo_client = init_socket(TCP);
    setup_socket(algo_client);
    listen_socket(algo_client);
    printf("[%p]\tcreated algo socket\n", algo_client);
    

    // Bluetooth connection
    android_client = init_socket(BLUETOOTH);
    setup_socket(android_client);
    listen_socket(android_client);
    printf("[%p]\tcreated android socket\n", android_client);
    // Setup TCP client to use ImageZMQ python
}

/**
 * Multi-processing require piping and mutex communication
 * 
 */ 
void start_hub_process()
{
    int i;
    int *d = malloc(sizeof(int));
    for(i = 0; i < 6; i++) {
        printf("getpid = 0x%X\n", getpid());
        if(fork()) {
            printf("Inside parent\n");
        } else {
            switch(i) {
                case 0:
                    run_algread(d);
                    break;
                case 1:
                    run_algwrite(d);
                    break;
                case 2:
                    //run_ardread(d);
                    break;
                case 3:
                    //run_ardwrite(d);
                    break;
                case 4:
                    run_andread(d);
                    break;
                case 5:
                    run_andwrite(d);
                    break;
            }
            exit(1);
        }
    }
}

void start_hub()
{
    pthread_t android_read, android_write, algo_read, algo_write, ard_read, ard_write;
    pthread_create(&android_read, NULL, run_andread, NULL); 
    pthread_create(&android_write, NULL, run_andwrite, NULL); 
    pthread_create(&algo_read, NULL, run_algread, NULL); 
    pthread_create(&algo_write, NULL, run_algwrite, NULL);
    printf("running all thread\n");
    //pthread_create(&ard_read, NULL, run_ardread, NULL); 
    //pthread_create(&ard_write, NULL, run_ardwrite, NULL);
    //pthread_join(android_read, NULL); 
    //pthread_join(android_write, NULL); 
    //pthread_join(ard_write, NULL); 
    //pthread_join(ard_read, NULL); 
    //pthread_join(algo_read, NULL); 
    //pthread_join(algo_write, NULL); 
    while(1)
    {

    }
}

/*
void* run_ardwrite(void *vargp)
{
    printf("running ardwrite()");
    while(1)
    {
        if(is_empty(arduino_queue) == 0)
        {
            char* msg = dequeue(arduino_queue);
            if(msg != NULL) {
                send_socket(arduino_client, msg);
            }
            usleep(1);
        }
    }
    return 0;
}
*/

void* run_ardread(void *vargp)
{
    printf("running ardread()\n");
    while(1)
    {
        struct packet_t* packet = read_serial(arduino_client);
        enqueue_t(algo_queue, packet);
    }
    return 0;
}

void* run_algread(void *vargp)
{
    printf("running algread()\n");
    while(1)
    {
        struct packet_t* pkt = read_socket(algo_client);
        if(pkt != NULL) {
            printf("read algo: %s\n", pkt->command);
            if(pkt->type == 1)
            {
                enqueue_t(algo_queue, pkt);
            }
            else if(pkt->type == 2)
            {
                enqueue_t(android_queue, pkt);
            }
            else
            {
                printf("error packet type: %d\n", pkt->type);
            }
        }
    }
    return 0;
}

void* run_algwrite(void *vargp)
{
    printf("running algwrite()\n");
    while(1)
    {
        struct node_t* node = dequeue_t(algo_queue);
        send_socket(algo_client, node->data->command);
        free(node->data->command);
        free(node->data);
        free(node);
    }
    return 0;
}

void* run_andread(void *vargp)
{
    printf("running andread()\n");
    while(1)
    {
        struct packet_t* pkt = read_socket(android_client);
        if(pkt != NULL) {
            printf("read android: %s\n", pkt->command);
            if(pkt->type == 1)
            {
                enqueue_t(algo_queue, pkt);
            }
            else if(pkt->type == 2)
            {
                enqueue_t(android_queue, pkt);
            }
            else
            {
                printf("error packet type: %d\n", pkt->type);
            }
        }
    }
    return 0;
}

void* run_andwrite(void *vargp)
{
    printf("running andwrite()\n");
    while(1)
    {
        struct node_t* node = dequeue_t(android_queue);
        send_socket(android_client, node->data->command);
        free(node->data->command);
        free(node->data);
        free(node);
    }
    return 0;
}