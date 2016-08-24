from_keyword_to_summary_submodularity = function(graph_keywords_scores_temp, utterances, start_time, to_stem, max_summary_length, scaling_factor, weighted_sum_concepts, negative_terms, lambda){
  
	extracted_keywords = graph_keywords_scores_temp$extracted_keywords
	scores = graph_keywords_scores_temp$scores

	units = utterances
	units_splitted = lapply(units, function (x) unlist(strsplit(x, split=" ")))
	units_splitted_stemmed = lapply(units_splitted, wordStem)

	keywords_temp = extracted_keywords
	scores_temp = scores
	names(scores_temp) = keywords_temp
	scores_temp = round(scores_temp/sum(scores_temp),4)
	budget = max_summary_length

	output = sentence_extraction_submodularity(units_splitted, units_splitted_stemmed, keywords_temp, scores_temp, to_stem, budget, start_time, scaling_factor, weighted_sum_concepts, negative_terms, lambda)

	my_summary = output$surviving_sentences
	my_start_times = output$start_times


	my_summary = unlist(my_summary)
	my_start_times = unlist(my_start_times)

	# if any, remove empty utterances
	index_to_remove = which(unlist(lapply(my_summary, nchar))==0)
	if (length(index_to_remove>0)){
		my_summary = my_summary[-index_to_remove]
		my_start_times = my_start_times[-index_to_remove]
	}

	index_to_remove = which(my_start_times==-999)
	if (length(index_to_remove>0)){
		my_summary = my_summary[-index_to_remove]
		my_start_times = my_start_times[-index_to_remove]
	}

	# sort summary in chronological order
	temporal_index = order(my_start_times, decreasing=FALSE)
	my_summary = my_summary[temporal_index]

	# output in right format for scoring with ROUGE
	my_summary = paste0(my_summary, ".")

	output = list(my_summary = my_summary)
  
}