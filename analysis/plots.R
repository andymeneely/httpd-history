# Clear
rm(list = ls())
cat("\014")

# Check for Database Connection Settings
if(!file.exists("db.settings.R")){
  stop(sprintf("db.settings.R file not found."))
}

# Include Libraries
source("db.settings.R")
source("includes.R")

# Initialize Libraries
init.libraries()

# ggplot Theme
plot.theme <-
  theme_bw() +
  theme(
    plot.title = element_text(
      size = 14, face = "bold", margin = margin(5,0,25,0)
    ),
    axis.text.x = element_text(size = 10, angle = 50, vjust = 1, hjust = 1),
    axis.title.x = element_text(face = "bold", margin = margin(15,0,5,0)),
    axis.text.y = element_text(size = 10),
    axis.title.y = element_text(face = "bold", margin = margin(0,15,0,5)),
    strip.text.x = element_text(size = 10, face = "bold"),
    legend.position = "bottom",
    legend.title = element_text(size = 9, face = "bold"),
    legend.text = element_text(size = 9)
  )

###############################################################################
## Lift Curves
###############################################################################

#### Query Data
query <- "
  SELECT release,
    sloc,
    becomes_vulnerable,
    num_bugs,
    num_enhancements,
    num_regression_bugs,
    num_bugs,
    ((num_bugs * 1.0)/sloc)
  FROM file
  WHERE (path LIKE '%.c' OR path LIKE '%h') AND sloc > 0
  ORDER BY release ASC, ((num_bugs * 1.0)/sloc) DESC
"
db.connection <- get.db.connection(db.settings)
dataset <- db.get.data(db.connection, query)
db.disconnect(db.connection)

dataset$becomes_vulnerable <- as.logical(dataset$becomes_vulnerable)

### Prepare Plotting Data Set
plot.dataset <- filter.dataset(dataset, filter.type = "bug")
plot.source <- data.frame()
for(release in unique(plot.dataset$release)){
  cat("Release", release, "\n")
  release.dataset <- plot.dataset[plot.dataset$release == release,]

  file.count <- nrow(release.dataset)
  vuln.count <- nrow(
    release.dataset[release.dataset$becomes_vulnerable == TRUE,]
  )

  file.percent <- numeric(length = nrow(release.dataset))
  vuln.percent <- numeric(length = nrow(release.dataset))

  vuln.found <- 0
  for(index in 1:nrow(release.dataset)){
    if(release.dataset[index,]$becomes_vulnerable == TRUE){
      vuln.found <- vuln.found + 1
    }
    vuln.percent[index] <- vuln.found / vuln.count
    file.percent[index] <- index / file.count
  }

  plot.source <- rbind(
    plot.source,
    data.frame(
      "release" = release,
      "label" = paste("Release", release),
      "vuln.percent" = vuln.percent,
      "file.percent" = file.percent
    )
  )
}

# Export Resolution: 550 x 400
ggplot(plot.source, aes(x = file.percent, y = vuln.percent)) +
  geom_line(size = 1) +
  scale_x_continuous(labels = scales::percent, breaks = seq(0, 1.0, by = 0.1)) +
  scale_y_continuous(labels = scales::percent) +
  facet_wrap(~ label, nrow = 1, scales = "fixed") +
  labs(title = "Lift Curves", x = "% Files", y = "% Vulnerable Files Found") +
  plot.theme

###############################################################################
## Density and Correlation Plots
###############################################################################

#### Query Data
query <- "
  SELECT release,
    sloc,
    becomes_vulnerable,
    num_bugs,
    num_enhancements,
    num_regression_bugs
  FROM file
  WHERE path LIKE '%.c' OR path LIKE '%h'
  ORDER BY release ASC, num_bugs DESC
"

db.connection <- get.db.connection(db.settings)
dataset <- dbGetQuery(db.connection, query)
dbDisconnect(db.connection)

dataset$becomes_vulnerable <- as.logical(dataset$becomes_vulnerable)

##########################################
### Bug Metrics
##########################################

plot.dataset <- filter.dataset(dataset, filter.type = "bug")

#####################
### Density Plots
#####################

### Base
#### Export Resolution: 400 x 380
ggplot(plot.dataset, aes(x = becomes_vulnerable, y = sloc)) +
  geom_violin(aes(fill = becomes_vulnerable), alpha = 0.3) +
  geom_boxplot(width = 0.07, outlier.size = 1) +
  scale_x_discrete(breaks = c("TRUE", "FALSE"), labels = c("Yes", "No")) +
  scale_y_log10() +
  scale_fill_manual(
    values = c("TRUE" = "#636363", "FALSE" = "#f0f0f0"),
    labels = c("TRUE" = "Yes", "FALSE" = "No"),
    name = "Vulnerable"
  ) +
  labs(
    title = "Distribution of SLOC",
    x = "Vulnerable", y = "Metric Value (Log Scale)"
  ) +
  plot.theme +
  theme(legend.position = "none")

### Reference
#### Export Resolution: 400 x 380
ggplot(plot.dataset, aes(x = becomes_vulnerable, y = num_bugs)) +
  geom_violin(aes(fill = becomes_vulnerable), alpha = 0.3) +
  geom_boxplot(width = 0.07, outlier.size = 1) +
  scale_x_discrete(breaks = c("TRUE", "FALSE"), labels = c("Yes", "No")) +
  scale_y_log10() +
  scale_fill_manual(
    values = c("TRUE" = "#636363", "FALSE" = "#f0f0f0"),
    labels = c("TRUE" = "Yes", "FALSE" = "No"),
    name = "Vulnerable"
  ) +
  labs(
    title = "Distribution of num-pre-bugs",
    x = "Vulnerable", y = "Metric Value (Log Scale)"
  ) +
  plot.theme +
  theme(legend.position = "none")

### Categories
#### Prepare Plotting Data Set
COLUMN.LABELS <- list(
  "num_enhancements" = "num-enhancements",
  "num_regression_bugs" = "num-regression-bugs"
)
plot.source <- data.frame()
for(index in 1:length(COLUMN.LABELS)){
  cat(COLUMN.LABELS[[index]], "\n")
  plot.source <- rbind(
    plot.source,
    data.frame(
      "label" = COLUMN.LABELS[[index]],
      "value" = plot.dataset[[names(COLUMN.LABELS)[index]]],
      "release" = factor(
        plot.dataset$release, levels = unique(plot.dataset$release)
      ),
      "becomes_vulnerable" = plot.dataset$becomes_vulnerable
    )
  )
}

#### Export Resolution: 420 x 380
ggplot(plot.source, aes(x = becomes_vulnerable, y = value)) +
  geom_violin(aes(fill = becomes_vulnerable), alpha = 0.3) +
  geom_boxplot(width = 0.07, outlier.size = 1) +
  scale_x_discrete(breaks = c("TRUE", "FALSE"), labels = c("Yes", "No")) +
  scale_y_log10() +
  scale_fill_manual(
    values = c("TRUE" = "#636363", "FALSE" = "#f0f0f0"),
    labels = c("TRUE" = "Yes", "FALSE" = "No"),
    name = "Vulnerable"
  ) +
  facet_wrap(~ label, nrow = 1, scales = "free_x") +
  labs(
    title = "Distribution of Pre-release Bug Category Metrics",
    x = "Vulnerable", y = "Metric Value (Log Scale)"
  ) +
  plot.theme +
  theme(legend.position = "none")
