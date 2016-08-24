overlap_html = function(my_summary, golden_summary, custom_stopwords){

	# separate punctuation from words
	my_summary = gsub('(?<=[,:;!.?])  (?=[,:;!.?])',' ',gsub("([,:;!?]|[.]+)", " \\1 ", my_summary),perl=T)

	# separate punctuation from words
	golden_summary = gsub('(?<=[,:;!.?])  (?=[,:;!.?])',' ',gsub("([,:;!?]|[.]+)", " \\1 ", golden_summary),perl=T)

	golden_split = unlist(strsplit(tolower(golden_summary), split=" "))
	summary_split = unlist(strsplit(my_summary, split=" "))

	golden_split_stem = wordStem(golden_split)
	summary_split_stem = wordStem(summary_split)

	overlap = unique(intersect(golden_split_stem,summary_split_stem))

	overlap_final_index = unlist(lapply(1:length(overlap), function(x){if(!overlap[x]%in%c(custom_stopwords,".")){return(x)}}))
	overlap_final = overlap[overlap_final_index]

	index_bold_golden = which(golden_split_stem%in%overlap_final)
	index_bold_summary = which(summary_split_stem%in%overlap_final)

	summary_split[index_bold_summary] = paste("<b>",summary_split[index_bold_summary],"</b>")
	my_html = summary_split

	golden_split[index_bold_golden] = paste("<b>",golden_split[index_bold_golden],"</b>")
	golden_html = golden_split

	output = list(my_html = my_html, golden_html = golden_html)

}