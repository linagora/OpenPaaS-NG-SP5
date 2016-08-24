cleaning_meeting_text <-
function(X, custom, pos, stem) {
  
	# remove between word dashes
	x = gsub("- ", " ", X, perl = TRUE) 
	x = gsub(" -", " ", x, perl = TRUE)
	
	# remove everything within curly brackets ({vocalsound}, etc)
	x = gsub("{.*?}", '', x, perl = TRUE)

	# remove space before apostrophes
	x = gsub("\\b\\s+'\\b", "'", x, perl=TRUE)

	# remove punctuation except apostrophes (the only punctuation mark featured in the ASR output)
	x = gsub("[^[:alnum:][:space:]']", " ", x)

	# remove apostrophes that are not intra-word
	x = gsub("' ", " ", x, perl = TRUE)
	x = gsub(" '", " ", x, perl = TRUE)

	# remove leading and trailing white space
	x = str_trim(x,"both")

	# remove extra white space
	x = gsub("\\s+"," ",x)

	# convert to lower case
	x = tolower(x)

	# make a copy of tokens without further preprocessing
	xx = unlist(strsplit(x, split=" "))

	# remove stopwords
    index_stopwords = which(xx%in%custom)
  
  if(length(index_stopwords)>0){
    
    x = xx[-index_stopwords]
    
  }

    if (length(x)>0){
      
      if(any(nchar(x)>0)){
    
	if (pos == TRUE) {
		# retain nouns and adjectives
		x_tagged = tagPOS(x)$output
		index = which(x_tagged%in%c("NN","NNS","NNP","NNPS","JJ","JJS","JJR"))
		if (length(index)>0){
			x = x[index]
		}
	}

	if (stem == TRUE){
		x = wordStem(x)
		xx = wordStem(xx)
	}

      }
    }
    
    # remove blanks or one letter elements
    index = c(which(x==""),which(nchar(x)<2))
    if (length(index)>0){
      x = x[-index]
    }
    
    index = c(which(xx==""),which(nchar(xx)<2))
    if (length(index)>0){
      xx = xx[-index]
    }

	output = list(unprocessed=xx, processed=x)

}
