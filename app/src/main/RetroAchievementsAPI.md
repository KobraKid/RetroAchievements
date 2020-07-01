# RetroAchievements API Reference Doc

To use:<br/>
retroachievements.org/API/`API_NAME`.php?z=`USERNAME`&y=`API_KEY`[&params=`PARAMS`]

#### Table of Contents
* [GetTopTenUsers](#gettoptenusers)
* [GetGame](#getgame)
* [GetGameExtended](#getgameextended)
* [GetConsoleIDs](#getconsoleids)
* [GetGameList](#getgamelist)
* [~~GetFeed~~](#getfeed)
* [GetUserRankAndScore](#getuserrankandscore)
* [GetUserProgress](#getuserprogress)
* [GetUserRecentlyPlayedGames](#getuserrecentlyplayedgames)
* [GetUserSummary](#getusersummary)
* [GetGameInforAndUserProgress](#getgameinfoanduserprogress)
* [GetAchievementsEarnedOnDay](#getachievementsearnedonday)
* [GetAchievementsEarnedBetween](#getachievementsearnedbetween)

## GetTopTenUsers
### Response
<pre><code>[
 {
  /* Username */
  "1": String,
  /* Score */
  "2": String,
  /* Retro Ratio */
  "3": String
 }
]</code></pre>

## GetGame
### Parameters
<pre><code>/* Game ID */
@param i: Int</code></pre>
### Response
<pre><code>{
 "Title": String,
 "ForumTopicID": String,
 "ConsoleID": String,
 "ConsoleName": String,
 "Flags": String,
 /* Escaped URL of Image Icon */
 "ImageIcon": String,
 /* Escaped URL of Game Icon */
 "GameIcon": String,
 /* Escaped URL of Game Title Screen */
 "ImageTitle": String,
 /* Escaped URL of Game Screenshot */
 "ImageIngame": String,
 /* Escaped URL of Game Box Art */
 "ImageBoxArt": String,
 "Publisher": String,
 "Developer": String,
 "Genre": String,
 "Released": String,
 "GameTitle": String,
 "Console": String
}</code></pre>

## GetGameExtended
### Parameters
<pre><code>/* Game ID */
@param i: Int</code></pre>
### Response
<pre><code>{
 /* @i */
 "ID": Int,
 "Title": String,
 "ConsoleID": Int,
 "ForumTopicID": Int,
 "Flags": Int,
 /* Escaped URL of Image Icon */
 "ImageIcon": String,
 /* Escaped URL of Game Title Screen */
 "ImageTitle": String,
 /* Escaped URL of Game Screenshot */
 "ImageIngame": String,
 /* Escaped URL of Game Box Art */
 "ImageBoxArt": String,
 "Publisher": String,
 "Developer": String,
 "Genre": String,
 "Released": String,
 "IsFinal": Boolean,
 "ConsoleName": String,
 "RichPresencePatch": String,
 "NumAchievements": Int,
 "NumDistinctPlayersCasual": String,
 "NumDistinctPlayersHardcore": String,
 "Achievements": {
  "[Achievements.ID]": {
   "ID": String,
   "NumAwarded": String,
   "NumAwardedHardcore": String,
   "Title": String,
   "Description": String,
   "Points": String,
   "TrueRatio": String,
   "Author": String,
   "DateModified": String,
   "DateCreated": String,
   "BadgeName": String,
   "DisplayOrder": String,
   "MemAddr": String
  }
 }
}</code></pre>

## GetConsoleIDs
### Response
<pre><code>[
 {
  "ID": String,
  "Name": String
 }
]</code></pre>

## GetGameList
### Parameters
<pre><code>/* Console ID */
@param i: Int</code></pre>
### Response
<pre><code>[
 {
  "Title": String,
  "ID": String,
  /* @i */
  "ConsoleID": String,
  /* Escaped URL of Image Icon */
  "ImageIcon": String,
  "ConsoleName": String
 }
]</code></pre>

## ~~GetFeed~~
### ~~Parameters~~
<pre><code>/* Unused */
@param u: String
/* Unused */
@param c: Int
/* Unused */
@param o: Int</code></pre>
### ~~Response~~
<pre><code>@Deprecated("Unused")
{
 "success": Boolean
}</code></pre>

## GetUserRankAndScore
### Parameters
<pre><code>/* Username */
@param u: String</code></pre>
### Response
<pre><code>{
 "Score": int,
 "Rank": String
}</code></pre>

## GetUserProgress
### Parameters
<pre><code>/* Username */
@param u: String
/* Game ID */
@param i: Int</code></pre>
### Response
<pre><code>{
 [@i]: { 
  "NumPossibleAchievements": String, 
  "PossibleScore": String, 
  "NumAchieved": String, 
  "ScoreAchieved": String, 
  "NumAchievedHardcore": String, 
  "ScoreAchievedHardcore": String 
 } 
}</code></pre>

## GetUserRecentlyPlayedGames
### Parameters
<pre><code>/* Username */
@param u: String
/* Number of recent games */
@param c: Int
/* Offset */
@param o: Int</code></pre>
### Response
<pre><code>[ 
 { 
  "GameID": String, 
  "ConsoleID": String, 
  "ConsoleName": String, 
  "Title": String, 
  /* Escaped URL of Image Icon */
  "ImageIcon": String, 
  "LastPlayed": String, 
  "MyVote": String?, 
  "NumPossibleAchievements": String, 
  "PossibleScore": String, 
  "NumAchieved": Int, 
  "ScoreAchieved": Int 
 } 
]</code></pre>

## GetUserSummary
### Parameters
<pre><code>/* Username */
@param u: String
/* Number of most recent games */
@param g: Int
/* Number of most recent achievements */
@param i: Int</code></pre>
### Response
<pre><code>{ 
 "RecentlyPlayedCount": Int, 
 /* Most recent @g games */
 "RecentlyPlayed": [ 
  { 
   "GameID": String, 
   "ConsoleID": String, 
   "ConsoleName": String, 
   "Title": String, 
   "ImageIcon": String, 
   "LastPlayed": String, 
   "MyVote": String? 
  } 
 ], 
 "MemberSince": String, 
 "LastActivity": { 
   "ID": String, 
   "timestamp": String, 
   "lastupdate": String, 
   "activitytype": String, 
   "User": String,
   /* Most recent achievement? */
   "data": String?, 
   /* Unknown */
   "data2": String? 
  },
 "RichPresenceMsg": String, 
 "LastGameID": String,
 "LastGame": {
  "ID": Int,
  "Title": String,
  "ConsoleID": Int,
  "ForumTopicID": Int,
  "Flags": Int,
  /* Escaped URL of Image Icon */
  "ImageIcon": String,
  /* Escaped URL of Game Title Screen */
  "ImageTitle": String,
  /* Escaped URL of Game Screenshot */
  "ImageIngame": String,
  /* Escaped URL of Game Box Art */
  "ImageBoxArt": String,
  "Publisher": String,
  "Developer": String,
  "Genre": String,
  "Released": String,
  "IsFinal": Boolean,
  "ConsoleName": String,
  "RichPresencePatch": String?
 },
 "ContribCount": String, 
 "ContribYield": String, 
 "TotalPoints": String,
 /* Retro Ratio */ 
 "TotalTruePoints": String, 
 "Permissions": String, 
 "Untracked": String, 
 "ID": String, 
 "UserWallActive": String, 
 "Motto": String, 
 "Rank": String, 
 /* Achievements awarded to @u for the most recent @g games */
 "Awarded":{ 
  "[GameID]": { 
   "NumPossibleAchievements": String, 
   "PossibleScore": String, 
   "NumAchieved": String, 
   "ScoreAchieved": String, 
   "NumAchievedHardcore": String, 
   "ScoreAchievedHardcore": String 
  } 
 } 
 /* Most recent @a achievements */
 "RecentAchievements": { 
  "Game ID": { 
   "Achievement ID": { 
    "ID": String, 
    "GameID": String, 
    "GameTitle": String, 
    "Title": String, 
    "Description": String, 
    "Points": String, 
    "BadgeName": String, 
    "IsAwarded": String, 
    "DateAwarded": String, 
    "HardcoreAchieved": String 
   } 
  } 
 } 
 "Points": String, 
 "UserPic": String, 
 "Status": String 
}</code></pre>

## GetGameInfoAndUserProgress
### Parameters
<pre><code>/* Username */
@param u: String
/* Game ID */
@param g: Int</code></pre>
### Response
<pre><code>{
 "ID": Int,
 "Title": String,
 "ConsoleID": Int,
 "ForumTopicID": Int,
 "Flags": Int,
 /* Escaped URL of Image Icon */
 "ImageIcon": String,
 /* Escaped URL of Game Title Screen */
 "ImageTitle": String,
 /* Escaped URL of Game Screenshot */
 "ImageIngame": String,
 /* Escaped URL of Game Box Art */
 "ImageBoxArt": String,
 "Publisher": String,
 "Developer": String,
 "Genre": String,
 "Released": String,
 "IsFinal": Boolean,
 "ConsoleName": String,
 "RichPresencePatch": String,
 "NumAchievements": Int,
 "NumDistinctPlayersCasual": String,
 "NumDistinctPlayersHardcore": String,
 "Achievements": {
  "Achievement ID": {
  "ID": String,
  "NumAwarded": String,
  "NumAwardedHardcore": String,
  "Title": String,
  "Description": String,
  "Points": String,
  "TrueRatio": String,
  "Author": String,
  "DateModified": String,
  "DateCreated": String,
  "BadgeName": String,
  "DisplayOrder": String,
  "MemAddr": String,
  "DateEarned": String,
  "DateEarnedHardcore": String
  }
 },
 "NumAwardedToUser": Int,
 "NumAwardedToUserHardcore": Int,
 "UserCompletion": String,
 "UserCompletionHardcore": String
}</code></pre>

## GetAchievementsEarnedOnDay
### Parameters
<pre><code>/* Username */
@param u: String
/* Date */
@param d: String</code></pre>
### Response
<pre><code>[
 {
  "Date": String,
  "HardcoreMode": String,
  "AchievementID": String,
  "Title": String,
  "Description": String,
  "BadgeName": String,
  "Points": String,
  "Author": String,
  "GameTitle": String,
  /* Escaped URL of Game Icon */
  "GameIcon": String,
  "GameID": String,
  "ConsoleName": String,
  /* The cumulative score of @u throughout the day */
  "CumulScore": Int,
  /* Escaped URL of Badge */
  "BadgeURL": String,
  /* Escaped URL of Game */
  "GameURL": String
 }
]</code></pre>

## GetAchievementsEarnedBetween
### Parameters
<pre><code>/* Username */
@param u: String
/* From date (in seconds since Epoch) */
@param f: String
/* To date (in seconds since Epoch) */
@param t: String</code></pre>
### Response
<pre><code>[
 {
  "Date": String,
  "HardcoreMode": String,
  "AchievementID": String,
  "Title": String,
  "Description": String,
  "BadgeName": String,
  "Points": String,
  "Author": String,
  "GameTitle": String,
  /* Escaped URL of Game Icon */
  "GameIcon": String,
  "GameID": String,
  "ConsoleName": String,
  /* The cumulative score of @u throughout the day */
  "CumulScore": Int,
  /* Escaped URL of Badge */
  "BadgeURL": String,
  /* Escaped URL of Game */
  "GameURL": String
 }
]</code></pre>
