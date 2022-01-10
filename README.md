# FamilyBudgetControll application
This application is designed to control the family budget by setting limits for both - the **USER** and the **FAMILY**, by the _FAMILY ADMINISTRATOR_ and the _GLOBAL ADMINISTRATOR_. 
Access to the methods of managing the _limits_ is distributed among the administrator _roles_ - the **global administrator** has the _highest_ access.

## RUN

To start the application - just **run method _main()_ in _FamilyBudgetControllApplication.class_**
Then you need run SQL script in H2 Console `(INSERT INTO USERS_ROLES (user_id, role_id) VALUES(10, 3);`
This script will set user with `id = 10` to **_GLOBAL ADMINISTRATOR_**

### Test

There is a file, named _FamilyBudgetController.postman_collection.json_ , where fully configurated requests to test functionality of application.
[FBCA json](https://www.mediafire.com/file/w96b75nm1k73xvt/FamilyBudgetController.postman_collection.json/file)

> Enjoy!



> This is just a _test task_, it's not pretend to be a comercial product etc.
