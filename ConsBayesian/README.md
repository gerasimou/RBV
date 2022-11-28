## Running the Robotic Mission

The robotic mission is provided as a en Eclipse-based Java tool that uses Maven for managing the project and its dependencies, and for generating the executable jars.

1. Import the project in your IDE of preference

2. Set the following environment variable (In Eclipse go to Run / Run Configurations / Environment tab / New)

        OSX: DYLD_LIBRARY_PATH = libs/runtime 
        
        *NIX: LD_LIBRARY_PATH = libs/runtime

3. Specify the configuration parameters in file [config.properties](https://github.com/gerasimou/NMI/blob/main/ConsBayesian/config.properties)

4. Run the [AUV class](https://github.com/gerasimou/NMI/blob/main/ConsBayesian/src/main/java/caseStudy/chainInspection/AUV.java)



--- 
For questions and comments, please contact [Simos Gerasimou](mailto:simos.gerasimou@york.ac.uk)
