# FlopBox-Agent 

**Auteurs :** <br>
Antoine Pochet, antoine.pochet.etu@univ-lille.fr <br>
Julien Michot, julien.michot.etu@univ-lille.fr <br>
**Date de début :** 21/03/22 <br>
[Vidéo du projet](doc/video.mp4)

## Presentation du projet : 

**Flopbox-Agent** est une application cliente pour la plate-forme FlopBox qui permette de synchroniser les données stockées à distance dans un ensemble de serveurs FTP avec le système de fichiers local d'une machine sur laquelle l'application cliente sera exécutée.  

## Utilisation :
### Pour générer la javadoc :
* Chemin de la javadoc une fois générée : `target/docs/*`
```
mvn javadoc:javadoc
```

### Pour générer le JAR :
```
mvn clean compile package
```

### Pour exécuter le JAR :
* Une fois le git submodule récupérer
#### Lancement du serveur FlopBox
```
.\launch_serveur.sh
```

#### Lancement du client FlopBox-Agent
* Lancement du programme principale permettant de synchroniser les données stockées à distance dans un ensemble de serveurs FTP avec le système de fichiers local d'une machine sur laquelle l'application cliente sera exécutée.
```
java -jar .\target\Flopbox-Agent-POCHET-1.0-SNAPSHOT.jar run {CONFIG} {PATH}
```
Avec : 
- `{CONFIG}` chemin absolu vers le fichier de configuration de Flopbox-Agent
- `{PATH}` chemin absolu vers le dossier d'où le client flopbox téléchargera les élements des serveurs FTP

---

* Lancement du programme principale permettant de lister le contenu du répertoire `./deleted` du serveur FTP correspondant à l'alias en paramètre
```
java -jar .\target\Flopbox-Agent-POCHET-1.0-SNAPSHOT.jar -l {CONFIG} {ALIAS}
```
Avec : 
- `{CONFIG}` chemin absolu vers le fichier de configuration de Flopbox-Agent
- `{ALIAS}` nom de l'alias du serveur FTP

---

* Lancement du programme principale permettant de supprimer l'entièreté du répertoire `./deleted` du serveur FTP correspondant à l'alias en paramètre
```
java -jar .\target\Flopbox-Agent-POCHET-1.0-SNAPSHOT.jar -d {CONFIG} {ALIAS}
```
Avec : 
- `{CONFIG}` chemin absolu vers le fichier de configuration de Flopbox-Agent
- `{ALIAS}` nom de l'alias du serveur FTP

---

* Lancement du programme principale permettant de récupérer un fichier contenu dans le répertoire `./deleted` du serveur FTP correspondant à l'alias en paramètre
```
java -jar .\target\Flopbox-Agent-POCHET-1.0-SNAPSHOT.jar -r {CONFIG} {ALIAS} {FILE} {PATH}
```
Avec : 
- `{CONFIG}` chemin absolu vers le fichier de configuration de Flopbox-Agent
- `{ALIAS}` nom de l'alias du serveur FTP
- `{FILE}` nom du fichier contenu dans le dossier `./deleted` du serveur FTP à récupérer
- `{PATH}` chemin du dossier de destination du fichier sur le serveur FTP 

## Architecture
*UML du projet :* <br> 
![UML](doc/diag-uml.png)

### Lister et expliquer la gestion d'erreur :

Dans ce projet, il y a plusieurs Classes qui extends Exception. <br>
* `RunException` : Cette classe permet de relever les exceptions lors du lancement du client
* `JSONException` : Cette classe permet de relever les exceptions JSON

## Code samples :

On peut voir ici la méthode pour récupérer le fichier "config.json" et transformer son contenu en String pour pouvoir recréer l'architecture de notre fichier avec des JSONObject plus tard.

```
private static String fileToString(String filePath) throws RunException {
  StringBuilder fileres = new StringBuilder();
  
  // On essaye de lire le fichier
  try (BufferedReader br = new BufferedReader(new FileReader("resources/user.json"))) {
    String line = "";
    //Tant que l'on lit quelque chose on ajoute la ligne au résultat
    do {
      line = br.readLine();
      fileres.append(line);
    } while (line != null);
  } catch (IOException e) {
    throw new RunException("Error lors de la lecture du fichier de configuration : " + e.getMessage());
  }
  return fileres.toString();
}
```

On peut voir ici une méthode récursive pour récupérer des fichiers sur le serveur FTP.
```
private void pull(String localPath, String serverPath, String alias) throws RunException {
   // Récupération des fichiers via l'API
   String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, serverPath.replace("\\", "/")).toString();
   if (ftpReceiveFiles.equals("[]")) System.out.println("Aucun fichiers");
   else {
      List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));

      for (ServerFile serverFile : serverFiles) {
         // Si c'est un dossier alors on créer le dossier en local et on refait un appel a la méthode pull récursivement
         if (serverFile.isDirectory()) {
            try {
               Files.createDirectory(Paths.get(localPath + "\\" + serverFile.getName()));
               if (serverPath.equals(""))
                  pull(localPath + "\\" + serverFile.getName(), serverFile.getName(), alias);
               else
                  pull(localPath + "\\" + serverFile.getName(), serverPath + "\\" + serverFile.getName(), alias);
            } catch (IOException e) {
               throw new RunException("Error lors de la récupération des données du serveur : " + e.getMessage());
            }
         // Sinon on récupére le fichier et on le télécharge en local
         } else {
            InputStream inputStream = api.retrieveFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, serverPath.replace("\\", "/") + "/" + serverFile.getName());
            try {
               writeLocalFile(inputStream, localPath + "\\" + serverFile.getName());

               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
               Date ftpDate = sdf.parse(serverFile.getDateAndTime());
               Files.setAttribute(Paths.get(localPath), "lastModifiedTime", FileTime.fromMillis(ftpDate.getTime()));
            } catch (IOException | ParseException e) {
               throw new RunException("Error lors de la récupération des données du serveur : " + e.getMessage());
            }
         }
      }
   }
}
```

On peut voir ici une méthode permettant de lister le contenu du dossier ./deleted d'un serveur FTP.
```
public void list() {
   for (String alias : ftpServerMap.keySet()) {
      String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted").toString();
      if (ftpReceiveFiles.equals("[]")) System.out.println("Aucun fichier dans le dossier ./deleted");
      else {
         List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));
         System.out.println("├── .deleted/");
         for (int i = 0; i < serverFiles.size(); i++) {
            if (i == serverFiles.size() - 1) System.out.println("│   └── " + serverFiles.get(i).getName());
            else System.out.println("│   ├── " + serverFiles.get(i).getName());
         }
      }
   }
}
```

On peut voir ici une méthode permettant de supprimer le contenu du dossier ./deleted d'un serveur FTP.
```
public void delete() {
   for (String alias : ftpServerMap.keySet()) {
      String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted").toString();
      if (ftpReceiveFiles.equals("[]")) System.out.println("Aucun fichier dans le dossier ./deleted");
      else {
         List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));
         for (ServerFile serverFile : serverFiles) {
            api.removeFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted/" + serverFile.getName());
          }
      }
   }
}
```

On peut voir ici une méthode permettant de récupérer un fichier du dossier ./deleted d'un serveur FTP.
```
public void retrieve(String fileName, String path) {
   for (String alias : ftpServerMap.keySet()) {
      String ftpReceiveFiles = api.listFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted").toString();
      if (ftpReceiveFiles.equals("[]")) System.out.println("Aucun fichier dans le dossier ./deleted");
      else {
         List<ServerFile> serverFiles = stringToServerFile(ftpReceiveFiles.substring(1, ftpReceiveFiles.length() - 1));
         for (ServerFile serverFile : serverFiles) {
            if (serverFile.getName().equals(fileName)) {
               if (path.equals(""))
                  api.renameFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted/" + fileName, "../" + fileName, null);
               else
                  api.renameFTPServer(ftpServerMap.get(alias).getUsername(), ftpServerMap.get(alias).getPassword(), PASSWORD, USERNAME, alias, ".deleted/" + fileName, "../" + path + "/" + fileName, null);
            }
         }
      }
   }
}
```
