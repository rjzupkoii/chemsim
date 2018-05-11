#!/usr/local/bin/rscript

library(matrixStats)

options(scipen=5)

EXPERIMENTAL <- '../data/experimental.csv'

OUTPUT_DIR <- '../results'
OUTPUT_PATH <- '../results/%s.png'

getExperimental <- function(label) {
	# Load the data
	df <- read.csv(EXPERIMENTAL, skip = 1, header = T)
	
	# Return if the data is not present
	if (!(label %in% colnames(df))) {
		return(NULL)
	}
	
	# Extract only the relevent data
	data <- cbind(df['Min'], df[label])
	data <- na.omit(data)
	return(data)
}

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

process <- function(file, unit) {
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
		analysis(data[[compound]], compound, unit)
	}
}

analysis <- function(data, label, unit) {
	# If we are working with mols, covert to mM and load the experimetnal data
	experimental <- NULL;
	if (unit == 'Mols') {
		data <- (data * 1000) / 1.8
		unit <- 'mM'
		experimental <- getExperimental(label)
	}
	
	# Find the stats
	min <- rowMins(data)
	mean <- rowMeans(data)
	max <- rowMaxs(data)	
	
	# Plot the data
	file = sprintf(OUTPUT_PATH, label)
	png(file = file, width = 1024, height = 768)
	plot(mean, type = 'l', xlab = 'Timestep, min', ylab = sprintf('%s, %s', label, unit))
	lines(min, type='l', col='blue')
	lines(max, type='l', col='red')
		
	# Plot experimetnal data if present
	if (!is.null(experimental)) {
		x <- as.list(experimental[,'Min'])
		y <- as.list(experimental[, label])
		points(x, y, pch=16, col="red")
		legend("right", legend = c("Mean", "Min", "Max", "Experimental"), 
				col = c("black", "blue", "red", "red"), lty=c(1, 1, 1, NA), cex=0.8, pch = c(NA, NA, NA, 16))
	} else {
		legend("right", legend = c("Mean", "Min", "Max"), col = c("black", "blue", "red"), lty=1, cex=0.8)
	}
	dev.off()	
}

dir.create(OUTPUT_DIR, showWarnings = FALSE)
#process('../data/simple/molecules', 'Molecules')
process('../data/mols', 'Mols')
