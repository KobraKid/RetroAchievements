<html>
	<head>
	</head>
	<body>
		<h1>Tests:</h1>
		<?php
		require_once( "RA_API.php" );
		$RAConn = new RetroAchievements('KobraKid1337', 'LrY9UvdmckJWfgTsVC5SdTODrlTcHrkj' );
		echo "<pre>";
		print_r($RAConn->GetTopTenUsers());
		echo "</pre></div></div><hr><pre>";
		print_r($RAConn->GetUserRecentlyPlayedGames('KobraKid1337'));
		echo "</pre><hr><pre>";
		// print_r($RAConn->GetAchievementsEarnedBetween('KobraKid1337', '2016-12-31 20:00:00', '2018-01-01 04:00:00'));
		echo "</pre><hr><pre>";
		print_r($RAConn->GetGameInfoExtended( 504 ));
		echo "</pre><hr><pre>";
		print_r($RAConn->GetGameInfo( 504 ));
		echo "</pre><hr><pre>";
		// print_r($RAConn->GetGameList( 4 ));
		echo "</pre><hr><pre>";
		print_r($RAConn->GetConsoleIDs());
		echo "</pre><hr><pre>";
		print_r($RAConn->GetFeedFor('Scott', 10));
		echo "</pre><hr>";
		echo "<hr>";
		?>
	</body>
</html>
