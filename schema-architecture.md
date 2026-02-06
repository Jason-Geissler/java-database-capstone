This Spring Boot application follows a layered architecture and uses a combination of MVC and REST controllers to handle different parts of the system. 
Thymeleaf templates are used to render server-side views for the Admin and Doctor dashboards, while RESTful APIs support all other modules and client interactions. 
This approach allows the application to serve both web-based interfaces and API consumers efficiently.

All incoming requests pass through a centralized service layer, which contains the business logic and coordinates interactions with the data layer. 
The application integrates with two databases: MySQL, which stores structured relational data such as patients, doctors, appointments, and administrators using JPA entities, and MongoDB, which stores prescription data using document-based models. 
This separation ensures that each type of data is stored in the most appropriate format while maintaining clean separation of concerns across the application.


1. Users interact with the Admin or Doctor dashboards through the web interface or access patient and appointment features through REST-based modules.
2. These requests are routed to either Thymeleaf MVC controllers (for dashboards) or REST controllers (for API-based modules).
3. The controllers forward the requests to the central service layer, where business logic is handled.
4. The service layer uses the appropriate repositories to interact with the data layer—MySQL repositories for relational data and MongoDB repositories for prescription data.
5. MySQL repositories access the MySQL database to read or write structured data such as patients, doctors, appointments, and admins.
6. This data is mapped to and from JPA entity models used within the application.
7. MongoDB repositories access the MongoDB database, mapping prescription data to document models used by the application.

