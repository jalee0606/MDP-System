#ifndef MDP_QUEUE_T
#define MDP_QUEUE_T

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

struct packet_t
{
    char type;
    char* command;
};

struct node_t 
{
    struct packet_t* data;
    struct node_t* next;
};

struct mdp_queue_t
{
    struct node_t* first;
    struct node_t* last;
    pthread_mutex_t lock;
    pthread_cond_t empty;
    int nelt;
    int capacity;
};

struct mdp_queue_t* init_queue_t(int);
void enqueue_t(struct mdp_queue_t*, struct packet_t*);
struct node_t* dequeue_t(struct mdp_queue_t*);
void destroy_queue_t(struct mdp_queue_t*);
struct node_t* create_node_t(struct packet_t*);


struct mdp_queue_t* init_queue_t(int capacity)
{
    struct mdp_queue_t* queue = malloc(sizeof(struct mdp_queue_t));
    queue->nelt = 0;
    queue->capacity = capacity;
    queue->first = NULL;
    queue->last = NULL;
    pthread_mutex_init(&queue->lock, NULL);
    pthread_cond_init(&queue->empty, NULL);
    return queue;
}

void enqueue_t(struct mdp_queue_t* queue, struct packet_t* data)
{
    pthread_mutex_lock(&queue->lock);
    struct node_t* node = create_node_t(data);
    if(queue->first == NULL)
    {
        queue->first = node;
        // create first node and last node
    }
    if(queue->last == NULL)
    {
        queue->last = node;
    }
    else
    {
        queue->last->next = node;
        queue->last = node;
    }
    queue->nelt += 1;
    pthread_cond_signal(&queue->empty); 
    pthread_mutex_unlock(&queue->lock); 
    return;
}

struct node_t* dequeue_t(struct mdp_queue_t* queue)
{
    pthread_mutex_lock(&queue->lock);
    if(queue->nelt == 0)
    {
        pthread_cond_wait(&queue->empty, &queue->lock); 
    }
    struct node_t* node = queue->first;
    queue->first = queue->first->next;
    queue->nelt -= 1;
    if(queue->nelt == 0)
    {
        queue->last = NULL;
    }
    pthread_mutex_unlock(&queue->lock); 
    return node;
}

void destroy_queue_t(struct mdp_queue_t* queue)
{
    free(queue->first);
    queue->first = NULL;
    free(queue->last);
    queue->last = NULL;
    free(queue);
    queue = NULL;
    return;
}

struct node_t* create_node_t(struct packet_t* data)
{
    struct node_t* node = malloc(sizeof(struct node_t));
    node->next = NULL;
    node->data = data;
    return node;
}

#endif