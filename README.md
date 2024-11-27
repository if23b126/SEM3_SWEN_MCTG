# Monster Card Trading Game (MCTG)

## Class Diagram
![Class Diagram](Klassendiagramm.png)

## GitHub Repository
[Here](https://github.com/if23b126/SEM3_SWEN_MCTG) is the link to my GitHub Repository.

## Deployment:
### 1. Docker-Compose
The following content needs to be copied into the docker-compsoe.yaml: 

    services:
        postgres:
            image: postgres
            ports:
                - 5432:5432
            volumes:
                - ./data:/var/lib/postgresql/data
            environment:
                - POSTGRES_PASSWORD=mctg
                - POSTGRES_USER=mctg

after that you can type the following into the terminal:

    docker compose up -d

after the docker is up and running, connect to it to execute following script (User: mctg, Password. mctg):

    CREATE TABLE public.users (
    username varchar(200) NULL,
    password varchar(200) null
    );
    
    CREATE TABLE  public.currently_logged_in (
    username varchar(200) NULL,
    token varchar(500) null
    );
    
    GRANT ALL ON public.users TO mctg;
    GRANT ALL ON public.currently_logged_in TO mctg;
    
    ALTER TABLE public.users ADD CONSTRAINT PK_users_username UNIQUE (username);
    ALTER TABLE public.currently_logged_in ADD CONSTRAINT PK_currently_logged_in_username UNIQUE (username);


### 2. Start Webserver

When the Docker Container is up and running, you can start the Webserver. To do so, build the Project with maven and execute the .jar file. If you're using IntelliJ, got to src/main/java/Main.java and click the Play-Button on the left Side next to the class name. 


### 3. Using the Webserver

To test the functionality, open an API testing Software of your choice and send Requests. 



## Technical Specifications

### Port
The Service will be available on port **10001**

### Methods
#### POST
- /users: register a new user
  - needs [body 1](#body-1)
- /sessions: login an existing user
  - needs [body 1](#body-1)
- /users/logout: logout a logged in user
  - needs [body 2](#body-2)

#### GET


### Bodies
#### body 1

    {
        "username": "<username>"
        "password": "<password>"
    }

#### body 2

    {
        "token": "<random Token>"
    }