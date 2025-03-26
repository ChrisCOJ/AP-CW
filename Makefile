# Makefile for Server and Client

JC = javac
JR = java
CLIENT_SRC_DIR = src/main/client
SERVER_SRC_DIR = src/main/server
CLIENT_CLASS = main.client.Client
SERVER_CLASS = main.server.ChatServer
OUT_DIR = out

.PHONY: all compile run-server run-client clean

compile: compile-server compile-client

compile-server:
	@echo "Compiling server..."
	$(JC) -d $(OUT_DIR) $(SERVER_SRC_DIR)/*.java
	@echo "Server compiled to $(SERVER_OUT_DIR)"

compile-client:
	@echo "Compiling client..."
	$(JC) -d $(OUT_DIR) $(CLIENT_SRC_DIR)/*.java
	@echo "Client compiled to $(CLIENT_OUT_DIR)"

run-server: compile-server
	@echo "Running server..."
	$(JR) -cp $(OUT_DIR) $(SERVER_CLASS)

run-client: compile-client
	@echo "Running client..."
	$(JR) -cp $(OUT_DIR) $(CLIENT_CLASS)

clean:
	@echo "Cleaning..."
	rm -rf $(OUT_DIR)
	@echo "Clean complete."