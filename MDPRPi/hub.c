#include "mdpqueue.c"
#include "mdpsocket.c"
#include "logger.c"
#include <stdio.h>
#include <pthread.h> 

/**
 * Threading Hub
 **/

struct mdp_queue *algo_queue = NULL, *android_queue = NULL, *arduino_queue = NULL;
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
    algo_queue = init_queue();
    android_queue = init_queue();
    arduino_queue = init_queue();

    // Initiate the connection
    // Algo connection
    algo_client = init_socket(TCP);
    setup_socket(algo_client);
    listen_socket(algo_client);

    // Serial connection
    arduino_client = init_socket(SERIAL);
    
    // Bluetooth connection
    android_client = init_socket(BLUETOOTH);
    setup_socket(algo_client);
    listen_socket(algo_client);

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
                    run_ardread(d);
                    break;
                case 3:
                    run_ardwrite(d);
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
    printf("creating thread android_read\n");
    pthread_create(&android_read, NULL, run_andread, NULL); 
    pthread_create(&android_write, NULL, run_andwrite, NULL); 
    pthread_create(&algo_read, NULL, run_algread, NULL); 
    pthread_create(&algo_write, NULL, run_algread, NULL); 
    pthread_create(&ard_read, NULL, run_ardread, NULL); 
    pthread_create(&ard_write, NULL, run_ardwrite, NULL);
    /*
    pthread_join(android_read, NULL); 
    pthread_join(android_write, NULL); 
    pthread_join(ard_write, NULL); 
    pthread_join(ard_read, NULL); 
    pthread_join(algo_read, NULL); 
    pthread_join(algo_write, NULL); 
    */
    while(1)
    {
        // looping
    }
}

void* run_ardwrite(void *vargp)
{
    printf("running ardwrite()");
    while(1)
    {
        printf("running ardwrite()\n");
    }
}

void* run_ardread(void *vargp)
{
    printf("running ardread()");
    while(1)
    {
        printf("running ardread()\n");
    }
}

void* run_algread(void *vargp)
{
    printf("running algread()");
    while(1)
    {
        printf("running algread()\n");
    }
}

void* run_algwrite(void *vargp)
{
    printf("running algwrite()");
    while(1)
    {
        printf("running algwrite()\n");
    }
}

void* run_andread(void *vargp)
{
    printf("running andread()");
    while(1)
    {
        printf("running andread()\n");
    }
}

void* run_andwrite(void *vargp)
{
    printf("running andwrite()");
    while(1)
    {
        printf("running andwrite()\n");
    }
}