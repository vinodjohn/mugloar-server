# Dragons of Mugloar Server

**Dragons of Mugloar Server** is a Spring Boot-based backend application that manages the game flow for the "Dragons of
Mugloar" game. It integrates with the Mugloar API, handles game state, inventory management, and provides real-time
updates via WebSockets.

**View this app hosted in AWS here:**  http://3.70.217.198:8080/
---

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Accessing H2 Console](#accessing-h2-console)
- [Using Actuator Endpoints](#using-actuator-endpoints)
- [Testing](#testing)
- [License](#license)
- [Contact](#contact)

---

## Features

- **Game Orchestration:** Initialize and manage game loops, including investigations and message solving.
- **Inventory Management:** Handle in-game items and apply their effects.
- **Shop Integration:** Purchase items based on strategic decisions.
- **Real-time Updates:** Provide game state updates via WebSockets.
- **Monitoring & Metrics:** Utilize Spring Actuator for application health and metrics.

---

## Technologies

- **Java 21**
- **Spring Boot 3.4**
- **H2 Database**
- **Spring Data JPA**
- **Spring WebSockets**
- **Spring Actuator**
- **Lombok**
- **SLF4J & Logback**
- **Maven**

---

## Prerequisites

- **Java JDK 21** or higher
- **Maven 3.4** or higher
- **Git**

---

## Installation

1. **Clone the Repository**

   ```bash
   git clone https://github.com/vinodjohn/mugloar-server
   cd mugloar-server

2. **Install dependencies:**
    ```sh
    mvn clean install
    ```

### Running the Application

1. **Using Docker Compose:**

   Ensure Docker is installed and running.

    ```sh
    docker-compose up --build
    ```

2. **Running Local

   Ensure you have a running instance of your database configured in `application.properties`.

    ```sh
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="--enable-preview"
    ```

_**Note:** This app doesn't have any preview features of Java._

## Authors

- **Vinod John** - [GitHub](https://github.com/vinodjohn)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.