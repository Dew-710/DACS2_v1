# Use an official Node.js image as the base image
FROM public.ecr.aws/bitnami/node:20 as base

# Install pnpm
RUN npm install -g pnpm

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and pnpm-lock.yaml to install dependencies
COPY package.json pnpm-lock.yaml ./

# Install dependencies using pnpm
RUN pnpm install --frozen-lockfile

# Copy the rest of the application code
COPY . .

# Build the Next.js application
# This command should be adjusted based on your actual build script
RUN pnpm build

# Expose the port your Next.js app runs on (default is 3000)
EXPOSE 3000

# Command to run the application
# This command should be adjusted based on your actual start script
CMD ["pnpm", "start"]
