## -------------------------------------------------------------
## Code to create, edit and query collections in MONGODB
 
## @Author - Abhishek Anand Singh
## IUMail - aasingh@indiana.edu
--------------------------------------------------------------

# install.packages('rmongodb')
# install.packages('wordcloud')
install.packages('RColorBrewer')
library('rmongodb')
library('plyr')
library('tm')
library('wordcloud')
library('RColorBrewer')
m <- mongo.create(host = "localhost")
mongo.is.connected(m)
mongo.get.databases(m)
DBNS= "ILS-PRJ.Papers"
mongo.find.one(mongo = m, ns = DBNS)

################################################
##             Creating Paper Collection
################################################

papers = data.frame(stringsAsFactors = F)

# making a cursor and dataframe for papers
c <- mongo.find(m, DBNS)
while(mongo.cursor.next(c)) { 
  tmp = mongo.bson.to.list(mongo.cursor.value(c)) #next record grabbed
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE) # making data frames
  papers = rbind.fill(papers, tmp.df)
}
dim(papers)
head(papers)
str(papers)

################################################
##             Creating Author Collection
################################################

# making above for authors
authors <- data.frame(stringsAsFactors = F)
authorsC <- "ILS-PRJ.Author"
mongo.find.one(m,authorsC)
cAuthors <- mongo.find(m,"ILS-PRJ.Author")
while(mongo.cursor.next(cAuthors)) { 
  tmp = mongo.bson.to.list(mongo.cursor.value(cAuthors)) #next record grabbed
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE) # making data frames
  authors = rbind.fill(authors, tmp.df)
}
dim(authors) # dimension of author
head(authors) # top five values in author
str(authors) #structure of authors dataframe


# another cursor and dataframe for PapersAuthorAffiliations
PAAffiliation <- data.frame(stringsAsFactors = F)
PAAffiliationC <- "ILS-PRJ.PapersAuthorAffiliations" 
mongo.find.one(m,PAAffiliationC)
cPAAffiliation <- mongo.find(m, PAAffiliationC)
while(mongo.cursor.next(cPAAffiliation)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cPAAffiliation))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  PAAffiliation = rbind.fill(PAAffiliation, tmp.df)
}
dim(PAAffiliation)
head(PAAffiliation)
str(PAAffiliation)


# ConferenceInstances
ConferenceInstances <- data.frame(stringsAsFactors = F)
ConferenceInstancesC <- "ILS-PRJ.ConferenceInstances" 
mongo.find.one(m,ConferenceInstancesC)
cConferenceInstances <- mongo.find(m, ConferenceInstancesC)
while(mongo.cursor.next(cConferenceInstances)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cConferenceInstances))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  ConferenceInstances = rbind.fill(ConferenceInstances, tmp.df)
}
dim(ConferenceInstances)
head(ConferenceInstances)
str(ConferenceInstances)

# ConferenceSeries
ConferenceSeries <- data.frame(stringsAsFactors = F)
ConferenceSeriesC <- "ILS-PRJ.Conferences" 
mongo.find.one(m,ConferenceSeriesC)
cConferenceSeries <- mongo.find(m, ConferenceSeriesC)
while(mongo.cursor.next(cConferenceSeries)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cConferenceSeries))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  ConferenceSeries = rbind.fill(ConferenceSeries, tmp.df)
}
dim(ConferenceSeries)
head(ConferenceSeries)
str(ConferenceSeries)

# FieldOfStudy
FieldOfStudy <- data.frame(stringsAsFactors = F)
FieldOfStudyC <- "ILS-PRJ.FieldOfStudy" 
mongo.find.one(m,FieldOfStudyC)
cFieldOfStudy <- mongo.find(m, FieldOfStudyC)
while(mongo.cursor.next(cFieldOfStudy)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cFieldOfStudy))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  FieldOfStudy = rbind.fill(FieldOfStudy, tmp.df)
}
dim(FieldOfStudy)
head(FieldOfStudy)
str(FieldOfStudy)

# PaperKeywords
PaperKeywords <- data.frame(stringsAsFactors = F)
PaperKeywordsC <- "ILS-PRJ.PaperKeywords" 
mongo.find.one(m,PaperKeywordsC)
cPaperKeywords <- mongo.find(m, PaperKeywordsC)
while(mongo.cursor.next(cPaperKeywords)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cPaperKeywords))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  PaperKeywords = rbind.fill(PaperKeywords, tmp.df)
}
dim(PaperKeywords)
head(PaperKeywords)
str(PaperKeywords)

# PaperReferences
PaperReferences <- data.frame(stringsAsFactors = F)
PaperReferencesC <- "ILS-PRJ.PapersReferences" 
mongo.find.one(m,PaperReferencesC)
cPaperReferences <- mongo.find(m, PaperReferencesC)
while(mongo.cursor.next(cPaperReferences)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cPaperReferences))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  PaperReferences = rbind.fill(PaperReferences, tmp.df)
}
dim(PaperReferences)
head(PaperReferences)
str(PaperReferences)

################################################
# Trying to create a new field displaying paper and their referers
################################################


PaperID = 0
Referred = 0
referer = c()
paperReferred = data.frame(PaperID, Referred, stringsAsFactors = FALSE)
for(i in 1:nrow(PaperReferences)) {
  count= 0
  for(j in 1:nrow(PaperReferences)) {
    if(PaperReferences[i,2] == PaperReferences[j,3]){
      val <- PaperReferences[j,3]
      #referer<- append(referer, value = val )
      count= count+1
    }
  }
  paperReferred[i,1] = PaperReferences$PaperId[i]
  paperReferred$Referred[i] = count
}



Keyword_Referred <- data.frame(stringsAsFactors = F)
Keyword_ReferredC <- "ILS-PRJ.Keyword_Referred" 
mongo.find.one(m,Keyword_ReferredC)
cKeyword_Referred <- mongo.find(m, Keyword_ReferredC)
while(mongo.cursor.next(cKeyword_Referred)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cKeyword_Referred))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  Keyword_Referred = rbind.fill(Keyword_Referred, tmp.df)
}
dim(Keyword_Referred)
head(Keyword_Referred)
str(Keyword_Referred)


# SampledSortedPaperFrequency

SampledSortedPaperFrequency <- data.frame(stringsAsFactors = F)
SampledSortedPaperFrequencyC <- "ILS-PRJ.SampledSortedPaperFrequency" 
mongo.find.one(m,SampledSortedPaperFrequencyC)
cSampledSortedPaperFrequency <- mongo.find(m, SampledSortedPaperFrequencyC)
while(mongo.cursor.next(cSampledSortedPaperFrequency)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cSampledSortedPaperFrequency))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  SampledSortedPaperFrequency = rbind.fill(SampledSortedPaperFrequency, tmp.df)
}
dim(SampledSortedPaperFrequency)
head(SampledSortedPaperFrequency)
str(SampledSortedPaperFrequency)


# SampledSortedKeywordFrequency
SampledSortedKeywordFrequency <- data.frame(stringsAsFactors = F)
SampledSortedKeywordFrequencyC <- "ILS-PRJ.SampledSortedKeywordFrequency" 
mongo.find.one(m,SampledSortedKeywordFrequencyC)
cSampledSortedKeywordFrequency <- mongo.find(m, SampledSortedKeywordFrequencyC)
while(mongo.cursor.next(cSampledSortedKeywordFrequency)) {
  tmp = mongo.bson.to.list(mongo.cursor.value(cSampledSortedKeywordFrequency))
  tmp.df = as.data.frame(t(unlist(tmp)), stringsAsFactors = FALSE)
  SampledSortedKeywordFrequency = rbind.fill(SampledSortedKeywordFrequency, tmp.df)
}
dim(SampledSortedKeywordFrequency)
head(SampledSortedKeywordFrequency)
str(SampledSortedKeywordFrequency)


# Taking paper Author Affiliations

for(i in 1: nrow(paperReferred)){
  if(paperReferred$PaperID == PAAffiliation$PaperID){
    paperReferred <-
  }
}

#########################################################
# Make a Corpus of all keywords
#########################################################

keywordsAll <- paste(SampledSortedKeywordFrequency[3], sep = ",")
myCorpus <- Corpus(VectorSource(keywordsAll))
myCorpus = tm_map(myCorpus, content_transformer(tolower))
myCorpus = tm_map(myCorpus, removePunctuation)
myCorpus = tm_map(myCorpus, removeNumbers)
myCorpus = tm_map(myCorpus, removeWords, stopwords("english"))

myDTM = TermDocumentMatrix(myCorpus, control = list(minWordLength = 2))

m = as.matrix(myDTM)
v = sort(rowSums(m), decreasing = TRUE)

#########################################################
# Make a wordcloud of keywords
#########################################################

wordcloud(myCorpus, scale=c(5,0.5), max.words=500, random.order=FALSE, rot.per=0.35, use.r.layout=FALSE, colors=brewer.pal(8, "Dark2"))


#### END ####