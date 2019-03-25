# AGV_Alogrithm

Introduction:
This is a isolated backend. To use it, you need to start the front end (AGV) from my repo.
This repo contains a routing algorithm for multiple AGVs regarding some tasks (node to node command).
The algorithm utlizes the genetic algorithm to schedule the tasks and time window based routing algorithm to achieve conflict-free solution.
This algorithm is not perfect solution for routing in the shortest distance, but it could be extended to that.
You can try it by running several tests in the AGV_GA_TEST in the algorithm directory. The test graph is in the TestGraphSet dir.
Or you can start the backend by running the Application.java and use npm run dev to start the front end to see the routing.
