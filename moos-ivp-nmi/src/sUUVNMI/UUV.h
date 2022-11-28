/************************************************************/
/*    NAME: Simos Gerasimou                                              */
/*    ORGN: MIT                                             */
/*    FILE: UUV.h                                          */
/*    DATE:                                                 */
/************************************************************/

#ifndef UUV_HEADER
#define UUV_HEADER

#include "MOOS/libMOOS/Thirdparty/AppCasting/AppCastingMOOSApp.h"
#include "MBUtils.h"

#include <map>

//new libraries
#include <random>
#include <stdlib.h>     /* srand, rand */

class UUV : public AppCastingMOOSApp
{
	 public:
	   UUV();
	   ~UUV();
	   bool buildReport();


	 protected:
	   bool OnNewMail(MOOSMSG_LIST &NewMail);
	   bool Iterate();
	   bool OnConnectToServer();
	   bool OnStartUp();
	   void RegisterVariables();

	 private:
	   void initSensorsMap();
	   void initServer();

////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
	private:
	   //new methods
		void initialiseSimulationParameters();
	   	int checkX ();
	   	std::string doX();
	   	void logic();
	   	void showMarker (std::string xPos, std::string yPos, std::string color, int ID);
	   	void showMarker (std::string xPos, std::string yPos, std::string color, int size, int ID);
	   	void showChainMessage (std::string msg);
	   	void initLegend();
	   	void handleChainsCoords(std::string value);
		void isMissionFinished();
	   //new parameters;
		int    X            = 1;
		double x_OK         = 0.9;
		double p_OK         = 0.1;
		double r_inspect    = 1/5.0;
		double r_travel     = 1/4.0;

		double r_clean      = 0.00001;
		double r_fail_clean = 0.5;
		double r_damage     = 0.000001;
		double r_retry      = 1;

		std::string log="log/";		

	    double dirtyHoldingTSum = 0;
	    double dirtyHoldingRSum = 0;
	    int dirtyHoldingTCount  = 0;

	    double retryTime;
	    //double holdingTimeSum = 0;
	    double cleaningCount 	= 0;

	    double sumProb          ;//    = r_clean + r_fail_clean + r_damage;
	    double normCleanProb    ;//    = r_clean/sumProb;
	    double normFailCleanProb;//    = r_fail_clean/sumProb;
	    double normDamageProb   ;//    = r_damage/sumProb;


		std::default_random_engine generator;
		//Bernoulli distribution for X
		std::bernoulli_distribution xDistribution;//(x_OK);
	    //Bernoulli distribution for P
	    std::bernoulli_distribution pDistribution;//(p_OK);
	    //Exponential distribution for r_inspect
	    std::exponential_distribution<double> inspectDistribution;//(r_inspect);
	    //Exponential distribution for r_travel
	    std::exponential_distribution<double> travelDistribution;//(r_travel);
	    //Exponential distribution for r_retry
	    std::exponential_distribution<double> retryDistribution;//(r_retry);    
	    //Exponential distribution for r_damage
	    std::exponential_distribution<double> dirtyDistribution;//(r_damage + r_clean + r_fail_clean);


	   bool new_chain_visited;
	   bool auv_steady;
	   bool isAuvCleaning   = false;
	   bool isCleaningDone  = false;
	   bool is_station_keep = false;
	   int  chainX			= 0;
	   int  chainY			= 0;
	   bool isFinalChain	= false;
	   int  numOfInspectedChains = 0;

	   std::string xCoord;
	   std::string yCoord;
   	   std::vector<std::string> chainCooords;
   	   std::string logFileName;
   	   int NUM_OF_CHAINS_TO_INSPECT;
       std::ostringstream logger;

		struct CHAIN {
		    int 	ID;
		    int   	countSum;
		    double 	holdingTimeSum;
		};
   	    std::vector<CHAIN> chainsVector;



////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
	 private: // Configuration variables
	   std::string m_uuv_name; //uuv name
	   std::vector<std::string> m_uuv_sensors;
	   double M_TIME_WINDOW;
	   int PORT;


	 private: // State variables
	   unsigned int m_iterations;
	   double       m_timewarp;

	   double m_app_start_time;
	   double m_current_iterate;
	   double m_previous_iterate;
	   double m_uuv_speed;




	 public:
	   struct Sensor{
	   	   public:
		   	   std::string toString()
		   	   {
		   		    std::string str = name +"\t\t"+ intToString(numOfReadings)
		   		    					   +"\t\t"+ doubleToString(averageRate,2) +"\t\t"+ intToString(state);
		   			return str;
		   	   }

		   	   std::string getSummary()
		   	   {
		   		    std::string str = name +":"+ doubleToString(averageRate,2)
		   		    					   +":"+ intToString(numOfReadings)
										   +":"+ intToString(numOfSuccReadings)
										   +":"+ intToString(state);
		   			return str;
		   	   }


		   	   void newReading(double value)
		   	   {
		   		   numOfReadings += 1;
		   		   averageRate	  = numOfReadings / (MOOSTime(true) - time);
		   		   if (value == 1){
		   			   numOfSuccReadings++;
		   		   }
		   	   }

		   	   void reset()
		   	   {
		   		   numOfReadings		= 0;
		   		   numOfSuccReadings	= 0;
				   averageRate  		= 0;
				   time					= MOOSTime(true);
		   	   }

		   public:
			   std::string name;
			   int numOfReadings;
			   int numOfSuccReadings;
			   double averageRate;
			   int state;
			   double time;
			   double other;

	   };
	   typedef std::map<std::string, Sensor> sensorsMap;

	   sensorsMap m_sensors_map;


};

#endif 
