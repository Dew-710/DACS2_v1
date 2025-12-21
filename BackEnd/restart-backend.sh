#!/bin/bash

# 🔄 Script để restart backend sau khi rebuild

echo "🛑 Stopping old backend process..."
pkill -f "BackEnd-0.0.1-SNAPSHOT.jar" || echo "No running backend found"

sleep 2

echo "🚀 Starting backend with new code..."
cd /Users/macintosh/DACS2/Backend

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
fi

# Start backend
nohup java -jar target/BackEnd-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=postgres \
    > backend.log 2>&1 &

echo "✅ Backend started! PID: $!"
echo "📋 Logs: tail -f backend.log"
