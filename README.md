# Task Manager Application

This project is a full-stack Task Manager application built with:
- Flutter (Web Frontend)
- Spring Boot (Backend)
- Nginx (Web Host and Reverse Proxy)
- Docker for running both services together

## Features
- User authentication (Register / Login)
- Add, edit, and delete tasks
- Persistent dark / light mode toggle
- API secured with JWT

## Prerequisites
Before running the application, ensure that the following are installed on your system:
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)  
  (Ensure that WSL2 backend is enabled on Windows.)
No additional setup for Flutter or Spring Boot is required.


## How to Run
1. Clone this repository to your system
2. Build and start all containers: docker-compose up --build
3. Accessing the Application
	Once the containers are running successfully:
	Frontend (Web Application): http://localhost
	Backend API (Spring Boot): http://localhost:8080
4. Stopping the Application: docker-compose down
