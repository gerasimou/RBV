#--------------------------------------------------------
# The CMakeLists.txt for:                       sUUV
# Author(s):                              Simos Gerasimou
#--------------------------------------------------------

# FILE(GLOB SRC *.cpp)

SET(SRC
  UUV.cpp
  UUV_Info.cpp
  main.cpp
  Utilities.cpp
#  server/serverLoop.cpp
  client/client.cpp
)



ADD_EXECUTABLE(sUUVNMI ${SRC})

TARGET_LINK_LIBRARIES(sUUVNMI
   ${MOOS_LIBRARIES}
   mbutil
   m
   pthread)

