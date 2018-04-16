#!/usr/local/bin/rscript

library(matrixStats)

OUTPUT_DIR <- '../results'
OUTPUT_PATH <- '../results/%s.png'

load <- function(path) {
	data <- list()
	ndx <- 0
	for (file in list.files(path)) {
		# Prepare the path
		file = paste(path, file, sep='/')
		
		# Load the data
		headers = read.csv(file, skip = 1, header = F, nrows = 1, as.is = T)
		df = read.csv(file, skip = 2, header = F)
		colnames(df) = headers
		
		# Drop any empty (i.e., all NA) columns
		empty <- sapply(df, function(x) all(is.na(x)))
		df <- df[!empty]
		
		# Append to list
		ndx <- ndx + 1
		data[[ndx]] <- df
	}
	return(data)
}

process <- function(file) {
	raw <- load(file)
	data <- list()
	# Extract the data for each compound
	for (compound in colnames(raw[[1]])) {
		data[[compound]] <- matrix(, nrow = nrow(raw[[1]]), ncol = length(raw))
		for (ndx in 1:length(raw)) {
			col <- matrix(unlist(raw[[ndx]][compound]), ncol = 1, byrow = TRUE)
			data[[compound]][, ndx] <- col
		}	
	}
	
	# Plot the data 
	for (compound in colnames(raw[[1]])) {
		analysis(data[[compound]], compound)
	}
}

analysis <- function(data, label) {
	min <- rowMins(data)
	mean <- rowMeans(data)
	max <- rowMaxs(data)	
	
	file = sprintf(OUTPUT_PATH, label)
	png(file = file, width = 1024, height = 768)
	plot(mean, type = 'l', xlab = 'Timestep', ylab = sprintf('Molecules %s', label))
	lines(min, type='l', col='blue')
	lines(max, type='l', col='red')
	dev.off()	
}

dir.create(OUTPUT_DIR, showWarnings = FALSE)
process('../data/simple')
