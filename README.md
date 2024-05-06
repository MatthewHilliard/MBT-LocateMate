<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/MatthewHilliard/MBT-LocateMate">
    <img src="app/src/main/res/drawable/explore_logo.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">Grub Gallery</h3>

  <p align="center">
    Personalized dishes and recipes with the click of a button!
    <br />
    <a href="https://www.youtube.com/watch?v=HiiPeKkUXfM" target= "_blank">View Demo</a>
    ·
    <a href="https://github.com/MatthewHilliard/Grub-Gallery/issues">Report Bug</a>
    ·
    <a href="https://github.com/MatthewHilliard/Grub-Gallery/issues">Request Feature</a>
  </p>
</div>

LocateMate is a social media application centered around a location-based guessing game, enabling users to share adventures through photo uploads and location guessing. 

## Description

When creating a post, the user is able to take a photo, get their location pinned, and add a song choice. Once posted, friends are free to comment and guess on their post. Friends' guesses on each others posts are displayed in a Post Leaderboard, where the closest guess is ranked number 1. Once a user guesses on any post, their average guess is calculated and displayed in a Friend Leaderboard, where all of a user's friends (including them) are ranked by the average of every guess they have made. Users are able to view their post history and edit their captions.

<!-- GETTING STARTED -->
## Getting Started

This is an example of how you can set up the project locally.
To get a local copy up and running follow these simple steps.

### Dependencies

* Android Studio
* Firebase Project
* Google API Key
* Jamendo API Key

### Installing

1. Clone the repo
   SSH - 
   ```sh
   git clone git@github.com:MatthewHilliard/MBT-LocateMate.git
   ```
   HTTPS - 
   ```sh
   git clone https://github.com/MatthewHilliard/MBT-LocateMate.git
   ```
   
3. Follow steps 1-3 of option 1 from [Firebase](https://firebase.google.com/docs/android/setup) to setup the Firebase project, ensuring to add your local SHA key within Android Studio to Firebase. Also ensure to replace the google-services.json within the app folder of the local project.
   
4. Follow the Creating API Keys step for [Google Cloud Console](https://developers.google.com/maps/documentation/android-sdk/get-api-key) and copy paste your Google API Key into the AndroidManifest.xml here:
   ```sh
   <meta-data
   android:name="com.google.android.geo.API_KEY"
   android:value="PUT YOUR API KEY HERE"/>
   ```
   
5. Follow steps to obtain a Jamendo API key [here](https://developer.jamendo.com/v3.0) and copy paste your Jamendo API Key into the Constants file within the Jamendo API folder:
   ```sh
   class Constants {
    companion object{
        const val JAMENDO_KEY = "YOUR API KEY HERE"
        const val BASE_URL = "https://api.jamendo.com/"
       }
   }
   ```

### Executing program

Run the app within an Android Studio emulator or with a device connected via USB

## Authors

Brinja Vogler (bvogler@bu.edu)

Matthew Hilliard (mch2003@bu.edu)

Ting Liu (tinglliu@gmail.com)
