/************************************************************/
/*    NAME: Simos Gerasimou                                              */
/*    ORGN: MIT                                             */
/*    FILE: UUV.cpp                                        */
/*    DATE:                                                 */
/************************************************************/

#include <iterator>
#include "UUV.h"
#include "Utilities.h"

#include "client/client.h"

//#include "server/serverLoop.h"
//#include <pthread.h>

using namespace std;

//---------------------------------------------------------
// Constructor
//---------------------------------------------------------
UUV::UUV() : xDistribution(x_OK), pDistribution(p_OK), inspectDistribution(r_inspect), travelDistribution(r_travel), 
			 retryDistribution(r_retry), dirtyDistribution(r_damage + r_clean + r_fail_clean)
{
	m_iterations 			= 0;
	m_timewarp   			= 1;
	m_app_start_time		= MOOSTime(true);
	m_current_iterate		= m_app_start_time;
	m_previous_iterate 		= m_app_start_time;

	M_TIME_WINDOW 			= 10;

	PORT 					= 8888;

	m_uuv_speed				= 4;


////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
	new_chain_visited = false;
	auv_steady		  = false;
	//initLegend();
}


//---------------------------------------------------------
// Destructor
//---------------------------------------------------------
UUV::~UUV()
{
}


//---------------------------------------------------------
// Procedure: OnStartUp()
//            happens before connection is open
//---------------------------------------------------------
bool UUV::OnStartUp()
{
	AppCastingMOOSApp::OnStartUp();               // Add this line

	list<string> sParams;
	m_MissionReader.EnableVerbatimQuoting(false);
	if(m_MissionReader.GetConfiguration(GetAppName(), sParams)) {
		list<string>::iterator p;
		for(p=sParams.begin(); p!=sParams.end(); p++) {
		  string original_line = *p;
		  string param = stripBlankEnds(toupper(biteString(*p, '=')));
		  string value = stripBlankEnds(*p);

		  if(param == "NAME") { // get uuv name
			m_uuv_name = value;
		  }
		  else if (param == "TIME_WINDOW"){
			  if (isNumber(value.c_str()))
				  M_TIME_WINDOW = atoi(value.c_str());
		  }
		  else if (param == "PORT"){
		  		reportEvent (doubleToString(PORT));
			  if (isNumber(value.c_str()))
				  PORT = atoi(value.c_str());
			  	reportEvent (doubleToString(PORT));
		  }
		  else if (param == "P_CHAIN_OK"){//  P_CHAIN_OK          = 0.1
			if (isNumber(value.c_str()) && atof(value.c_str())>=0 && atof(value.c_str())<=1 ){
		  		p_OK = atof(value.c_str());
			}
	  		else
				reportConfigWarning("Invalid P_CHAIN_OK value:" +value);
		  }
		  else if (param == "P_CLEAN_CHAIN"){//  X_CHAIN_CLEAN       = 0.9
			if (isNumber(value.c_str()) && atof(value.c_str())>=0 && atof(value.c_str())<=1 ){
		  		x_OK = atof(value.c_str());
			}
	  		else
				reportConfigWarning("Invalid X_CHAIN_CLEAN value:" +value);
		  }
		  else if (param == "R_CHAIN_INSPECT"){//  R_CHAIN_INSPECT     = 5.0
			if (isNumber(value.c_str()) && atof(value.c_str())>=0){
		  		r_inspect = atof(value.c_str());
			}
	  		else
				reportConfigWarning("Invalid R_CHAIN_INSPECT value:" +value);
		  }
		  else if (param == "R_CHAIN_TRAVEL"){//  R_CHAIN_TRAVEL      = 4.0
			if (isNumber(value.c_str()) && atof(value.c_str())>=0){
		  		r_travel = atof(value.c_str());
			}
	  		else
				reportConfigWarning("Invalid R_CHAIN_TRAVEL value:" +value);
		  }
		  else if (param == "R_CHAIN_CLEAN"){//  R_CHAIN_CLEAN       = 0.00001
			if (isNumber(value.c_str()) && atof(value.c_str())>=0){
		  		r_clean = atof(value.c_str());
			}
	  		else
				reportConfigWarning("Invalid R_CHAIN_CLEAN value:" +value);
		  }
		  else if (param == "R_CHAIN_FAIL_CLEAN"){//  R_CHAIN_FAIL_CLEAN  = 0.5
			if (isNumber(value.c_str()) && atof(value.c_str())>=0){
		  		r_fail_clean = atof(value.c_str());
			}
	  		else
				reportConfigWarning("Invalid R_CHAIN_FAIL_CLEAN value:" +value);
		  }
		  else if (param == "R_DAMAGE"){//  R_DAMAGE            = 0.000001
			if (isNumber(value.c_str()) && atof(value.c_str())>=0){
		  		r_damage = atof(value.c_str());
			}
	  		else
				reportConfigWarning("Invalid R_DAMAGE value:" +value);
		  }
  		  else if (param == "LOG_FILE"){
  		  	logFileName = value.c_str();
		  }
		  else if (param == "SEED"){
			  if (isNumber(value.c_str()))
				  generator.seed(atoi(value.c_str()));
		  }
		  else //throw a configuration warning
			  reportUnhandledConfigWarning(original_line);
		}
	}
	else{
		reportConfigWarning("No configuration block found for " + GetAppName());
	}

	m_timewarp = GetMOOSTimeWarp();

	//do initialisations
	initialiseSimulationParameters();
	//initSensorsMap();

   //init controller server
   reportEvent("UUV starts mission");
   initServer();


	RegisterVariables();
	return(true);
}


//------------------------------------------------------------------------------
// Procedure: initialiseSimulationParameters()
//            if a parameter is not provided in the configuration block (see onStartUp)
//			  the default (in the header file is used)
//------------------------------------------------------------------------------
void UUV::initialiseSimulationParameters(){
    sumProb 			= r_clean + r_fail_clean + r_damage;
	normCleanProb       = r_clean/sumProb;
	normFailCleanProb   = r_fail_clean/sumProb;
	normDamageProb      = r_damage/sumProb;

	pDistribution 		= bernoulli_distribution(p_OK);
	xDistribution 		= bernoulli_distribution(x_OK);
	inspectDistribution = exponential_distribution<double>(r_inspect);
	travelDistribution 	= exponential_distribution<double>(r_travel);
	dirtyDistribution 	= exponential_distribution<double>(r_damage + r_clean + r_fail_clean);
}


//---------------------------------------------------------
// Procedure: OnConnectToServer
//---------------------------------------------------------
bool UUV::OnConnectToServer()
{
   // register for variables here
   // possibly look at the mission file?
   // m_MissionReader.GetConfigurationParam("Name", <string>);
   // m_Comms.Register("VARNAME", 0);

   showChainMessage("UUV starts mission");
   RegisterVariables();
   return(true);
}


//---------------------------------------------------------
// Procedure: RegisterVariables
//---------------------------------------------------------
void UUV::RegisterVariables()
{
	AppCastingMOOSApp::RegisterVariables();      // Add this line

	// for (vector<string>::iterator it = m_uuv_sensors.begin();  it != m_uuv_sensors.end(); it++){
	// 	 Register(*it, 0);
	// }

	Register("NEW_CHAIN", 0);
	Register("NAV_SPEED", 0);
	Register("CLEANING_DONE", 0);
	Register("STATION_KEEP", 0);
	Register("FINAL_CHAIN", 0);
	Register("SURVEY_UPDATES2", 0);
	initLegend();
}


//---------------------------------------------------------
// Procedure: OnNewMail
//---------------------------------------------------------
bool UUV::OnNewMail(MOOSMSG_LIST &NewMail)
{
	AppCastingMOOSApp::OnNewMail(NewMail);        // Add this line

	MOOSMSG_LIST::iterator p;

	for(p=NewMail.begin(); p!=NewMail.end(); p++) {
		CMOOSMsg &msg = *p;

		#if 0 // Keep these around just for template
			string key   = msg.GetKey();
			string comm  = msg.GetCommunity();
			double dval  = msg.GetDouble();
			string sval  = msg.GetString();
			string msrc  = msg.GetSource();
			double mtime = msg.GetTime();
			bool   mdbl  = msg.IsDouble();
			bool   mstr  = msg.IsString();
		#endif

		string key   = msg.GetKey();
		double value  = msg.GetDouble();
		// if (find(m_uuv_sensors.begin(), m_uuv_sensors.end(), key) != m_uuv_sensors.end()){

		// 	m_sensors_map[key].newReading(value);
		// }
		if (key == "NEW_CHAIN"){
			new_chain_visited = true;
			string valueS = msg.GetString(); //station_pt=$(X),$(Y)
			std::size_t pos = valueS.find("=");      // position of "=" in str
  			std::string coords = valueS.substr (pos+1);     // get from "=" to the end

			  pos = coords.find(",");      // position of "live" in str
			  xCoord = coords.substr (0,pos);     // X
			  yCoord = coords.substr (pos+1);     // Y

			  Notify("Chains", numOfInspectedChains);
			  if (numOfInspectedChains==chainsVector.size()-1){
			  	r_fail_clean = 0.3;
  				Notify("R_CHAIN_FAIL_CLEAN", 0.3);
			  	initialiseSimulationParameters();
			  }
		}
		if (key == "STATION_KEEP"){
			if (msg.GetString() == "true"){
				is_station_keep = true;
				if (!isFinalChain){
					stringstream ss;
					ss << "UUV inspects chain " << (numOfInspectedChains+1);
					showChainMessage  (ss.str());
				}
			}
		}
		if (key == "NAV_SPEED"){
			if (value <= 0.1){
				auv_steady = true;
			}
			else{
					auv_steady = false;
			}
		}
		if (key == "CLEANING_DONE"){
			isCleaningDone = true;
//   			Utilities::writeToFile("log/"+logFileName, "CLEANING_DONE received\n");
		}
		if (key == "FINAL_CHAIN"){
			isFinalChain = true;
		}
		if (key == "SURVEY_UPDATES2"){

			vector<string> s = parseString(removeWhite(msg.GetString()),"=");

			handleChainsCoords(s[1]);
			
			string p;
			//for (const auto &piece : points) p += piece;
			int j = 1;
			std::vector<int>::size_type sz = chainCooords.size();
  			for (int i=0; i<sz; ){
				showMarker(chainCooords[i], chainCooords[i+1], "orange", 8, j++);
  				p += chainCooords[i] +" ";
  				p += chainCooords[i+1] + ", ";
  				i += 2;
  			}
			Notify("POINTS", p +"!-->"+ intToString(sz));
		    Notify("VIEW_SEGLIST", "pts={"+s[1]+"},label=");
		    Notify("CHAINS_NUM", chainsVector.size());
		}

	}

	return(true);
}


//---------------------------------------------------------
// Procedure: Iterate()
//            happens AppTick times per second
//---------------------------------------------------------
bool UUV::Iterate()
{
	AppCastingMOOSApp::Iterate();                  // Add this line
    stringstream ss;

	//do app stuff here
	m_iterations++;
	m_current_iterate = MOOSTime(true);
    int dirtyResult;//      = 0;
    double travelT;

	if (new_chain_visited && auv_steady && !isCleaningDone){ //arrived at a new chain

		if (!isAuvCleaning){
		   string outputStr = "";

		    double inspectTime = inspectDistribution(generator);
		    double pOK         = pDistribution(generator);
		    // cout << inspectTime <<"\t" << pOK << endl;
		    if (pOK){//go to DONE
		        travelT = travelDistribution(generator);
		        outputStr =  "Chain " + intToString(numOfInspectedChains+1) +  " is OK, going to the next, Travelling time: " + doubleToString(1/travelT) +"\n\n";
				Utilities::writeToFile("log/"+logFileName, outputStr);
				reportEvent(outputStr);
		        Notify ("STATION_KEEP", "false");
		        new_chain_visited = false;
		        showMarker(xCoord, yCoord, "aquamarine", ++numOfInspectedChains);
		        ss << "Chain " << numOfInspectedChains << " is clean";
				showChainMessage (ss.str());
		    	isMissionFinished();
		    }
		    else{
		    	outputStr = "Chain " + intToString(numOfInspectedChains+1) + " needs cleaning ";
		        ss << outputStr;
				showChainMessage (ss.str());
		    	outputStr += doX();
        		Utilities::writeToFile("log/"+logFileName, outputStr);
				reportEvent(outputStr);		
		    }
		}
		Notify("AUV_STEADY", auv_steady);
	}


	if (new_chain_visited && isCleaningDone && isAuvCleaning){
        Notify ("STATION_KEEP", "true"); Notify ("CLEAN", "false");
        string outputStr = "\tCleaning done\n";
		Utilities::writeToFile("log/"+logFileName, outputStr);
		reportEvent(outputStr);		
		isAuvCleaning = false;
	}


	if (new_chain_visited && isCleaningDone && auv_steady){
		string outputStr = "";
        double v = ((double) rand() / (RAND_MAX));

        if (v <= normDamageProb){
            dirtyResult = -1; //catastrophic failure
            outputStr += "Catastrophic failure\n\n";
			isAuvCleaning 		= false; 
			isCleaningDone		= false;
			new_chain_visited	= false;
			auv_steady 			= false;
			Notify("DEPLOY", "false");
	        ss << "UUV has catastrophic failure while on chain " << (numOfInspectedChains+1);
			showChainMessage (ss.str());
	        showMarker(xCoord, yCoord, "red", ++numOfInspectedChains);
        }
        else if (v <= normCleanProb + normDamageProb){
            dirtyResult = +1; //chain cleaned successfully
            outputStr += "Chain cleaned\n\n";
			isAuvCleaning 		= false;
			isCleaningDone		= false;
			new_chain_visited	= false;
			auv_steady 			= false;
			Notify("STATION_KEEP", "false");
        	ss << "UUV cleaned successfully chain " << (numOfInspectedChains+1);
			showChainMessage (ss.str());
	        showMarker(xCoord, yCoord, "green", ++numOfInspectedChains);
	    	isMissionFinished();
	//        if (isFinalChain)
	//			Notify ("RETURN", "true");
        }
        else{
            dirtyResult = 0; //failed cleaning the chain
			isCleaningDone		= false;
            outputStr += "\tCleaning failed";
        	ss << "Chain " << (numOfInspectedChains+1) << " needs further cleaning";
			showChainMessage (ss.str());
            outputStr += doX();
        }
		Utilities::writeToFile("log/"+logFileName, outputStr);
		reportEvent(outputStr);
    }

//		m_previous_iterate = m_current_iterate;
//	}
	AppCastingMOOSApp::PostReport();               // Add this line
	return(true);
}


string UUV::doX(){
    stringstream ss;

    X= checkX();
    if (X){//try to clean the chain
        double holdingT     =  dirtyDistribution(generator);
        double holdingR     =  1/holdingT;

        dirtyHoldingTSum   += holdingT;
        dirtyHoldingRSum   += holdingR;
        cleaningCount ++;
        chainsVector[numOfInspectedChains].holdingTimeSum += holdingT;

	    dirtyHoldingTCount ++;
	    chainsVector[numOfInspectedChains].countSum += 1;

//		Utilities::writeToFile("log/"+logFileName, outputStr);
//		reportEvent(outputStr);		
		Notify ("STATION_KEEP", "false"); Notify ("CLEAN", "true");
		string point = "#polygon = label=chain, format=lawnmower, x=" +xCoord +", y=" +yCoord +", height=10, width=20, lane_width=5";
//					string point = "#polygon = label=chain, format=radial, x=" +xCoord +", y=" +yCoord +", radius=10, pts=3, snap=1";//
		Notify("CLEANING_UPDATES", "name="+intToString(cleaningCount)+point);
		isAuvCleaning = true;
    	ss << "UUV is cleaning chain " << (numOfInspectedChains+1) ;
		showChainMessage (ss.str());
    	showMarker(xCoord, yCoord, "magenta", (numOfInspectedChains+1));
		return "\n\t" + intToString(dirtyHoldingTCount) + ")\tHolding time=" + doubleToString(holdingT) +"\n";
	}
   else { //skip cleaning the chain, go to the next chain
//      dirtyResult = 2;
        double travelT = travelDistribution(generator);
//		Utilities::writeToFile("log/"+logFileName, outputStr);
//		reportEvent(outputStr);		
		isAuvCleaning 		= false; 
		isCleaningDone		= false;
		new_chain_visited	= false;
		Notify ("STATION_KEEP", "false"); 
		Notify ("CLEAN", "false");
    	ss << "UUV skips cleaning chain " << (numOfInspectedChains+1) ;
		showChainMessage (ss.str());
    	showMarker(xCoord, yCoord, "white", ++numOfInspectedChains);
    	isMissionFinished();
//        if (isFinalChain)
//			Notify ("RETURN", "true");
        return "\n\tSkipping current chain, going to the next, Travelling time: " + doubleToString(1/travelT) +"\n\n";
	}
}


int UUV::checkX()
{
    // return 1;
    //string paramsToServer = doubleToString(dirtyHoldingTCount) +","+ doubleToString(dirtyHoldingTSum) +"\n";
    string paramsToServer = "6,"+ intToString(numOfInspectedChains+1) 	+
    						","+ intToString(chainsVector[numOfInspectedChains].countSum)+
    						","+ doubleToString(chainsVector[numOfInspectedChains].holdingTimeSum) +"\n";
    //doubleToString(dirtyHoldingTCount) +","+ doubleToString(dirtyHoldingTSum) +"\n";
	reportEvent("Sending\t" + paramsToServer);

    char variables[256];// = "5,4,4,95,90,85,1,5,3.5,0\n";
    //memset(variables, 0, sizeof(char)*(v));
    strcpy(variables, (paramsToServer.c_str()));

    //Model Checker
	// runClient(variables);
	// string received = "Received:\t" + string(variables);
	// reportEvent(received);
    // return atoi(variables);

    //SIMULATION
    int b = xDistribution(generator);
    Notify("checkX", b);
	//string received = "Received:\t" + intToString(b);
	//reportEvent(received);
    return b;
}


void UUV::isMissionFinished(){
	if (isFinalChain){
		//ss << "UUV finised mission";
		Notify ("RETURN", "true");
		showChainMessage ("UUV finised mission");
	}
}


void UUV::showMarker (string xPos, string yPos, string color, int ID){
//  Notify("VIEW_MARKER", "type=circle,x=" + xPos +",y=" + yPos +",scale=2,label=" + it->first +",color=" + sensorColor + ",width=12");
	showMarker (xPos, yPos, color, 8, ID);
}


void UUV::showMarker (string xPos, string yPos, string color, int size, int ID){
//  Notify("VIEW_MARKER", "type=circle,x=" + xPos +",y=" + yPos +",scale=2,label=" + it->first +",color=" + sensorColor + ",width=12");
	 Notify("VIEW_MARKER", "type=gateway,x=" + xPos +",y=" + yPos +",scale=2, color=" + color + ",width="+ intToString(size) +",label=chain"+ intToString(ID) +",msg="+intToString(ID));
}

void UUV::showChainMessage(string msg){
	 Notify("VIEW_MARKER", "type=circle,x=100,y=0,scale=10,color=black,width=1,label=chainStatus,msg="+ msg);
}


void UUV::initLegend(){
	 Notify("VIEW_MARKER", "type=circle,x=50,y=50,color=orange,width=6,label=insps,  msg=-chain to be inspected");
	 Notify("VIEW_MARKER", "type=circle,x=100,y=50,color=violet,width=6,label=violet,msg=-chain currently being cleaned");
	 Notify("VIEW_MARKER", "type=circle,x=50,y=35,color=cyan,width=6,label=cyan,	 msg=-no cleaning needed");
	 Notify("VIEW_MARKER", "type=circle,x=100,y=35,color=green,width=6,label=green,  msg=-chain cleaned");
	 Notify("VIEW_MARKER", "type=circle,x=50,y=20,color=white,width=6,label=white,   msg=-chain skipped");
	 Notify("VIEW_MARKER", "type=circle,x=100,y=20,color=red,width=6,label=red,      msg=-mission failed catastrophic failure");
}



//---------------------------------------------------------
// Procedure: buildReport
//---------------------------------------------------------
bool UUV::buildReport()
{
	m_msgs << "UUV \t" << m_uuv_name << "mission log:\n" << endl ;

	m_msgs << logger.str();

//	m_msgs << "UUV Sensors (" << m_uuv_sensors.size() << ")" << endl;
//	m_msgs << "------------------------------------------------"   << endl;
//	for (unsigned i=0; i<m_uuv_sensors.size(); i++){
//		m_msgs << m_uuv_sensors.at(i) << endl;
//	}

	m_msgs << endl << endl;

	return true;
}


//---------------------------------------------------------
// Procedure: handle survey point
// check if the provided string is in the format SENSOR_1SEart:end:degradationPercentage
// e.g. 60,-40:40,-90:60,-140:150,-140:170,-90:150,-40
//---------------------------------------------------------
void UUV::handleChainsCoords(string s)
{
	vector<string> v = parseString(removeWhite(s),":");//60,-40:40,-90:60,-140:150,-140:170,-90:150,-40

	//check if all tokens are alphanumerics
	for (vector<string>::iterator it = v.begin();  it != v.end(); it++){ //60,-40
		vector<string> points = parseString(*it, ",");

		for (vector<string>::iterator itP = points.begin();  itP != points.end(); itP++){ //60,-40
			if (!isNumber(*itP))
				reportConfigWarning("Problem with configuring 'SENSORS ="+ s +"': expected alphanumeric but received " + *itP);
			else
				//if everything is OK, create a sensor element and add it to the vector
				chainCooords.push_back(*itP);			
		}
	}

	std::vector<int>::size_type sz = chainCooords.size();
	for (int i=0; i<sz; i+=2){
		CHAIN chain;
		chain.ID 				= i;
		chain.countSum 			= 0;
		chain.holdingTimeSum	= 0;
		chainsVector.push_back(chain);
	}

}


//---------------------------------------------------------
// Procedure: initServer
//---------------------------------------------------------
void UUV::initServer()
{
//  //When acting as a SERVER
//	initialiseServer(PORT);
//	pthread_t thread;
//	int n = pthread_create(&thread, NULL, runServer, NULL);
//	int n = pthread_create(&thread, NULL, runServer2,  (void *) &m_sensors_map);

//  //When acting as a CLIENT
	//initialiseClient(PORT);
}




//---------------------------------------------------------
// Procedure: initSensorsMap
//---------------------------------------------------------
void UUV::initSensorsMap()
{
//	for (vector<string>::iterator it = m_uuv_sensors.begin();  it != m_uuv_sensors.end(); it++){
//		Sensor newSensor;
//		newSensor.name		 		= *it;
//		newSensor.averageRate 		= 0;
//		newSensor.numOfReadings 	= 0;
//		newSensor.numOfSuccReadings	= 0;
//		newSensor.state				= -1;
//		newSensor.time				= MOOSTime(true);
//		newSensor.other				= 0;
//		m_sensors_map[*it] 			= newSensor;
//	}

//	//Dummy workaround for getting the speed value from controller
//	//have a sensor element in sensor map with SPEED name. time is the speed value
//	Sensor newSensor;
//	newSensor.name		 		= "SPEED";
//	newSensor.averageRate 		= 0;
//	newSensor.numOfReadings 	= 0;
//	newSensor.numOfSuccReadings	= 0;
//	newSensor.state				= -1;
//	newSensor.time				= 0;
//	newSensor.other				= m_uuv_speed;
//	m_sensors_map["SPEED"] 		= newSensor;
}