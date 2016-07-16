# -------------------------------------------------------------
## Test Code to create, edit and query nodes in Neo4J using Python package Py2Neo 
 
## @Author - Abhishek Anand Singh
## IUMail - aasingh@indiana.edu
# --------------------------------------------------------------

import json
from py2neo import Graph, authenticate
authenticate("localhost:7474", "neo4j", "abhi1992")
graph = Graph()

with open('/Users/Abhishek/Desktop/1samp.json') as data_file:
	json = json.load(data_file)
query = """
WITH {json} AS document
UNWIND document.papers AS paper
MERGE (p:Paper {PId: paper.PId})
MERGE (k1:Keyword {Key0: paper.KW0})
MERGE (k2:Keyword {Key1: paper.KW1})
MERGE (k3:Keyword {Key2: paper.KW2})
MERGE (k4:Keyword {Key7: paper.KW7})
MERGE (k1)-[:KEYWORD]->(p)
MERGE (k2)-[:KEYWORD]->(p)
MERGE (k3)-[:KEYWORD]->(p)
MERGE (k4)-[:KEYWORD]->(p)
RETURN paper.PId, paper.PRank, paper.KW0, paper.KW1, paper.KW2, paper.KW7
"""
print graph.cypher.execute(query, json = json)

##Output 
## {
## u'PublYear': 2008, 
## u'PRefIds': u'5D82C5E4|587A64D7|5A4CB248|0233D103|5B74E9C2|5BD9C7F1|137A2F83|6D46A002|6840DDAB|6DC727F2|707B3A51|5A765E23|5E43E5A4|59B0F480|0C33AD48|5D193415|0C6C6BDB|6DA73FA5|3.75E173|63E30B04|', 
## u'IsJrnl': False, 
## u'FOSIds': u'083736DA|0304C748|04EDB532|8.782E12|7868074|097C6C78|06ADBDFF|', 
## u'AuthIds': u'0E2CDBF9|49CC53F2|700180FB|', 
## u'KW': u'optimization problem|artificial neural network|external memory|genetic algorithm|objective function|evolutionary algorithm|explicit memory|',
## u'_id': 
## 		{
##      	u'$oid': u'571a4f41f6d58551e032ac3b'
##      }, 
## u'IsConf': True, 
## u'PId': u'5853E23C', 
## u'PRank': 19485
## }


