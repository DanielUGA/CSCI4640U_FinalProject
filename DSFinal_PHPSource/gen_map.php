<?php

header("Content-Type: image/png");

if (isset($_GET['mapid'])) {
	$id = $_GET['mapid'];
} else {
	$id = 0;
}


$im = imagecreatefrompng("grid2.png");
$red = imagecolorallocate($im, 255, 0, 0);
$black = imagecolorallocate($im, 0, 0, 0);

$hostname = "localhost";
$username = "allen_minerobot";
$password = "mineRobot2";
$database = "allen_minerobot";

if ($id != 0) {
	mysql_connect($hostname, $username, $password) or die("Cannot connect to MySQL DB");
	mysql_select_db($database);
	$query1 = "SELECT minestring FROM scan WHERE id=$id";
	$result = mysql_query($query1);
	$row = mysql_fetch_row($result);
	$mines = explode(".", $row['0']);
	
	foreach($mines as $mine) {
		$data = explode(",", $mine);
		imagefilledrectangle($im, $data[0], $data[1], $data[0]+10, $data[1]+10, $red);
	}
}

imagesetthickness($im, 1);
for($i = 0; $i <= 900; $i += 10) {
	imageline($im, $i, 0, $i, 500, $black);
}
for($i = 0; $i <=500; $i += 10) {
	imageline($im, 0, $i, 900, $i, $black);
}
imagesetthickness($im, 3);
for($i = 0; $i <= 900; $i += 50) {
	imageline($im, $i, 0, $i, 500, $black);
}
for($i = 0; $i <=500; $i += 50) {
	imageline($im, 0, $i, 900, $i, $black);
}


imagepng($im);

imagedestroy($im);

?>