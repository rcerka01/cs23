#!/bin/bash

# Set default values for environment variables
HTTP_PORT=${HTTP_PORT:-9004}
CSCARDS_ENDPOINT=${CSCARDS_ENDPOINT:-/app.clearscore.com/api/global/backend-tech-test/v1/cards}
SCOREDCARDS_ENDPOINT=${SCOREDCARDS_ENDPOINT:-/app.clearscore.com/api/global/backend-tech-test/v2/creditcards}

# Export environment variables for the SBT process
export HTTP_PORT
export CSCARDS_ENDPOINT
export SCOREDCARDS_ENDPOINT

# Start the SBT service
sbt run
