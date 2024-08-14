# UsainWolt

## Introduction
**UsainWolt** is a Java-based clone of the Wolt food delivery application. The system allows customers to browse nearby restaurants, view menus, place orders, and track their order history. For restaurants, it provides functionality to update menus, manage orders, and track revenue. The project is built using object-oriented design principles and follows a client-server architecture, with separate packages for client and server functionalities.

## How to run the project
1. Clone the repository
2. Open the project in an IDE (IntelliJ IDEA is recommended)
3. Run the `ServerMain` class to start the server
4. Run the `UsainWoltMain` class to start the client
5. And you are good to go!

## Troubleshooting
** We use the following dependencies in the project:**
- Gson

** We use the following ports in the project:**
- 8080 for the image server
- 12345 for the server

## ToDos
### Server
- [ ] Check edge cases of every method that requires input from the user
- [ ] Porhibit use of [";" , "," , "\n"] in the input fields (all of them are used as delimiters)
- [ ] return parsed data from the server to the client ("#" -> "\n") and ("@" -> ",") in some cases

### Client
- [ ] Make the admin GUI

### General
- [ ] Add more restaurants and dishes to the system, with better descriptions, prices, and images
- [ ] Add more test cases to the system
- [ ] Add more comments to the code