#ifndef CONSTANT_H
#define CONSTANT_H

#define PORT 3000
#define BUFFER_SIZE 1024
#define ANDROID_WRITE 0
#define ANDROID_READ 1
#define ALGO_WRITE 2
#define ALGO_READ 3
#define ARD_WRITE 4
#define ARD_READ 5

const short ANDROID = 1 << 0;
const short ARDUINO = 1 << 1;
const short ALGORITHM = 1 << 2;
const short RPI = 1 << 3;

#endif