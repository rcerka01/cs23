#!/bin/bash

# Set default values for environment variables
HTTP_PORT=${HTTP_PORT:-8080}
CSCARDS_ENDPOINT=${CSCARDS_ENDPOINT:-http://localhost:8000}
SCOREDCARDS_ENDPOINT=${SCOREDCARDS_ENDPOINT:-http://localhost:9000}

# Export environment variables for the SBT process
export HTTP_PORT
export CSCARDS_ENDPOINT
export SCOREDCARDS_ENDPOINT

# Start the SBT service
sbt run
