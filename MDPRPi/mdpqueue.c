#ifndef MDP_QUEUE
#define MDP_QUEUE

#include <string.h>
#include <stdlib.h>
#include <stdio.h>

struct Node {
    char* data;
    size_t length;
    struct Node* next;
};

struct mdp_queue {
    struct Node *head;
    struct Node *tail;
};

void enqueue(struct mdp_queue*, char*);
char* dequeue(struct mdp_queue*);
struct mdp_queue* init_queue();
int is_empty(struct mdp_queue*);
void pushFront(struct mdp_queue*, char*);
struct Node* createNode(char*);

void pushFront(struct mdp_queue* queue, char* data)
{
    if(queue->head == NULL) {
        enqueue(queue, data);
        return;
    }
    struct Node* node = createNode(data);
    node->next = queue->head;
    queue->head = node;
    return;
}

int is_empty(struct mdp_queue* queue)
{
    if(queue->head == NULL)
        return 1;
    return 0;
}

struct mdp_queue* init_queue()
{
    struct mdp_queue* queue = malloc(sizeof(struct mdp_queue));
    return queue;
}

struct Node* createNode(char* data)
{
    struct Node *node = (struct Node*) malloc(sizeof(struct Node));
    size_t data_len = strlen(data);
    node->data = (char*) malloc(data_len+1);
    strncpy(node->data, data, data_len);
    *(data+data_len) = '\0';
    node->length = data_len;
    node->next = NULL;
    return node;
}

void enqueue(struct mdp_queue* queue, char* data)
{
    if(queue->head == NULL)
    {
        struct Node *node = createNode(data);
        queue->head = node;
        queue->tail = node;
    }
    else
    {
        struct Node *node = createNode(data);
        queue->tail->next = node;
        queue->tail = node;
    }
}

/**
 * Return NULL if queue is empty, else return the message
 **/
char* dequeue(struct mdp_queue* queue)
{
    if(queue->head == NULL) {
        return NULL;
    }
    struct Node* node = queue->head;
    queue->head = node->next;
    return node->data;
}

#endif