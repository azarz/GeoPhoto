# GeoPhoto
Mini-projet Android

![app/src/main/res/mipmap-hdpi/ic_launcher.png](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png "Logo")

### Manuel d'utilisation :

Lancer l'APK GeoPhoto.apk situé dans la racine du projet

La checkbox "Envoyer courriel" correspond à l'envoi d'un mèl après la prise d'une photo (appui sur le bouton "PHOTO")

Si la position n'est pas activée **au lancement** de l'application, la position est aléatoire.
Si elle est activée, il faut attendre d'être localisé pour que la photo soit prise, ce qui peut être long en intérieur.

### BUG CONNUS

+ Sur mon Wiko Pulp 4G, les images enregistrées dans le dossier Pictures du stockage du téléphone ont un problème : il faut redémarrer l'appareil pour accéder aux données EXIF et pour la supprimer/modifier.
+ Si le signal GPS n'est pas reçu, l'application n'arrive pas encore à passer à la localisation par WIFI ou par GSM.
