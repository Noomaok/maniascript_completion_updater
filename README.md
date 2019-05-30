# maniascript-completion-updater
This is an app to update completion file for : https://github.com/MattMcFarland/vscode-maniascript  
It use this link to get all the datas : https://maniaplanet.github.io/maniascript-reference/annotated.html

# Requirement
You need to have Maven install in order to run the project

# Run project
## Using VSCode
Launch both compile&run task to test the project  
Launch jar task to create a Jar executable (shaded)

## Command line
To compile and run project  
```
mvn clean compile exec:java 
```

To create a Jar executable  
```
mvn clean package
```