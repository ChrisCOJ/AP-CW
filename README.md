**If running on Windows:**
1. Navigate to the source directory (i.e. AP-CW) and copy paste the following:
2. To run the server: mkdir out && javac -d out src\main\server\*.java && java -cp out main.server.ChatServer
3. To run the client: mkdir out && javac -d out src\main\client\*.java && java -cp out main.client.Client


**If running this code on Unix based systems:**
1. Download and install make, and navigate to the source directory.
2. To run the server: make run-server
3. to run the client: make run-client\
OR
4. Copy paste the following:
5. To run the server: mkdir -p out && javac -d out src/main/server/*.java && java -cp out main.server.ChatServer
6. To run the client: mkdir -p out && javac -d out src/main/client/*.java && java -cp out main.client.Client
