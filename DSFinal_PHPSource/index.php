<?php

$hostname = "localhost";
$username = "allen_minerobot";
$password = "mineRobot2";
$database = "allen_minerobot";

mysql_connect($hostname, $username, $password) or die("Cannot connect to MySQL DB");
mysql_select_db($database);

$query1 = "SELECT id, date FROM scan ORDER BY id DESC";
$result = mysql_query($query1);
echo "<br /><form action=\"index.php\" method=\"post\">\n";
echo "<select name=\"viewId\">\n";
while(($row = mysql_fetch_array($result)) != NULL) {
	echo "<option value=\"{$row['id']}\">Scan ({$row['id']}) - {$row['date']}</option>\n";
}

echo "</select>\n";
echo "<input type=\"submit\" name=\"submit\" value=\"Show Map History\" />\n";
echo "</form>\n";
echo "<hr><br /><br />";

// if post
if(isset($_POST['submit'])) {
	$mapId = $_POST['viewId'];
} else {
	$mapId = 0;
}
echo "<html><head></head><body>\n";
echo "<img src=\"gen_map.php?mapid=$mapId&t=" . time() . "\" width=\"900\" height=\"500\" border=\"3\" />\n";
echo "</body></html>\n";

?>