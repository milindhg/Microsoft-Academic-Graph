# Microsoft Academic Graph
  Big Data project for coursework Z604 Big Data Analytics
###### Team Members
	* ABHISHEK ANAND SINGH (aasingh@indiana.edu)
	* MILIND GOKHALE (mgokhale@indiana.edu)
	* RENUKA DESHMUKH (renudesh@indiana.edu)
	* SANTHOSH SUNDARAJAN (soundars@umail.iu.edu)
	* RITESH AGARWAL (riteagar@iu.edu)


##### Task 1: Citation Recommendation Problem
###### In task 1, given a paper id, we tried to predict Paper References (citation) based on the paper keywords, authors and other such data. This is an important problem as it can be used to recommend other reading material for researchers similar to the paper they are interested in. 

*>SAMPLING:*

	1. Sampling.java: 
		This class primarily does the work of sampling. We started with a random sample of 100,000 papers and fetched corresponding data for these papers from various other tables. 
				
*>DATA PREPARATION:*

	1. JsonToNeo4J.java: 
		This class primarily converts the json input file to separate csv files as per the tables so that they can be imported as nodes and relationships in neo4j database.

	2. Paper.java:
		This is a data class for holding Paper objects in the various tasks and other files.
		
*>PREDICTION AND RANKING:*

	1. PaperScore.java: 
		This is a data class for holding the paper scores of the papers and for storing the calculated final score.
		
	2. PaperScoreNormVars.java:
		This class primarily contains static variables for saving the Normalization variables. These variables helped in normalizing the scores of the Author Popularity, Paper rank, Keyword match and Paper year scores. 

	3. PaperScoreWeights.java:
		This is an ENUM to hold the weight given to the various scores in the Paper scores calculation.

	4. Prediction.java:
		This class primarily works on the predicting the citation recommendations for various papers using Recommendation class. This class gives output in the steps of 20 from 20 predictions to 100 predictions.

	5. Recommendation.java:
		This class primarily does the work of ranking the citation recommendations. The scores are calculated, final score is calculated and the recommendations are sorted in descending order of the final score.

##### Task 2: Predict Papers with most relevant Keywords
###### In task 2, given a paper id, we try to predict key words that the paper can be tagged with. To run the below given python files, the PaperCollection.json is required to compute page rank on the 100000 paper records present in it. 

			Important Files :
			PageRank_Rec.py:-  Implements Page Rank and compute most relevant paper keywords for a target paper.  
			keyWordCloud.ipynb:- Used to visualize our output pertaining to predicting keywords. 
			PaperCollection.json:- Used for storing the subset of the papers data in the .json format.
			buildGraphFromPy.json:-Used for build a graph based on schema designed from the .py env. 

##### Evaluation
	
####### Task 1 
When testing for dataset of different sizes we found that we get the best results from the largest dataset which has details about 100,000 papers and all its references. Increasing the dataset size from 1000 to 100,000 papers saw an increase in the overall precision and recall. 
Hence, we decided to primarily work with the largest sampled dataset. With this dataset, we varied the number of prediction size from 20 to 100 in steps of 20. We found that we got the best precision with 60 predictions, with the value of 18%. 

###### Task 2 
We computed the precision recall measures for this task and found some unstable results since the data under consideration is a subset and building a ground truth on this sub-set either gave us exact match of 100%, 50% or no match in most of the cases since we found only 1 or 2 PRefIds for the ground truth which can either be present in the predicted list or not be present.

