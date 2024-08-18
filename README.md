# UsainWolt

## Introduction
**UsainWolt** is a Java-based clone of the Wolt food delivery application. The system allows customers to browse nearby restaurants, view menus, place orders, and track their order history. For restaurants, it provides functionality to update menus, manage orders, and track revenue. The project is built using object-oriented design principles and follows a client-server architecture, with separate packages for client and server functionalities.

## How to run the project
1. Clone the repository
2. Open the project in an IDE (IntelliJ IDEA is recommended)
3. Rename the .envtemp file (in "src/Server") to .env and fill in the API keys as provided (I added the key in the notes section of the submission box along with the link to this repository)
4. Run ServerMain.java to start the server
5. Run UsainWoltMain.java to start the client
6. Use the client GUI to interact with the system

List of users can be found in the users.csv file. The username is the first column, the password is the second column, and the role is the third column.

## Troubleshooting
**We use the following dependencies in the project:**
- Gson

**We use the following ports in the project:**
- 8080 for the image server
- 12345 for the server

## ToDos
### Server
- [ ] Add the all the admin requests

### Client
- [ ] Make the admin GUI - David on it

### General
***This can be done by anyone***
- [ ] Add more restaurants and dishes to the system, with better descriptions, prices, and images
- [ ] Add more test cases to the system
- [ ] Add more comments to the code