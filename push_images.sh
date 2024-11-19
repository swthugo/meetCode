#!/bin/bash

# Function to display usage message
usage() {
    echo "Usage: $0 -e <env-file>"
    echo "  -e <env-file>    Specify the environment file (mandatory)"
    exit 1
}

# Parse options
while getopts "e:" option; do
    case $option in
        e) ENV_FILE=$OPTARG ;;
        *) usage ;;
    esac
done

# Check if the env file was provided
if [ -z "$ENV_FILE" ]; then
    echo "Error: Environment file is required."
    usage
fi

# Source the environment file
if [ -f "$ENV_FILE" ]; then
    echo "Loading environment variables from $ENV_FILE..."
    source "$ENV_FILE"
else
    echo "Error: Environment file '$ENV_FILE' not found."
    exit 1
fi

# Validate essential environment variables
REQUIRED_VARS=(
    "AWS_ACCOUNT_ID"
    "AWS_REGION"
    "FRONTEND_IMAGE_NAME"
    "BACKEND_IMAGE_NAME"
)
for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: $var is not set in the environment file."
        exit 1
    fi
done

# Variables (Replace with your values if not set in ENV_FILE)
AWS_ECR_BASE_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Log in to AWS ECR
echo "Logging in to AWS ECR..."
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ECR_BASE_URL

# Push images to ECR
echo "Pushing frontend image ${FRONTEND_IMAGE_NAME} to ECR..."
docker push $FRONTEND_IMAGE_NAME

echo "Pushing backend image ${BACKEND_IMAGE_NAME} to ECR..."
docker push $BACKEND_IMAGE_NAME

echo "Docker images pushed to AWS ECR successfully."
