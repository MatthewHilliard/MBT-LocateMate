<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/MatthewHilliard/MBT-LocateMate">
    <img src="app/src/main/res/drawable/explore_logo.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">LocateMate</h3>

  <p align="center">
    Share adventures, guess locations, and connect globally!
    <br />
  </p>
</div>

## About the Project
LocateMate is a social media application centered around a location-based guessing game, enabling users to share adventures through photo uploads and location guessing. When creating a post, the user is able to take a photo, get their location pinned, and add a song choice. Once posted, friends are free to comment and guess on their post. Friends' guesses on each others posts are displayed in a Post Leaderboard, where the closest guess is ranked number 1. Once a user guesses on any post, their average guess is calculated and displayed in a Friend Leaderboard, where all of a user's friends (including them) are ranked by the average of every guess they have made. Users are able to view their post history and edit their captions.

### App Architecture
Database: Firebase Firestore and Firebase Cloud Storage (image storage)
APIs: Google Maps API, Jamendo API
### Main Fragments
**Explore:**
- Fetches friend/public posts from the Firestore and displays them in a recycler view
- Allows user to view friend requests, add/remove friends, search for users
  
**Create Post:**
- Gets current location of user and displays a marker in a SupportMapFragment
- Allows user to take a photo using the device camera, choose visibility of their post, and add a caption and song
  
**Profile:**
- Allows user to view their own posts, navigate to the Friend Leaderboard, and access settings
  
**Individual Post:**
- Allows user to view a post of their own in more detail, edit their caption, or delete their post
- chosen song is played when viewing
  
**Map Guess:**
- GoogleMaps Support Map Fragment 
- Allows user to first place a marker and make a guess
- Shows distance from guess to real post location
  
**Post Leaderboard:**
- Displays Leaderboard of all the guesses made on a post
- Users are ranked by the distance of their guess
- Top 3 users get a badge (gold, silver, bronze)
- Highlights current users guess
  
**Friend Leaderboard:**
- Ranks friends (including user) by their average distance across all guesses
- Top 3 users get a badge (gold, silver, bronze)
- Highlights current users guess
  
### Data Classes:
**Comment**:
- keeps track of text, username, and profile photo of commenter
  
**Post**:
- keeps track of post content including image, profile picture, username, timestamp, location, and caption text

**Friend**:
- keeps track of the id, username and profile picture of a friendrture

**Guess**:
- keeps track of the username, profile picture, distance, and rank of a guess for a post

**Leaderboard**:
- keeps track of the username, profile picture, average guess distance, and rank for a user (used )

**Result/SongResponse:**
- used for the results of Jamendo API to fetch songs

### Database Design
Main Collections:
- **Posts**: all posts
- **Users**: all users
- **Friends**: friend associations for each user

Subcollections:
- Within **Posts**:
  - **Guesses**: Friend's guesses on this post
  - **Comments**: Friend's comments on this post
- Within **Users**:
  - **Guesses**: all guesses made by this user
- Within **Friends**:
  - **Incoming Requests**
  - **Outgoing Requests**
  - **Friend usernames**

### Built With

[![My Skills](https://skillicons.dev/icons?i=androidstudio,figma,firebase,gcp,github,gradle,kotlin,materialui,postman)](https://skillicons.dev)

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
