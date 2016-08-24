cores_dec = function(g, degeneracy, directed_mode, directed){

	# "g" is a graph with edge weights (optional)
	# "directed_mode" can be "all", "in", or "out"

	operating_system = .Platform$OS.type

	if (degeneracy == "weighted_k_core"){

		if (directed_mode == "all"){
			my_number = 0
		} else if (directed_mode == "in") {
			my_number = 1
		} else {
			my_number = 2
		}

		command = paste('java -jar "kcore-1.0.jar" edgelist.txt', my_number)
		command_unix = paste('java -jar kcore-1.0.jar edgelist.txt', my_number)

		if (operating_system=="unix"){

			# calls to a Linux environment - shinyapps
			system(paste0(command_unix))

		} else if (operating_system=="windows"){

			system(paste("cmd.exe /c", command), intern = FALSE, wait = TRUE)

		}

		file_read = read.csv("edgelist_cores.csv", header=FALSE, stringsAsFactors = FALSE, sep=" ")
		file_read = file_read[,-2]

		cores_g = as.numeric(file_read[,2])
		names(cores_g) = as.character(file_read[,1])

	}

	# sort vertices by decreasing core number
	cores_g = sort(cores_g, decreasing = TRUE)

	output = list(cores = cores_g)

}