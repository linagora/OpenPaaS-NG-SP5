keyword_extraction <-
  function(g, community_algo, method, use_elbow, use_percentage, percentage, number_to_retain, which_nodes){
    
    # for CR, one should always have specified a "percentage" value
    
    # "method" can be any of "CRP", "CRE", "dens" 
    
    # if use_percentage is TRUE: percentage is used (real between 0 and 1)
    # otherwise, number_to_retain (integer) - currently not using
    
    # which_nodes is used in coreRank only (for selecting the neighbors), and can be "all", "in", or "out"
    
    if ((method=="CRP")|(method=="CRE")){
      
      method_inner = "CR"
      
    } else {
      
      method_inner = method
      
    }
    
    if (community_algo=="none"){
      
      output = keyword_extraction_inner(g, method=method_inner, use_elbow, use_percentage, percentage, number_to_retain, which_nodes)
      
      if (method_inner=="CR"){
        
        if (method=="CRE"){
          
          extracted_keywords = output$extracted_keywords[[2]]
          scores = output$scores[[2]]
          
        } else if (method=="CRP"){
          
          extracted_keywords = output$extracted_keywords[[1]]
          scores = output$scores[[1]]
          
        }
        
      } else {
        
        extracted_keywords = output$extracted_keywords
        scores = output$scores
        
      }
      
    }
    
    output = list(extracted_keywords = extracted_keywords, scores = scores)
    
  }