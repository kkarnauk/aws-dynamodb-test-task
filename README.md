# AWS DynamoDB Test Task

It's a test task for Fall 2021 Practice in JetBrains for the project 'Emulator for AWS DynamoDB'.

This project is just a simple CSV importer to HSQL-database.

### How to use?

Pass the following options as command line args: `/path/to/CSV.csv`, `name of table`, `schema of table`.
Then the program will ask you about URL, username and password to your database. 
The last three options are not passed along with the rest for security reasons. **Your database must exist**.

If the passed table doesn't exist, then it'll be created for you and filled with the values from the CSV file.
Otherwise, the values will be inserted to the table. 

Note: if your database does exist, then the columns names must match with names in the CSV file.
