#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdlib.h>
#include <cstring>
#include <unistd.h>
#include <iostream>
#include <fstream>


using namespace std;

int sockfd, portno, n;

struct sockaddr_in serv_addr;
struct hostent *server;


//-------------------------------
void error(const char *msg)
{
    perror(msg);
    exit(0);
}



//---------------------------------------------------------
// Procedure: writeToFile
//---------------------------------------------------------
/**General function that writes to a given filename the message passed as a parameter **/
bool writeToFile(string message){
    static bool firstTimeFlag2 = true;
    ofstream myfile;
    if (firstTimeFlag2){
        myfile.open ("log/client.txt");
        firstTimeFlag2 = false;
    }
    else{
        myfile.open ("log/client.txt", ios::app);
    }

    myfile << message;
    myfile.close();
    return true;
}


//-------------------------------
void initialiseClient(int portNumber){   
    writeToFile("Initialising client 1\n");
    const char * argv[]={"socket","localhost", "56567"};
    portno = portNumber;//atoi(argv[2]);

    writeToFile("Initialising client 2");
    sockfd = socket(AF_INET, SOCK_STREAM, 0);

    writeToFile("Initialising client 3\n");
    if (sockfd < 0)
        error("ERROR opening socket");


    writeToFile("Initialising client 4\n");
    server = gethostbyname(argv[1]);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }


    writeToFile("Initialising client 5\n");
    memset((char *) &serv_addr, 0, sizeof(serv_addr));

    writeToFile("Initialising client 6\n");
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr,
          (char *)&serv_addr.sin_addr.s_addr,
          server->h_length);
    serv_addr.sin_port = htons(portno);

    writeToFile("Initialising client 7: " + to_string(portno) +"\n");
    if (connect(sockfd,(struct sockaddr *)&serv_addr,sizeof(serv_addr)) < 0)
        error("ERROR connecting");

    writeToFile("Initialising client 8\n");
    return;
}

//-------------------------------
void runClient(char *variables){
    writeToFile("\nRunning client 9\t Sending: ");
    n = write(sockfd,variables,strlen(variables));
    writeToFile(variables);    
    if (n < 0)
        error("ERROR writing to socket");

    memset(variables, 0, sizeof(char)*256);
    writeToFile("Running client 10\tReceiving: ");
    n = read(sockfd, variables, 255);
    writeToFile(variables);
    return;
}

/* void runClient(string vars){
    char* variables = vars.c_str();

    writeToFile("test.txt", "Running client 9\n");
    n = write(sockfd,variables,strlen(variables));
    if (n < 0)
        error("ERROR writing to socket");

    memset(variables, 0, sizeof(char)*256);
    n = read(sockfd, variables, 255);
    writeToFile("test.txt", "Running client 10\n");
    return;
} */