/*
 * Utilities.cpp
 *
 *  Created on: 4 Jan 2017
 *      Author: sgerasimou
 */

#include "Utilities.h"
#include <fstream>
#include <sys/stat.h>

using namespace std;

Utilities::Utilities()
{}

Utilities::~Utilities()
{}


inline bool exists (const std::string& name) {
  struct stat buffer;   
  return (stat (name.c_str(), &buffer) == 0); 
}

//---------------------------------------------------------
// Procedure: writeToFile(string filename, string outputString)
//
//---------------------------------------------------------
void Utilities::writeToFile(string filename, string outputString)
{
	ofstream myfile;
	//if (exists(filename)){
	if (!logExists){
		logExists = true;
		myfile.open (filename);
	}
	else{
		myfile.open (filename, ios::app);
	}
	myfile << outputString << "\n";
	myfile.close();
	return;
}