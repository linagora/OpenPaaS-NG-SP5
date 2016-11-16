######################
##### packages #######
######################
library(stringr)
library(textcat)
library(SnowballC)
library(igraph)
library(hash)
library(wordcloud2)
library(metricsgraphics)
# not cited at the bottom of the page cause part of base R
library(tools)

######################
##### functions ######
######################
source("from_keyword_to_summary_submodularity.R")
source("concept_submodularity_objective.R")
source("from_terms_to_keywords.R")
source("assign_attributes_to_graph_nocomm.R")
source("sentence_extraction_submodularity.R")
source("cores_dec.R")
source("assign_attributes_to_graph_initial.R")
source("clean_utterances.R")
source("cleaning_meeting_text.R")
source("cleaning_transcript.R")
source("from_cleaned_transcript_to_terms_list.R")
source("from_terms_to_graph.R")
source("from_terms_to_summary.R")
source("heapify.R")
source("keyword_extraction.R")
source("keyword_extraction_inner.R")
source("sentence_selection_greedy.R")
source("string_join.R")
source("utterance_collapse.R")
source("best_level_density.R")
source("get_elbow_point.R")
source("overlap_html.R")

##########################
overall_wd = getwd()

# load stopwords
custom_stopwords = read.csv('custom_stopwords_full.csv',header=FALSE,stringsAsFactors = FALSE)[,1]

# load filler words
filler_words = read.csv('filler_words.csv',header=FALSE,stringsAsFactors = FALSE)[,1]
golden_names = list.files(paste0("./golden_summaries/"))

shinyServer(function(input, output) {
  
  output$ui <- renderUI({
    
    switch(input$corpus,
           "AMI_corpus" = selectInput("input_data", label = "Select AMI test set meeting", choices = c("ES2004a","ES2004b","ES2004c","ES2004d","ES2014a","ES2014b","ES2014c","ES2014d","IS1009a","IS1009b","IS1009c","IS1009d","TS3003a","TS3003b","TS3003c","TS3003d","TS3007a","TS3007b", "TS3007c","TS3007d"),selected = "IS1009a"),
           
           "ICSI_corpus" = selectInput("input_data", label = "Select ICSI test set meeting", choices = c("Bed004","Bed009","Bed016", "Bmr005","Bmr019","Bro018"),selected = "Bed004"),
           
           "custom" =   fileInput("input_data", label = h5("Upload a utf-8 .csv file or tab separated .txt file with headers: `start', `end', `role', `text'", a("(example in English)",href = "https://github.com/Tixierae/examples/blob/master/example_input.csv", target="_blank"),a("(example in French)",href = "https://github.com/Tixierae/examples/blob/master/asr_info_french.txt", target="_blank")),accept=c('text/csv','text/plain'))
    )  
    
  })
  
  
  prelude = reactive({
    
    if (input$corpus!="custom"){
	
	dir.create("./rouge2.0-distribution/test-summarization/reference",recursive=T)
    dir.create("./rouge2.0-distribution/test-summarization/system")
    
      boolean = FALSE
      
      meeting_name = input$input_data
      
      # to avoid null error on start-up (UI loads after reactive)
      if(!is.null(meeting_name)){
        
        asr_info = read.table(paste0(overall_wd,"/ASR/",paste0(meeting_name,".da-asr")), header=FALSE, sep="\t", quote="")
        
        # retain only columns of interest
        asr_info = asr_info[,c(2,3,5,9)]
        colnames(asr_info) = c("start","end","role","text")
        
        meeting_info = cbind("number of participants"=length(unique(asr_info[,"role"])),"duration (mins)"=paste(round((-as.numeric(asr_info[1,"start"]) + as.numeric(asr_info[nrow(asr_info),"end"]))/60,2)), "size (words)"=length(unlist(lapply(as.character(asr_info[,"text"]), function(x) unlist(strsplit(x, split=" "))))),"number of human summaries"=c(1,3,"unknown")[input$corpus == c("AMI_corpus","ICSI_corpus","custom")])
        
      } else {
        
        boolean = TRUE
        meeting_info = cbind("number of participants"=NA,"duration (mins)"=NA, "size (words)"=NA,"number of human summaries"=NA)
        
      }
      
    } else {
      
      boolean = TRUE
	  	 
      meeting_info = cbind("number of participants"=NA,"duration (mins)"=NA, "size (words)"=NA,"number of human summaries"=NA)
	  
    }
    
    list(meeting_info = meeting_info, boolean=boolean)
    
  })
  
    
  output$"meeting_info"= renderTable(prelude()$meeting_info,include.rownames=FALSE)
  
  
  initial = eventReactive(input$goButton,{
    
	operating_system = .Platform$OS.type
	
    withProgress({
      setProgress(message = "Loading text...")
      
      boolean = isolate(prelude()$boolean)
      
      if (boolean==TRUE){
        # that is, if a custom .csv or space separated .txt file has been passed
        
        # input file needs be in proper format
        # there is nothing to write to the ROUGE directory in that case
        
        to_pass = isolate(input$input_data$datapath)
		
		#asr_info = read.csv(to_pass, header=TRUE)
		
		 if (file_ext(input$input_data$name) == 'csv'){
        
			 asr_info = read.csv(to_pass, header=TRUE, fileEncoding = 'utf-8')
		
		 } else if (file_ext(input$input_data$name) == 'txt'){
		
			asr_info = read.delim(to_pass, stringsAsFactors=FALSE, header=TRUE, fileEncoding = 'utf-8')
			
		}
        
        # detect language and output the results (in any case)
        detected_language = textcat(paste(asr_info[,'text'],collapse=' '))
        
        # if French, overwrite existing stopwords and filler words
        if (detected_language=='french') {
		
		if (operating_system == 'unix'){
			# custom_stopwords = read.csv('custom_stopwords_full_french.csv',header=FALSE,stringsAsFactors=FALSE, encoding='latin1')[,1]
		  custom_stopwords = read.table('custom_stopwords_full_french.txt',header=FALSE,stringsAsFactors=FALSE,fileEncoding='utf-8')[,1]
			#filler_words = read.csv('filler_words_french.csv',header=FALSE,stringsAsFactors=FALSE, encoding = 'latin1')[,1]
			filler_words = read.table('filler_words_french.txt',header=FALSE,stringsAsFactors=FALSE,fileEncoding='utf-8')[,1]
		} else if (operating_system == 'windows'){
		    custom_stopwords = read.csv('custom_stopwords_full_french.csv',header=FALSE,stringsAsFactors=FALSE)[,1]
			filler_words = read.csv('filler_words_french.csv',header=FALSE,stringsAsFactors=FALSE)[,1]
		}
          
        } else {
          
          if (detected_language!='english') {
            # if other than French we need to stop and raise a warning
            detected_language = 'Sorry, only the English and French languages are currently supported.'
          }
          
        }
        
        index_meeting = NA
        golden_summary = NA 
        meeting_name = NA
        
        method="CRP"
        scaling_factor=0.3
        lambda=5
        
      } else {
        
        detected_language='english'
        
        meeting_name = isolate(input$input_data)
        input_corpus = isolate(input$corpus)
        
        if (input_corpus=="AMI_corpus") {
          method="dens"
          scaling_factor=0.9
          lambda=2
        } else if (input_corpus=="ICSI_corpus") {
          method="CRP"
          scaling_factor=0.3
          lambda=5
        }
        
        asr_info = read.table(paste0(overall_wd,"/ASR/",paste0(meeting_name,".da-asr")), header=FALSE, sep="\t", quote="")
        
        # retain only columns of interest
        asr_info = asr_info[,c(2,3,5,9)]
        colnames(asr_info) = c("start","end","role","text")
        
        # write golden summaries to ROUGE directory
        index_meeting = which(unlist(lapply(golden_names, function(x) grepl(meeting_name,x)))==TRUE)	
        my_file = paste0(overall_wd,"/golden_summaries/",golden_names[index_meeting])
        
        file.copy(from=my_file,to=paste0("./rouge2.0-distribution/test-summarization/reference/",golden_names[index_meeting]), overwrite=TRUE)
        
        if (length(my_file)>1){
          
          golden_summary = lapply(my_file, function(x) readChar(x, file.info(x)$size))
          
        } else {
          
          golden_summary = readChar(my_file, file.info(my_file)$size)
          
        }
        
      }
      
    }) # end loading text
    
    if (detected_language %in% c('english','french')){
      
      withProgress({
        setProgress(message = "Cleaning text...")
        
        cleaned_transcript = cleaning_transcript(my_transcript_df = asr_info, time_prune = 0.85, custom = custom_stopwords, pos=FALSE, to_stem=TRUE, overlap_threshold = 1.5, detected_language=detected_language)
        
        cleaned_transcript_df = cleaned_transcript$collapse_output
        cleaned_transcript_df_processed = cleaned_transcript_df$reduced_my_df_proc
        cleaned_transcript_df_unprocessed = cleaned_transcript_df$reduced_my_df_unproc
        
        terms_list = list(processed = from_cleaned_transcript_to_terms_list(cleaned_transcript_df_processed[,4])$terms_list_partial, unprocessed = from_cleaned_transcript_to_terms_list(cleaned_transcript_df_unprocessed[,4])$terms_list_partial)
        
        utterances = as.character(cleaned_transcript_df_unprocessed[,4])
        start_time = cleaned_transcript_df_unprocessed[,1]
        
        # prune out utterances that are too short
        utterances_lengthes = unlist(lapply(utterances, function(x){length(setdiff(unlist(strsplit(tolower(x),split=" ")), custom_stopwords))}))
        index_remove = which(utterances_lengthes<=3)
        
        if (length(index_remove)>0){
          
          utterances = utterances[-index_remove]
          start_time = start_time[-index_remove]
          
        }
        
        # clean utterances
        utterances = clean_utterances(utterances, my_stopwords=custom_stopwords, filler_words=filler_words)$utterances
        
      })
      
    } else {
      
      index_meeting = NA
      golden_summary = NA 
      meeting_name = NA
      
      method=NA
      scaling_factor=NA
      lambda=NA
      
      terms_list=NA
      utterances=NA
      start_time=NA
      
    }
    
    list(method=method, scaling_factor=scaling_factor, lambda=lambda, terms_list=terms_list, utterances=utterances, start_time=start_time, index_meeting=index_meeting, golden_summary=golden_summary, meeting_name=meeting_name, detected_language=detected_language, operating_system = operating_system)

  })
  
  output$detected_language <- renderText({ 
    paste0('-> language detected: ', initial()$detected_language)
  })
  
  third = reactive({
    
    method = initial()$method
    terms_list = initial()$terms_list
    
    
    withProgress({
      setProgress(message = "Extracting keywords...")
      
      keywords_scores = from_terms_to_keywords(terms_list=terms_list, window_size=12, to_overspan=T, to_build_on_processed=T, community_algo="none", weighted_comm=NA, directed_comm=NA, rw_length=NULL, size_threshold=NULL, degeneracy="weighted_k_core", directed_mode="all", method=method, use_elbow=FALSE, use_percentage=NA, percentage=0.15, number_to_retain=NA, which_nodes="all", overall_wd)$output
      
    })
    
    df_wc = data.frame(words = keywords_scores$extracted_keywords, freq = round(as.numeric(keywords_scores$scores),4))
    
    min_5_row_number = min(50, nrow(df_wc))
    
    df_wc_bis = df_wc[1:min_5_row_number,]
    
    if (max(df_wc_bis[,2], na.rm=T)!= min(df_wc_bis[,2], na.rm=T)){
      
      # normalize values so that they are in the range [0.1, 1]
      df_wc_bis[,2] = 0.1 + (df_wc_bis[,2] - min(df_wc_bis[,2]))*(1-0.1) /(max(df_wc_bis[,2]) - min(df_wc_bis[,2]))
      
    } else {
      
      df_wc_bis[,2] = rep(1, nrow(df_wc_bis))
      
    }
    
    # m1 is here
    df_wc_bis %>%  mjs_plot(x=freq, y=words) %>% mjs_bar() %>% mjs_axis_x() -> m1
    
    list(keywords_scores=keywords_scores, df_wc=df_wc, df_wc_bis=df_wc_bis, m1=m1)
    
  })
  
  
  fourth = reactive({
    
	operating_system = initial()$operating_system
	
    # should only react when a new meeting is summarized (goButton), or when input$size changes
    
    input$goButton
    
    utterances = isolate(initial()$utterances)
    start_time = isolate(initial()$start_time)
    scaling_factor = isolate(initial()$scaling_factor)
    lambda = isolate(initial()$lambda)
    meeting_name = isolate(initial()$meeting_name)
    index_meeting = isolate(initial()$index_meeting)
    golden_summary = isolate(initial()$golden_summary)
    keywords_scores = isolate(third()$keywords_scores)
    boolean = isolate(prelude()$boolean)
    
    withProgress({
      setProgress(message = "Generating summary...")
      
      my_summary = from_keyword_to_summary_submodularity(graph_keywords_scores_temp = keywords_scores, utterances = utterances, start_time = start_time, to_stem=T, max_summary_length=input$size, scaling_factor=scaling_factor, weighted_sum_concepts=T, negative_terms=FALSE, lambda=lambda)$my_summary
      
      
    })
    
    
    if (boolean==FALSE){
      
      # write golden summary(ies) to ROUGE directory (again, to remediate the unlink when modifying summary size)
      index_meeting = which(unlist(lapply(golden_names, function(x) grepl(meeting_name,x)))==TRUE)	
      my_file = paste0(overall_wd,"/golden_summaries/",golden_names[index_meeting])
      
      file.copy(from=my_file,to=paste0("./rouge2.0-distribution/test-summarization/reference/",golden_names[index_meeting]), overwrite=TRUE)
      
      if (length(my_file)>1){
        
        golden_summary = lapply(my_file, function(x) readChar(x, file.info(x)$size))
        
      } else {
        
        golden_summary = readChar(my_file, file.info(my_file)$size)
        
      }
      
      # write system summary to ROUGE directory
      writeLines(my_summary, paste0(overall_wd,"/rouge2.0-distribution/test-summarization/system/",meeting_name,"_system.txt"))
      
      
      withProgress({
        setProgress(message = "Computing ROUGE scores...")
        
        setwd("./rouge2.0-distribution/")
        
        command = 'java -jar "rouge2.0.jar"'
        command_unix = 'java -jar rouge2.0.jar'
                
        if (operating_system=="unix"){
          
          # calls to a Linux environment - shinyapps
          system(paste0(command_unix))
          
        } else if (operating_system=="windows"){
          
          system(paste("cmd.exe /c", command), intern = FALSE, wait = TRUE)
          
        }
        
        # read back results
        rouge_scores = read.csv("results.csv", header=TRUE)[,c("Avg_Recall","Avg_Precision","Avg_F.Score")]
        
        # set back to old wd
        setwd(overall_wd)
        
      })
      
      unlink(paste0(overall_wd,"/rouge2.0-distribution/test-summarization/reference/",golden_names[index_meeting]))
      unlink(paste0(overall_wd,"/rouge2.0-distribution/test-summarization/system/",meeting_name,"_system.txt"))
      
      withProgress({
        setProgress(message = "Displaying results...")
        
        if (class(golden_summary)=="list"){
          
          outputs = lapply(golden_summary, function(x) overlap_html(my_summary, x, custom_stopwords))
          
        } else {
          
          outputs = list()
          outputs[[1]] = overlap_html(my_summary, golden_summary, custom_stopwords)
          
        }
        
      })
      
    } else {
      
      rouge_scores = c(0,0,0)
      outputs = list()
      outputs[[1]] = my_summary
      
      
    }
    
    list(rouge_scores=rouge_scores, outputs=outputs)
    
  })
  
  
  
  
  
  
  output$keywords_scores = downloadHandler(
    filename = function() {
      paste("keywords_scores.txt")
    },
    
    content = function(file) {
      write.table(third()$df_wc, file, col.names = FALSE, row.names = FALSE, quote=FALSE)
    }
  )
  
  output$wordcloud = renderWordcloud2({
    
    wordcloud2(third()$df_wc_bis, size = 0.5)
    
  })
  
  
  output$keywords_barplot <- renderMetricsgraphics(third()$m1)
  
  output$rouge = renderText({
    
    paste(c("Recall:","Precision:","F1:"), round(fourth()$rouge_scores,3), collapse=", ")
    
  })
  
  
  output$"summary_custom"=renderUI({
    HTML(fourth()$outputs[[1]])
  })
  
  output$"summary_1"=renderUI({
    HTML(fourth()$outputs[[1]]$my_html)
  })
  
  output$"summary_2"=renderUI({
    HTML(fourth()$outputs[[2]]$my_html)
  })
  
  output$"summary_3"=renderUI({
    HTML(fourth()$outputs[[3]]$my_html)
  })
  
  output$"golden_summary_1"=renderUI({
    HTML(fourth()$outputs[[1]]$golden_html)
  })
  
  output$"golden_summary_2"=renderUI({
    HTML(fourth()$outputs[[2]]$golden_html)
  })
  
  output$"golden_summary_3"=renderUI({
    HTML(fourth()$outputs[[3]]$golden_html)
  })
  
  output$"summary"=renderUI({
    HTML(fourth()$outputs[[1]]$my_html)
  })
  
  output$"golden_summary"=renderUI({
    HTML(fourth()$outputs[[1]]$golden_html)
  })
  
})