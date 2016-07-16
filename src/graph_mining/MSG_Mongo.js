/// -------------------------------------------------------------
/// Code to transform data in MONGODB
 
/// @Author - Abhishek Anand Singh
/// IUMail - aasingh@indiana.edu
/// --------------------------------------------------------------


/// ----------------------------------------------------
 // Performing aggregation on papers based on times referenced
 /// ----------------------------------------------------
  
db.PapersReferences.aggregate(
    [
    {
        $group : { 
                _id : "$ PaperreferenceID",
                TimesReferenced : {$sum : 1},
                }
    },
    {
        $out : "ModPaperReference"
    }
    ],
    {
        allowDiskUse:true,
        cursor:{}
    }

)



 /// ----------------------------------------------------
 // Performing left join in mongodb to collect number of citations for each paper
 /// ----------------------------------------------------
    db.PaperKeywords.aggregate([{
          $lookup:
            {
              from: "ModPaperReference",
              localField: "PaperID",
              foreignField: "_id",
              as: "Referred"
            }
        }
    ])


 /// ----------------------------------------------------
 // Aggregation for keyword frequency
 /// ----------------------------------------------------

db.Keyword_Referred.aggregate( [
        { "$project": 
            {   
                "_id" : 0,
                "PaperID": 1,
                " KeyWordName" : 1,
                "TimesReferenced" : "$Referred.TimesReferenced"   
            }
            
        },
        {
            $out :"KeywordFrequency"
        }
],
    {
        allowDiskUse:true,
        cursor:{}
    }
)
    
    
db.KeywordFrequency.aggregate([{$unwind : "$TimesReferenced"}, {$out : "FinalKeywordFrequency"} ])
    

db.FinalKeywordFrequency.aggregate(
[
    {
        $match : {"PaperID" : {$ne : 0}}
    },
    {
        $sort : { TimesReferenced : -1 }
    },
    {
        $out : "SortedKeywordFrequency"
    }
],
    {
        allowDiskUse:true,
        cursor:{}
    }
)
    
    
db.SortedKeywordFrequency.stats()
db.FinalKeywordFrequency.stats()

// -------------------------------------------
// Doing analysis on data based on several criterias
// -------------------------------------------

// By Publish Year

db.SortedPaperFrequency.aggregate(
[
    {
        $match : {$and : [ {"PaperID" : {$ne : 0} }, {" PublishYear" : {$gt : 1900}} ]}
    },
    {
        $sort : { " PublishYear" : -1 }
    }
],
    {
        allowDiskUse:true,
        cursor:{}
    }
)
 
 
// By Paper Rank
 

db.SortedPaperFrequency.aggregate(
[
    {
        $match : {"PaperID" : {$ne : 0}}
    },
    {
        $sort : { " PaperRank" : -1 }
    }
],
    {
        allowDiskUse:true,
        cursor:{}
    }
)
    
//By TimeReferenced
db.SortedPaperFrequency.find()
 
// Sampled Data

db.SortedKeywordFrequency.aggregate(
    [{ 
        $sample : {size : 200000}
    },
    {
        $out : "SampledSortedKeywordFrequency"
    }
    ])
    
    
db.SortedPaperFrequency.aggregate(
    [{ 
        $sample : {size : 200000}
    },
    {
        $out : "SampledSortedPaperFrequency"
    }
    ])
 
// Test Queries

db.Papers.aggregate([{
      $lookup:
        {
          from: "ModPaperReference",
          localField: "PaperID",
          foreignField: "_id",
          as: "TimesReferred"
        }
    },
    {
        $out : "Papers_Referred"
    }
])
  
    
    
db.Papers_Referred.aggregate( [
        { "$project": 
            {   
                "_id" : 0,
                "PaperID": 1,
                " PublishYear" : 1,
                " PaperRank" : 1,
                "TimesReferenced" : "$TimesReferred.TimesReferenced"   
            }
            
        },
        {
            $out :"PaperFrequency"
        }
],
    {
        allowDiskUse:true,
        cursor:{}
    }
)
    

    
db.PaperFrequency.aggregate([{$unwind : "$TimesReferenced"}, {$out : "FinalPaperFrequency"} ])


        
db.getCollection('FinalPaperFrequency').find({"PaperID" : {$ne : 0}}).sort({"TimesReferenced" : -1}).limit(300000)



db.FinalPaperFrequency.aggregate(
[
    {
        $match : {"PaperID" : {$ne : 0}}
    },
    {
        $sort : { TimesReferenced : -1 }
    },
    {
        $out : "SortedPaperFrequency"
    }
],
    {
        allowDiskUse:true,
        cursor:{}
    }
)