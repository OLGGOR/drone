## Drones

---

### Local running

>#### _build_
> - if you run the project via IDE you should before running app run maven task `compile` to generate _OpenApi_ and _MapStruct_ objects 
> - if you want to run the app as `jar` file, you should run maven task `package` to build `.jar` file

>#### _run_
> Running opportunities:
> - run using IDE. In this case just run it
> - run `.jar` file. Run in terminal next command: `java -jar drones.jar`

>#### _test_
> For testing application you can send REST requests:
> - using Postman. In this case you can export postman file which is located by path `local/Musala.postman_collection.json`, 
> - also you can use _OpenApi ui_ for sending requests, it will be available by http://localhost:5555/api after running the app 
> - or you can use any other application for sending requests.  
>   Description of all available endpoints you also will find by http://localhost:5555/api.
