# -*- coding: utf-8 -*-
"""
Created on Sat Apr 23 03:03:38 2016

@author: Santhosh
"""

import sys, os, re, requests 
from py2neo import neo4j, node, rel
from neo4jrestclient.client import GraphDatabase



inp_file = '/Users/Santhosh/PaperCollection2.json'
papers = []
keyword_dict = {}
author_dict_pid = {}
author_dict_kw = {}        

f_obj = open(inp_file)

for line in f_obj:
    try:
        kw = re.findall('"KW":"([^"]*)', line)
        authIds = re.findall('"AuthIds":"([^"]*)',line)
        kw = kw[0].split("|")
        authIds = authIds[0].split("|")
        pap_id = re.findall('"PId":"([^"]*)', line)[0]
        papers.append(pap_id)        #add(pap_id)
        for i in kw:
            if i in keyword_dict:
                keyword_dict[i].append(pap_id)
            else:
                keyword_dict[i] = [pap_id]
    
        for i in authIds:
            if i in author_dict_pid:  
                author_dict_pid[i].append(pap_id)
            else:
                author_dict_pid[i] = [pap_id]

    except Exception, e:
        print e, type(i)

print "no. of papers:                " + str(len(papers))
print "no. of unique keywords        " + str(len(keyword_dict))
print "no. of unique authors         " + str(len(author_dict_pid))


#building Graph
#change uid pwd as per your sys 
gdb = GraphDatabase("http://localhost:7474/db/data", username="neo4j", password="sandyNeoClient")

paperLabels = gdb.labels.create("PaperNodes")
authorLabels = gdb.labels.create("AuthorNodes")
KeyWordLabels = gdb.labels.create("KeyWordNodes")
uniqueKeyword = keyword_dict.keys()


def createPapersNodes():
    for p in range(0, (len(papers))):
        pid= papers[p]
        pid = gdb.nodes.create(paperId=pid)
        paperLabels.add(pid)

def createKwNodes():    
    for k in range(0, (len(uniqueKeyword))):
        kwd = uniqueKeyword[k]
        kwd = gdb.nodes.create(keyWord = kwd)
        KeyWordLabels.add(kwd)
        #kwdStr = uniqueKeyword[k]
        for i in range(0, len(keyword_dict[kwd])):
            kwd.relationships.create("appearInPaper",keyword_dict[kwd][i])

createPapersNodes()
createKwNodes()


uniqueAuthor = author_dict_pid.keys()
def createAuthorNodes():    
    for a in range(0, (len(uniqueAuthor))):
        aid = uniqueAuthor[a]
        aid = gdb.nodes.create(authorID = aid)
        authorLabels.add(aid)
 
createAuthorNodes()   





# ----------------- Rough wrk ----------------------------------
    
  

# a dict to store keyword and pid as {kw1:[pid1, pid2...], {kw2}...}
#keyword_dict = {}

#paper id
#papers

     
        # = gdb.nodes.create(keyWord = kwd)  

#paper.relationship.create("hasKw", key)
# next step is to create relationships 
#    alice.relationships.create("Knows", bob, since=1980)
        






'''


        
uniqueKeyword = keyword_dict.keys()
def createKPRelationship():    
    for k in range(0, (len(uniqueKeyword))):
        kwd = uniqueKeyword[k]
        for i in range(0, len(keyword_dict[kwd])):
            kwd.relationships.create("appearInPaper",keyword_dict[kwd][i])
 
        
        kwdStr = uniqueKeyword[k]
        for i in range(0, len(keyword_dict[kwdStr])):
            kwd.relationships.create("appearInPaper",keyword_dict[kwdStr][i])
'''


'''

uniqueKeyword = keyword_dict.keys()
def relatePapersToKw():
    for pk in range(0, (len(uniqueKeyword))):
    
    

def create(cls, name, *emails): 
        person_node, _ = graph_db.create(node(name=name), 
                  rel(cls._root, "PERSON", 0)) 
        for email in emails: 
            graph_db.create(node(email=email), rel(cls._root, "EMAIL", 0),
                      rel(person_node, "EMAIL", 0)) 
        return Person(person_node) 
   
   
   
# Connect to graph and add constraints.
neo4jUrl = os.environ.get('NEO4J_URL',"http://localhost:7474/db/data/")
graph = neo4j.GraphDatabaseService(neo4jUrl)

# Add uniqueness constraints.
#neo4j.CypherQuery(graph, "CREATE CONSTRAINT ON (q:Question) ASSERT q.id IS UNIQUE;").run()

# Build URL.
#apiUrl = "https://api.stackexchange.com/2.2/questions...." % (tag,page,page_size)
# Send GET request.
json = requests.get(apiUrl, headers = {"accept":"application/json"}).json()

query = 
WITH {json} as data
UNWIND data.items as q
MERGE (question:Question {id:q.question_id}) ON CREATE
  SET question.title = q.title, question.share_link = q.share_link, question.favorite_count = q.favorite_count

MERGE (owner:User {id:q.owner.user_id}) ON CREATE SET owner.display_name = q.owner.display_name
MERGE (owner)-[:ASKED]->(question)

FOREACH (tagName IN q.tags | MERGE (tag:Tag {name:tagName}) MERGE (question)-[:TAGGED]->(tag))
FOREACH (a IN q.answers |
   MERGE (question)<-[:ANSWERS]-(answer:Answer {id:a.answer_id})
   MERGE (answerer:User {id:a.owner.user_id}) ON CREATE SET answerer.display_name = a.owner.display_name
   MERGE (answer)<-[:PROVIDED]-(answerer)
)



# Send Cypher query.
neo4j.CypherQuery(graph, query).run(json=json)


'''