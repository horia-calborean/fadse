# Compute Metrics with FADSE

#Introduction 

In this page we are providing a short tutoril on how to compute the following metrics with FADSE (from one folder, from multiple folders): hypervolume, two set difference hypervolume, coverage, number of unique individuals, 

# Details

## Hypervolume

### A Single Run

You are going to use this when you have a single run and want to determine the convergence speed or something else.

Start your IDE and open the **ComputeMetrics** class. Run this class and follow the steps on the screen (path to folder where the filled######.csv files are located, population size, number of objectives).

This class computes several metrics (in a new folder with the format metrics######### ): - hypervolume - number of unique generated individuals (unique.csv) = number of simulations performed - number of individuals that reached the parent population (population_increase.csv) - from the total offspring generated only a part are good enough to survive to the next parent population. The total number is saved in the before mentioned csv file. - some information about how long it took to compute the metrics (info.txt) - "7 Point" average distance metric in file 7point.csv - a file is created outside this folder called "files.csv" that contains a list of all the "filled" files. This can be used for scripts like the ones we id in the "R" scripting tool.

The hypervolume.csv file contains two columns. The first column is the hypervolume of the current generation (first line = generation 1, second line = generation 2, etc.). The second column is the hypervolume of all the individuals produced up to that point (an algorithm might loose good individuals during the DSE process, thus the values from the first column might decrease sometimes) - this value should always be equal or larger than the one before. We recommend to use the values from the first column since they provide a better image about the convergence of the algorithm. If you want to show the best possible hypervolume with everything the algorithm has found use the second column. 

### Multiple Runs

Open the class **HypervolumeFromNFolders** and run it. Follow the steps from the screen: specify number of runs you want to compare, number of objectives, population size. To change this behavior (to compare runs with different population sizes for example) change the HypervolumeHelperResult.ReadDirectories() method.

After this you have to specify the path to the folders where the filled######.csv files are located. After the computation is finished several files are created in the "metricsComposed#######" folder. The files will have a format like: hypervolume0.csv , hypervolume1.csv, etc. (the same with 7point).

These files contain the hypervolumes computed with the same reference point for all the folders specified before. The order in which the user gave the files is the order of these files. The user now has to combine all these files into a single one to obtain the file from where it can compare the hypervolumes. We recommend to use the first column from each file (see previous paragraph). 

## Coverage Metric

This metric can be computed between two populations, or between two runs. Only the example that compares two runs is presented.

Open and run the class CoverageFromTwoFolders. Follow the steps on the screen (path to folder where the filled#####.csv files are situated, etc.).

This metric will generate a new folder metricsComposed##### and inside a new file: coverage.csv This file will contain values on two columns. The first column will contain the values for the coverage C(folder 1, folder 2) while the second column will contain C(folder 2, folder 1) 
